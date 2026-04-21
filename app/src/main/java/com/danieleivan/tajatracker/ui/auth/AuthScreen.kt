package com.danieleivan.tajatracker.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danieleivan.tajatracker.ui.components.AppButtonTone
import com.danieleivan.tajatracker.ui.components.PremiumButton
import com.danieleivan.tajatracker.ui.components.PremiumCard

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
    onOpenRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Acceso privado",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Tu noche, bajo control.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            viewModel.clearMessages()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Usuario") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.clearMessages()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    PremiumButton(
                        text = if (uiState.isLoading) "ACCEDIENDO..." else "INICIAR SESIÓN",
                        onClick = { viewModel.signIn(username, password) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth(),
                        minHeight = 60,
                        tone = AppButtonTone.Primary
                    )

                    PremiumButton(
                        text = "CREAR CUENTA",
                        onClick = onOpenRegister,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth(),
                        minHeight = 58,
                        tone = AppButtonTone.Secondary
                    )

                    Text(
                        text = "El acceso se realiza con tu nombre de usuario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (uiState.infoMessage != null) {
                        Text(
                            text = uiState.infoMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

