package com.danieleivan.tajatracker.ui.stats

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun WrappedStatsScreen(
    viewModel: WrappedStatsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTricksDialog by remember { mutableStateOf(false) }
    var showPlacesDialog by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "wrappedTransition")
    val titleScale by transition.animateFloat(
        initialValue = 0.99f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Tu DrunkWrapped 2026",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .scale(titleScale),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Fiesta, numeros y cero remordimientos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        RowRangeSelector(
            selectedRange = uiState.selectedRange,
            onRangeSelected = viewModel::seleccionarRango
        )

        if (uiState.isLoading) {
            WrappedCard("Cargando estadisticas...")
        } else if (uiState.errorMessage != null) {
            WrappedCard("Ups, hubo un fallo: ${uiState.errorMessage}")
            Button(
                onClick = viewModel::cargarEstadisticas,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("REINTENTAR", style = MaterialTheme.typography.labelLarge)
            }
        } else {
            WrappedCard(
                title = "Pasta invertida",
                value = "${formatEuros(uiState.totalGastado)} EUR",
                subtitle = "Hoy toca mirar el ticket..."
            )
            WrappedCard(
                title = "Dinero ahorrado",
                value = "${formatEuros(uiState.totalAhorrado)} EUR",
                subtitle = "Gracias al modo Robin Hood"
            )
            WrappedCard(
                title = "Bebida mas consumida",
                value = uiState.topBebida,
                subtitle = "El algoritmo te conoce mejor que tu ex"
            )
            WrappedCard(
                title = "Total chupitos",
                value = uiState.totalChupitos.toString(),
                subtitle = "Velocidad de vertigo certificada"
            )

            Button(
                onClick = { showTricksDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(
                    text = "VER RESUMEN DE TRUCOS",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = { showPlacesDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "VER RESUMEN DE LUGARES",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "VOLVER AL REGISTRO",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        if (showTricksDialog) {
            TricksSummaryDialog(
                tricks = uiState.trucosResumen,
                total = uiState.totalTrucos,
                onDismiss = { showTricksDialog = false }
            )
        }

        if (showPlacesDialog) {
            PlacesSummaryDialog(
                range = uiState.selectedRange,
                places = uiState.lugaresResumen,
                onDismiss = { showPlacesDialog = false }
            )
        }
    }
}

@Composable
private fun TricksSummaryDialog(
    tricks: List<TrucoProgress>,
    total: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("CERRAR")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Resumen de Trucos",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Trucos totales: $total",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${tricks.count { it.veces > 0 }} categorias desbloqueadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                tricks.forEach { trick ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${trick.nombre}: ${trick.veces}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = trick.descripcion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!trick.trackeable) {
                                Text(
                                    text = trick.nota.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun PlacesSummaryDialog(
    range: StatsRange,
    places: List<LugarResumen>,
    onDismiss: () -> Unit
) {
    val rangeLabel = when (range) {
        StatsRange.LAST_7_DAYS -> "Ultimos 7 dias"
        StatsRange.LAST_30_DAYS -> "Ultimos 30 dias"
        StatsRange.ALL_TIME -> "Historico"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("CERRAR")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Resumen de lugares · $rangeLabel",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Lugares totales: ${places.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (places.isEmpty()) {
                    Text(
                        text = "Todavia no hay lugares registrados en este periodo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    places.forEach { place ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = place.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${place.totalRegistros} noches registradas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun RowRangeSelector(
    selectedRange: StatsRange,
    onRangeSelected: (StatsRange) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Periodo",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        RowRangeButtons(
            title = "7 dias",
            isSelected = selectedRange == StatsRange.LAST_7_DAYS,
            onClick = { onRangeSelected(StatsRange.LAST_7_DAYS) }
        )
        RowRangeButtons(
            title = "30 dias",
            isSelected = selectedRange == StatsRange.LAST_30_DAYS,
            onClick = { onRangeSelected(StatsRange.LAST_30_DAYS) }
        )
        RowRangeButtons(
            title = "Historico",
            isSelected = selectedRange == StatsRange.ALL_TIME,
            onClick = { onRangeSelected(StatsRange.ALL_TIME) }
        )
    }
}

@Composable
private fun RowRangeButtons(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WrappedCard(
    title: String,
    value: String = "",
    subtitle: String = ""
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (value.isNotBlank()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatEuros(value: Double): String = String.format(Locale.US, "%.2f", value)
