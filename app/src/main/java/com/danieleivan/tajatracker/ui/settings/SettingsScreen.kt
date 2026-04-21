package com.danieleivan.tajatracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danieleivan.tajatracker.ui.components.AppButtonTone
import com.danieleivan.tajatracker.ui.components.PremiumButton
import com.danieleivan.tajatracker.ui.components.PremiumCard

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit = {},
    currentUsername: String? = null,
    onRefreshAccountData: () -> Unit = {},
    onUpdateUsername: (String) -> Unit = {},
    isAuthActionLoading: Boolean = false,
    authErrorMessage: String? = null,
    authInfoMessage: String? = null
) {
    var discreetMode by rememberSaveable { mutableStateOf(false) }
    var confirmBeforeRegister by rememberSaveable { mutableStateOf(true) }
    var hydrationReminderEnabled by rememberSaveable { mutableStateOf(false) }
    var hydrationIntervalIndex by rememberSaveable { mutableIntStateOf(1) }
    var clearDraftOnExit by rememberSaveable { mutableStateOf(true) }
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }
    var usernameDraft by rememberSaveable { mutableStateOf("") }

    val intervals = listOf(30, 45, 60)

    LaunchedEffect(Unit) {
        onRefreshAccountData()
    }

    LaunchedEffect(currentUsername) {
        if (!currentUsername.isNullOrBlank()) {
            usernameDraft = currentUsername
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Personaliza tu experiencia Speakeasy",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        SettingsToggleCard(
            title = "Modo discreto",
            subtitle = "Reduce información sensible visible en pantalla.",
            checked = discreetMode,
            onCheckedChange = { discreetMode = it }
        )

        SettingsToggleCard(
            title = "Confirmar antes de registrar día",
            subtitle = "Muestra confirmación final antes del guardado en lote.",
            checked = confirmBeforeRegister,
            onCheckedChange = { confirmBeforeRegister = it }
        )

        SettingsToggleCard(
            title = "Recordatorio de hidratación",
            subtitle = "Activa avisos de agua durante sesiones largas.",
            checked = hydrationReminderEnabled,
            onCheckedChange = { hydrationReminderEnabled = it }
        )

        if (hydrationReminderEnabled) {
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Intervalo de recordatorio",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        intervals.forEachIndexed { index, minutes ->
                            Button(
                                onClick = { hydrationIntervalIndex = index },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 54.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hydrationIntervalIndex == index) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    contentColor = if (hydrationIntervalIndex == index) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            ) {
                                Text(
                                    text = "${minutes}m",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        SettingsToggleCard(
            title = "Limpiar borrador al salir",
            subtitle = "Si se desactiva, el registro temporal se conserva.",
            checked = clearDraftOnExit,
            onCheckedChange = { clearDraftOnExit = it }
        )

        Text(
            text = "Nota: en esta versión los ajustes son locales de sesión.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Cuenta",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        PremiumCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Nombre de usuario",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = usernameDraft,
                    onValueChange = { usernameDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Usuario") },
                    singleLine = true,
                    enabled = !isAuthActionLoading
                )

                PremiumButton(
                    text = if (isAuthActionLoading) "ACTUALIZANDO..." else "GUARDAR USUARIO",
                    onClick = { onUpdateUsername(usernameDraft) },
                    enabled = !isAuthActionLoading && usernameDraft.isNotBlank() && usernameDraft != (currentUsername ?: ""),
                    modifier = Modifier.fillMaxWidth(),
                    minHeight = 56,
                    tone = AppButtonTone.Primary
                )
            }
        }

        PremiumButton(
            text = "CERRAR SESIÓN",
            onClick = { showSignOutDialog = true },
            enabled = !isAuthActionLoading,
            modifier = Modifier.fillMaxWidth(),
            minHeight = 58,
            tone = AppButtonTone.Secondary
        )

        if (authErrorMessage != null) {
            Text(
                text = authErrorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (authInfoMessage != null) {
            Text(
                text = authInfoMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        PremiumButton(
            text = "VOLVER AL MENÚ",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            minHeight = 62,
            tone = AppButtonTone.Tertiary
        )

        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            showSignOutDialog = false
                            onSignOut()
                        }
                    ) {
                        Text("CONFIRMAR")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showSignOutDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("CANCELAR")
                    }
                },
                title = { Text("Cerrar sesión") },
                text = { Text("¿Seguro que quieres cerrar sesión en este dispositivo?") }
            )
        }

        // La eliminación de cuenta se gestionará desde backend seguro (Edge Function)
        // cuando esté disponible; en cliente solo exponemos cierre de sesión.
    }
}

@Composable
private fun SettingsToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

