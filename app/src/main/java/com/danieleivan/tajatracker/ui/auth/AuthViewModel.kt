package com.danieleivan.tajatracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danieleivan.tajatracker.data.remote.SupabaseProvider
import com.danieleivan.tajatracker.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(isAuthenticated = repository.hasActiveSession())
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email y contraseña son obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            repository.signIn(email.trim(), password)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            errorMessage = null,
                            infoMessage = "Bienvenido de nuevo"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            errorMessage = error.message ?: "No se pudo iniciar sesión"
                        )
                    }
                }
        }
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email y contraseña son obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            repository.signUp(email.trim(), password)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            infoMessage = "Cuenta creada. Revisa tu email para confirmar."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo crear la cuenta"
                        )
                    }
                }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Introduce un email válido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            repository.resetPassword(email.trim())
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            infoMessage = "Te enviamos un email para recuperar contraseña."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo enviar el email de recuperación"
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun signOut(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            repository.signOut()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            infoMessage = "Sesión cerrada"
                        )
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo cerrar sesión"
                        )
                    }
                }
        }
    }
}

class AuthViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val repository = AuthRepository(SupabaseProvider.client)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

