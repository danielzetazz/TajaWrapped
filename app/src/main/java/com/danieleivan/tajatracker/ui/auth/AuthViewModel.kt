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
    val accountUsername: String? = null,
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

    init {
        observeSessionState()
    }

    private fun observeSessionState() {
        viewModelScope.launch {
            repository.observeSessionAuthenticated().collect { isAuthenticated ->
                _uiState.update { state ->
                    state.copy(isAuthenticated = isAuthenticated)
                }

                if (isAuthenticated) {
                    loadCurrentUsername(silent = true)
                } else {
                    _uiState.update { state -> state.copy(accountUsername = null) }
                }
            }
        }
    }

    fun signIn(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Usuario y contraseña son obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            repository.signInWithUsername(username.trim(), password)
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

    fun signUp(email: String, username: String, password: String) {
        if (email.isBlank() || username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email, usuario y contraseña son obligatorios") }
            return
        }

        if (username.length < 3) {
            _uiState.update { it.copy(errorMessage = "El usuario debe tener al menos 3 caracteres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            repository.signUpWithUsername(email.trim(), username.trim(), password)
                .onSuccess {
                    val hasSession = repository.hasActiveSession()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = hasSession,
                            errorMessage = if (hasSession) null else "La verificación por email sigue activa en Supabase. Desactívala para acceso inmediato.",
                            infoMessage = if (hasSession) "Cuenta creada y acceso concedido" else null
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

    fun loadCurrentUsername(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            }

            repository.getCurrentUsername()
                .onSuccess { username ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            accountUsername = username
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo cargar el usuario"
                        )
                    }
                }
        }
    }

    fun updateUsername(newUsername: String) {
        if (newUsername.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El usuario no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            repository.updateUsername(newUsername)
                .onSuccess { updatedUsername ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            accountUsername = updatedUsername,
                            infoMessage = "Usuario actualizado"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo actualizar el usuario"
                        )
                    }
                }
        }
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

