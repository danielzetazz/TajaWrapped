package com.danieleivan.tajatracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danieleivan.tajatracker.data.model.ConsumicionRow
import com.danieleivan.tajatracker.data.model.RegistroRow
import com.danieleivan.tajatracker.data.remote.SupabaseProvider
import com.danieleivan.tajatracker.data.repository.ConsumicionesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class StatsRange {
    LAST_7_DAYS,
    LAST_30_DAYS,
    ALL_TIME
}

data class WrappedStatsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedRange: StatsRange = StatsRange.ALL_TIME,
    val totalGastado: Double = 0.0,
    val totalAhorrado: Double = 0.0,
    val topBebida: String = "Sin datos",
    val totalChupitos: Int = 0,
    val totalRegistros: Int = 0,
    val totalTrucos: Int = 0,
    val trucosDesbloqueados: Int = 0,
    val trucosResumen: List<TrucoProgress> = emptyList(),
    val totalLugares: Int = 0,
    val lugaresResumen: List<LugarResumen> = emptyList(),
    val registrosResumen: List<RegistroResumenUi> = emptyList()
)

data class LugarResumen(
    val nombre: String,
    val totalRegistros: Int
)

data class ConsumicionResumenUi(
    val descripcion: String,
    val cantidad: Int
)

data class RegistroResumenUi(
    val id: String,
    val fechaTexto: String,
    val lugarNombre: String,
    val fotoUri: String? = null,
    val cubatasHidalgoTotal: Int,
    val vomitosTotal: Int,
    val totalConsumiciones: Int,
    val consumicionesResumen: List<ConsumicionResumenUi>
)

