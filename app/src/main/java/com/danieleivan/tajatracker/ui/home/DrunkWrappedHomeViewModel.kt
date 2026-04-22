package com.danieleivan.tajatracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danieleivan.tajatracker.data.model.ConsumicionInsert
import com.danieleivan.tajatracker.data.model.LugarInsert
import com.danieleivan.tajatracker.data.model.LugarRow
import com.danieleivan.tajatracker.data.model.RegistroInsert
import com.danieleivan.tajatracker.data.remote.SupabaseProvider
import com.danieleivan.tajatracker.data.repository.ConsumicionesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

data class SaveConsumicionUiState(
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class PlacesUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val places: List<LugarRow> = emptyList()
)

class DrunkWrappedHomeViewModel(
    private val repository: ConsumicionesRepository
) : ViewModel() {

    private val _saveState = MutableStateFlow(SaveConsumicionUiState())
    val saveState: StateFlow<SaveConsumicionUiState> = _saveState.asStateFlow()

    private val _placesState = MutableStateFlow(PlacesUiState())
    val placesState: StateFlow<PlacesUiState> = _placesState.asStateFlow()

    init {
        loadPlaces()
    }


    fun guardarConsumicion(
        formato: String,
        alcoholBase: String,
        mezcla: String?,
        conHielo: Boolean,
        precioCapturado: Double,
        esRobado: Boolean,
        cantidad: Int = 1,
        lugarNombre: String? = null
    ) {
        guardarRegistro(
            listOf(
                DrinkDraft(
                    formato = formato,
                    alcoholBase = alcoholBase,
                    mezcla = mezcla,
                    conHielo = conHielo,
                    precioCapturado = precioCapturado,
                    esRobado = esRobado,
                    cantidad = cantidad,
                    hidalgoCount = 0
                )
            ),
            lugarNombre = lugarNombre.orEmpty(),
            vomitosTotal = 0,
            fechaRegistro = OffsetDateTime.now().toString()
        )
    }

    fun guardarRegistro(
        registro: List<DrinkDraft>,
        lugarNombre: String,
        vomitosTotal: Int,
        fechaRegistro: String = OffsetDateTime.now().toString()
    ) {
        viewModelScope.launch {
            _saveState.value = SaveConsumicionUiState(isSaving = true)

            if (registro.isEmpty()) {
                _saveState.update {
                    SaveConsumicionUiState(errorMessage = "El registro está vacío")
                }
                return@launch
            }

            val lugarNormalizado = lugarNombre.trim()
            if (lugarNormalizado.isBlank()) {
                _saveState.update {
                    SaveConsumicionUiState(errorMessage = "Indica dónde has bebido antes de registrar")
                }
                return@launch
            }

            val fechaRegistroNormalizada = normalizarFechaRegistro(fechaRegistro)
            val registroId = UUID.randomUUID().toString()
            val hidalgoTotal = registro.sumOf { it.hidalgoCount.coerceAtLeast(0) }
            val registroPayload = RegistroInsert(
                id = registroId,
                fechaHora = fechaRegistroNormalizada,
                lugarNombre = lugarNormalizado,
                cubatasHidalgoTotal = hidalgoTotal,
                vomitosTotal = vomitosTotal.coerceAtLeast(0)
            )

            val registroResult = repository.insertRegistro(registroPayload)
            if (registroResult.isFailure) {
                _saveState.update {
                    SaveConsumicionUiState(
                        errorMessage = registroResult.exceptionOrNull()?.message
                            ?: "No se pudo guardar el registro de la noche"
                    )
                }
                return@launch
            }

            registro.forEachIndexed { index, item ->
                val precioPagado = if (item.esRobado) 0.0 else item.precioCapturado
                val valorEstimado = if (item.esRobado) item.precioCapturado else null

                val payload = ConsumicionInsert(
                    fechaHora = fechaRegistroNormalizada,
                    registroId = registroId,
                    lugarNombre = lugarNormalizado,
                    formato = item.formato,
                    alcoholBase = item.alcoholBase,
                    mezcla = item.mezcla,
                    conHielo = item.conHielo,
                    precioPagado = precioPagado,
                    esRobado = item.esRobado,
                    valorEstimado = valorEstimado
                )

                val total = item.cantidad.coerceAtLeast(1)
                repeat(total) { repeatIndex ->
                    val result = repository.insertConsumicion(payload)
                    if (result.isFailure) {
                        val error = result.exceptionOrNull()
                        _saveState.update {
                            SaveConsumicionUiState(
                                errorMessage = error?.message
                                    ?: "Error al insertar en Supabase (${index + 1}.${repeatIndex + 1}/$total)"
                            )
                        }
                        return@launch
                    }
                }
            }

            _saveState.update {
                SaveConsumicionUiState(isSuccess = true)
            }
        }
    }

    fun loadPlaces() {
        viewModelScope.launch {
            _placesState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            repository.syncLugaresFromRegistros()
                .onSuccess {
                    repository.getLugares()
                        .onSuccess { places ->
                            _placesState.update {
                                it.copy(
                                    isLoading = false,
                                    places = places,
                                    errorMessage = null
                                )
                            }
                        }
                        .onFailure { error ->
                            _placesState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = error.message ?: "No se pudieron cargar los lugares"
                                )
                            }
                        }
                }
                .onFailure { error ->
                    _placesState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudieron sincronizar los lugares"
                        )
                    }
                }
        }
    }

    fun addPlace(nombre: String, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            val normalized = normalizePlaceName(nombre)
            if (normalized.isBlank()) {
                _placesState.update { it.copy(errorMessage = "Introduce un nombre de lugar") }
                return@launch
            }

            val alreadyExists = _placesState.value.places.any {
                normalizePlaceName(it.nombre) == normalized
            }

            _placesState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            repository.insertLugar(
                LugarInsert(
                    nombre = nombre.trim(),
                    nombreNormalizado = normalized
                )
            ).onSuccess {
                repository.getLugares()
                    .onSuccess { places ->
                        val canonicalName = places.firstOrNull {
                            normalizePlaceName(it.nombre) == normalized
                        }?.nombre ?: nombre.trim()

                        _placesState.update {
                            it.copy(
                                isLoading = false,
                                places = places,
                                errorMessage = null,
                                infoMessage = if (alreadyExists) {
                                    "Lugar ya existente reutilizado"
                                } else {
                                    "Lugar añadido a la lista"
                                }
                            )
                        }
                        onSuccess(canonicalName)
                    }
                    .onFailure { error ->
                        _placesState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "No se pudo refrescar la lista de lugares"
                            )
                        }
                    }
            }.onFailure { error ->
                _placesState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudo añadir el lugar"
                    )
                }
            }
        }
    }

    fun deletePlace(lugarId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _placesState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            repository.deleteLugar(lugarId)
                .onSuccess {
                    loadPlaces()
                    onSuccess()
                    _placesState.update { it.copy(infoMessage = "Lugar eliminado") }
                }
                .onFailure { error ->
                    _placesState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo eliminar el lugar"
                        )
                    }
                }
        }
    }

    private fun normalizarFechaRegistro(fechaRegistro: String): String {
        val input = fechaRegistro.trim()
        if (input.isBlank()) return OffsetDateTime.now().toString()

        runCatching { OffsetDateTime.parse(input).toString() }
            .getOrNull()
            ?.let { return it }

        return runCatching {
            LocalDate.parse(input)
                .atTime(LocalTime.now())
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .toString()
        }.getOrElse {
            OffsetDateTime.now().toString()
        }
    }

    private fun normalizePlaceName(value: String): String {
        return value.trim()
            .replace(Regex("\\s+"), " ")
            .lowercase()
    }

    fun limpiarEstadoGuardado() {
        _saveState.value = SaveConsumicionUiState()
    }
}

class DrunkWrappedHomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrunkWrappedHomeViewModel::class.java)) {
            val repository = ConsumicionesRepository(SupabaseProvider.client)
            @Suppress("UNCHECKED_CAST")
            return DrunkWrappedHomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

