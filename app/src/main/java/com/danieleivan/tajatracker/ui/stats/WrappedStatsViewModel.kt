package com.danieleivan.tajatracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danieleivan.tajatracker.data.model.ConsumicionRow
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
    val totalChupitos: Int = 0
)

class WrappedStatsViewModel(
    private val repository: ConsumicionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WrappedStatsUiState())
    val uiState: StateFlow<WrappedStatsUiState> = _uiState.asStateFlow()
    private var allRows: List<ConsumicionRow> = emptyList()

    init {
        cargarEstadisticas()
    }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getConsumiciones()
                .onSuccess { rows ->
                    allRows = rows
                    applyRange(uiState.value.selectedRange)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudieron cargar las estadisticas"
                        )
                    }
                }
        }
    }

    fun seleccionarRango(range: StatsRange) {
        applyRange(range)
    }

    private fun applyRange(range: StatsRange) {
        val filtered = filterByRange(allRows, range)
        _uiState.value = WrappedStatsUiState(
            isLoading = false,
            selectedRange = range,
            totalGastado = filtered.sumOf { it.precioPagado ?: 0.0 },
            totalAhorrado = filtered.filter { it.esRobado }.sumOf { it.valorEstimado ?: 0.0 },
            topBebida = calculateTopDrink(filtered),
            totalChupitos = filtered.count { it.formato.equals("chupito", ignoreCase = true) }
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