class WrappedStatsViewModel(
    private val repository: ConsumicionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WrappedStatsUiState())
    val uiState: StateFlow<WrappedStatsUiState> = _uiState.asStateFlow()
    private var allRows: List<ConsumicionRow> = emptyList()
    private var allRegistros: List<RegistroRow> = emptyList()

    init {
        cargarEstadisticas()
    }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val consumicionesResult = repository.getConsumiciones()
            val registrosResult = repository.getRegistros()

            if (consumicionesResult.isFailure || registrosResult.isFailure) {
                val error = consumicionesResult.exceptionOrNull() ?: registrosResult.exceptionOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error?.message ?: "No se pudieron cargar las estadisticas"
                    )
                }
                return@launch
            }

            allRows = consumicionesResult.getOrDefault(emptyList())
            allRegistros = registrosResult.getOrDefault(emptyList())
            applyRange(uiState.value.selectedRange)
        }
    }

    fun seleccionarRango(range: StatsRange) {
        applyRange(range)
    }

    private fun applyRange(range: StatsRange) {
        val filtered = filterByRange(allRows, range)
        val filteredRegistros = filterRegistrosByRange(allRegistros, range)
        val hidalgoTotal = filteredRegistros.sumOf { it.cubatasHidalgoTotal }
        val vomitosTotal = filteredRegistros.sumOf { it.vomitosTotal }
        val registrosResumen = calculateRegistroSummary(filteredRegistros, filtered)
        val tricks = calculateTrucosSummary(
            rows = filtered,
            hidalgoTotal = hidalgoTotal,
            malacopaTotal = vomitosTotal
        )
        val places = calculatePlaceSummary(filteredRegistros)
        _uiState.value = WrappedStatsUiState(
            isLoading = false,
            selectedRange = range,
            totalGastado = filtered.sumOf { it.precioPagado ?: 0.0 },
            totalAhorrado = filtered.filter { it.esRobado }.sumOf { it.valorEstimado ?: 0.0 },
            topBebida = calculateTopDrink(filtered),
            totalChupitos = filtered.count { it.formato.equals("chupito", ignoreCase = true) },
            totalRegistros = registrosResumen.size,
            totalTrucos = tricks.totalActivaciones,
            trucosDesbloqueados = tricks.trucosDesbloqueados,
            trucosResumen = tricks.detalles,
            totalLugares = places.size,
            lugaresResumen = places,
            registrosResumen = registrosResumen
        )
    }

    private fun filterByRange(rows: List<ConsumicionRow>, range: StatsRange): List<ConsumicionRow> {
        val now = Instant.now()
        val limit = when (range) {
            StatsRange.LAST_7_DAYS -> now.minus(7, ChronoUnit.DAYS)
            StatsRange.LAST_30_DAYS -> now.minus(30, ChronoUnit.DAYS)
            StatsRange.ALL_TIME -> null
        }

        if (limit == null) return rows
        return rows.filter { row ->
            val parsed = parseFechaHora(row.fechaHora)
            parsed != null && !parsed.isBefore(limit)
        }
    }

    private fun filterRegistrosByRange(rows: List<RegistroRow>, range: StatsRange): List<RegistroRow> {
        val now = Instant.now()
        val limit = when (range) {
            StatsRange.LAST_7_DAYS -> now.minus(7, ChronoUnit.DAYS)
            StatsRange.LAST_30_DAYS -> now.minus(30, ChronoUnit.DAYS)
            StatsRange.ALL_TIME -> null
        }

        if (limit == null) return rows
        return rows.filter { row ->
            val parsed = parseFechaHora(row.fechaHora)
            parsed != null && !parsed.isBefore(limit)
        }
    }

    private fun parseFechaHora(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        return runCatching { OffsetDateTime.parse(value).toInstant() }
            .recoverCatching { Instant.parse(value) }
            .getOrNull()
    }

    private fun calculateTopDrink(rows: List<ConsumicionRow>): String {
        if (rows.isEmpty()) return "Top 1: Aun no hay consumiciones"

        val grouped = rows.groupBy { row ->
            buildString {
                append(row.formato.ifBlank { "Bebida" })
                if (row.alcoholBase.isNotBlank()) {
                    append(" de ")
                    append(row.alcoholBase)
                }
                if (!row.mezcla.isNullOrBlank()) {
                    append(" con ")
                    append(row.mezcla)
                }
                append(if (row.conHielo) " y hielo" else " sin hielo")
            }
        }

        val winner = grouped.maxByOrNull { it.value.size }?.key ?: "Aun no hay consumiciones"
        return "Top 1: $winner"
    }

    private fun calculatePlaceSummary(rows: List<RegistroRow>): List<LugarResumen> {
        return rows
            .asSequence()
            .filter { it.lugarNombre.isNotBlank() }
            .groupBy { it.lugarNombre.trim().lowercase() }
            .map { (_, groupedRows) ->
                LugarResumen(
                    nombre = groupedRows.first().lugarNombre.trim(),
                    totalRegistros = groupedRows.size
                )
            }
            .sortedWith(
                compareByDescending<LugarResumen> { it.totalRegistros }
                    .thenBy { it.nombre.lowercase() }
            )
    }

    private fun calculateRegistroSummary(
        registros: List<RegistroRow>,
        consumiciones: List<ConsumicionRow>
    ): List<RegistroResumenUi> {
        val consumicionesByRegistro = consumiciones
            .asSequence()
            .filter { !it.registroId.isNullOrBlank() }
            .groupBy { it.registroId.orEmpty() }

        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es-ES"))

        return registros
            .sortedByDescending { parseFechaHora(it.fechaHora) ?: Instant.EPOCH }
            .map { registro ->
                val registroConsumiciones = consumicionesByRegistro[registro.id].orEmpty()
                val groupedDescriptions = registroConsumiciones
                    .groupBy { describeConsumicion(it) }
                    .map { (descripcion, rows) ->
                        ConsumicionResumenUi(
                            descripcion = descripcion,
                            cantidad = rows.size
                        )
                    }
                    .sortedWith(
                        compareByDescending<ConsumicionResumenUi> { it.cantidad }
                            .thenBy { it.descripcion.lowercase() }
                    )

                RegistroResumenUi(
                    id = registro.id,
                    fechaTexto = parseFechaHora(registro.fechaHora)
                        ?.atZone(java.time.ZoneId.systemDefault())
                        ?.format(dateFormatter)
                        ?: registro.fechaHora.orEmpty().ifBlank { "Sin fecha" },
                    lugarNombre = registro.lugarNombre.ifBlank { "Sin lugar" },
                    fotoUri = registro.fotoUri,
                    cubatasHidalgoTotal = registro.cubatasHidalgoTotal,
                    vomitosTotal = registro.vomitosTotal,
                    totalConsumiciones = registroConsumiciones.size,
                    consumicionesResumen = groupedDescriptions
                )
            }
    }

    private fun describeConsumicion(row: ConsumicionRow): String {
        return buildString {
            append(row.formato.ifBlank { "Bebida" })
            if (row.alcoholBase.isNotBlank()) {
                append(" | ")
                append(row.alcoholBase)
            }
            if (!row.mezcla.isNullOrBlank()) {
                append(" + ")
                append(row.mezcla)
            }
            if (row.conHielo) {
                append(" | Hielo")
            }
            if (row.esRobado) {
                append(" | Robado")
            }

            val precio = row.precioPagado ?: row.valorEstimado ?: 0.0
            append(" | ")
            append(String.format(Locale.US, "%.2f", precio))
            append(" EUR")
        }
    }
}

class WrappedStatsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WrappedStatsViewModel::class.java)) {
            val repository = ConsumicionesRepository(SupabaseProvider.client)
            @Suppress("UNCHECKED_CAST")
            return WrappedStatsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

