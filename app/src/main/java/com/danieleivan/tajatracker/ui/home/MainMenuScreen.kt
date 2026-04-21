package com.danieleivan.tajatracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danieleivan.tajatracker.ui.components.AppButtonTone
import com.danieleivan.tajatracker.ui.components.PremiumButton
import com.danieleivan.tajatracker.ui.components.PremiumCard
import com.danieleivan.tajatracker.ui.theme.DrunkWrappedTheme

@Composable
fun MainMenuScreen(
    onNewRecord: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DrunkWrapped",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
            ,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Tu menú principal para registrar una noche completa, no una bebida suelta.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        PremiumCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Qué quieres hacer?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Crea un nuevo registro para una borrachera concreta o revisa tu Wrapped.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                PremiumButton(
                    text = "NUEVO REGISTRO",
                    onClick = onNewRecord,
                    modifier = Modifier
                        .fillMaxWidth(),
                    minHeight = 62,
                    tone = AppButtonTone.Primary
                )

                PremiumButton(
                    text = "VER MI WRAPPED",
                    onClick = onOpenStats,
                    modifier = Modifier
                        .fillMaxWidth(),
                    minHeight = 62,
                    tone = AppButtonTone.Secondary
                )

                PremiumButton(
                    text = "AJUSTES",
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .fillMaxWidth(),
                    minHeight = 58,
                    tone = AppButtonTone.Tertiary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainMenuScreenPreview() {
    DrunkWrappedTheme {
        MainMenuScreen(onNewRecord = {}, onOpenStats = {})
    }
}
