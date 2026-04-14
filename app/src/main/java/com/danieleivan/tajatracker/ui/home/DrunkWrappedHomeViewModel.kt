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
        esRobado: Boolean
    ) {
        viewModelScope.launch {
            _saveState.value = SaveConsumicionUiState(isSaving = true)

            val precioPagado = if (esRobado) 0.0 else precioCapturado
            val valorEstimado = if (esRobado) precioCapturado else null

            val payload = ConsumicionInsert(
                formato = formato,
                alcoholBase = alcoholBase,
                mezcla = mezcla,
                conHielo = conHielo,
                precioPagado = precioPagado,
                esRobado = esRobado,
                valorEstimado = valorEstimado
            )

            repository.insertConsumicion(payload)
                .onSuccess {
                    _saveState.update {
                        SaveConsumicionUiState(isSuccess = true)
                    }
                }
                .onFailure { error ->
                    _saveState.update {
                        SaveConsumicionUiState(errorMessage = error.message ?: "Error al insertar en Supabase")
                    }
                }
        }
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

