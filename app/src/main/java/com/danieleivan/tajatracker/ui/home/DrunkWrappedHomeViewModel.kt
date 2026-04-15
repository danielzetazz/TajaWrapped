package com.danieleivan.tajatracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danieleivan.tajatracker.data.model.ConsumicionInsert
import com.danieleivan.tajatracker.data.remote.SupabaseProvider
import com.danieleivan.tajatracker.data.repository.ConsumicionesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SaveConsumicionUiState(
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class DrunkWrappedHomeViewModel(
    private val repository: ConsumicionesRepository
) : ViewModel() {

    private val _saveState = MutableStateFlow(SaveConsumicionUiState())
    val saveState: StateFlow<SaveConsumicionUiState> = _saveState.asStateFlow()


    fun guardarConsumicion(
        formato: String,
        alcoholBase: String,
        mezcla: String?,
        conHielo: Boolean,
        precioCapturado: Double,
        esRobado: Boolean,
        cantidad: Int = 1
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
                    cantidad = cantidad
                )
            )
        )
    }

    fun guardarRegistro(registro: List<DrinkDraft>) {
        viewModelScope.launch {
            _saveState.value = SaveConsumicionUiState(isSaving = true)

            if (registro.isEmpty()) {
                _saveState.update {
                    SaveConsumicionUiState(errorMessage = "El registro está vacío")
                }
                return@launch
            }

            registro.forEachIndexed { index, item ->
                val precioPagado = if (item.esRobado) 0.0 else item.precioCapturado
                val valorEstimado = if (item.esRobado) item.precioCapturado else null

                val payload = ConsumicionInsert(
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

