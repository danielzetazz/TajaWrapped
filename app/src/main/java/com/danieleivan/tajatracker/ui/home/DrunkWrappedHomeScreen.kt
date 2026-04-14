package com.danieleivan.tajatracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

private enum class DrinkFormat(val label: String, val asksMixer: Boolean) {
    COPA("Copa", true),
    CHUPITO("Chupito", false),
    CERVEZA("Cerveza", false),
    VINO("Vino", false),
    GARRAFA("Garrafa", true)
}

private enum class BaseAlcohol(val label: String) {
    RON("Ron"),
    VODKA("Vodka"),
    GIN("Gin"),
    WHISKY("Whisky"),
    TEQUILA("Tequila"),
    NONE("Sin alcohol")
}

private enum class Mixer(val label: String) {
    COLA("Cola"),
    LIMON("Limon"),
    NARANJA("Naranja"),
    TONICA("Tonica"),
    ENERGETICA("Energetica")
}

@Composable
fun DrunkWrappedHomeScreen(
    viewModel: DrunkWrappedHomeViewModel,
    onOpenStats: () -> Unit = {}
) {
    var selectedFormat by remember { mutableStateOf<DrinkFormat?>(null) }
    var selectedAlcohol by remember { mutableStateOf<BaseAlcohol?>(null) }
    var selectedMixer by remember { mutableStateOf<Mixer?>(null) }
    var withIce by remember { mutableStateOf<Boolean?>(null) }
    var priceInput by remember { mutableStateOf("0") }
    var isRobbed by remember { mutableStateOf(false) }
    var lastSaved by remember { mutableStateOf<String?>(null) }
    val saveState by viewModel.saveState.collectAsState()

    Surface(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "DrunkWrapped",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Registro rapido de consumiciones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = onOpenStats,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 84.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(
                    text = "VER MI WRAPPED",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }

            SectionTitle("1) Elige formato")
            DrinkFormat.entries.forEach { format ->
                GiantOptionButton(
                    text = format.label,
                    selected = selectedFormat == format,
                    onClick = {
                        selectedFormat = format
                        selectedAlcohol = null
                        if (!format.asksMixer) selectedMixer = null
                    }
                )
            }

            if (selectedFormat != null) {
                SectionTitle("2) Elige alcohol base")
                BaseAlcohol.entries.forEach { alcohol ->
                    GiantOptionButton(
                        text = alcohol.label,
                        selected = selectedAlcohol == alcohol,
                        onClick = { selectedAlcohol = alcohol }
                    )
                }
            }

            if (selectedFormat?.asksMixer == true) {
                SectionTitle("3) Elige refresco")
                Mixer.entries.forEach { mixer ->
                    GiantOptionButton(
                        text = mixer.label,
                        selected = selectedMixer == mixer,
                        onClick = { selectedMixer = mixer }
                    )
                }
            }

            if (selectedFormat != null) {
                SectionTitle("4) Lleva hielo?")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GiantOptionButton(
                        text = "SI",
                        selected = withIce == true,
                        onClick = { withIce = true },
                        modifier = Modifier.weight(1f)
                    )
                    GiantOptionButton(
                        text = "NO",
                        selected = withIce == false,
                        onClick = { withIce = false },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (selectedFormat != null) {
                SectionTitle("5) Coste")
                val priceLabel = if (isRobbed) "Valor estimado del robo" else "Precio"
                Text(
                    text = priceLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${formatMoney(priceInput.toDoubleOrNull() ?: 0.0)} EUR",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                NumericPad(
                    onKeyPress = { key ->
                        priceInput = when (key) {
                            "<-" -> removeLastDigit(priceInput)
                            "C" -> "0"
                            else -> appendPriceInput(priceInput, key)
                        }
                    }
                )

                Button(
                    onClick = { isRobbed = !isRobbed },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 92.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRobbed) Color(0xFFD50000) else Color(0xFF7F1D1D),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isRobbed) "¡Robado! ACTIVADO" else "¡Robado!",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            val enteredCost = priceInput.toDoubleOrNull() ?: 0.0
            val hasCost = enteredCost > 0.0
            val canSave = selectedFormat != null &&
                selectedAlcohol != null &&
                (selectedFormat?.asksMixer != true || selectedMixer != null) &&
                withIce != null &&
                hasCost

            Button(
                onClick = {
                    val realCost = if (isRobbed) 0.0 else enteredCost
                    viewModel.guardarConsumicion(
                        formato = selectedFormat?.label.orEmpty(),
                        alcoholBase = selectedAlcohol?.label.orEmpty(),
                        mezcla = if (selectedFormat?.asksMixer == true) selectedMixer?.label else null,
                        conHielo = withIce == true,
                        precioCapturado = enteredCost,
                        esRobado = isRobbed
                    )

                    lastSaved = buildString {
                        append(selectedFormat?.label)
                        append(" | ")
                        append(selectedAlcohol?.label)
                        if (selectedFormat?.asksMixer == true) {
                            append(" + ")
                            append(selectedMixer?.label)
                        }
                        append(" | Hielo: ")
                        append(if (withIce == true) "Si" else "No")
                        if (isRobbed) {
                            append(" | Valor estimado del robo: ")
                            append(formatMoney(enteredCost))
                            append(" EUR")
                            append(" | Coste real: 0.00 EUR")
                        } else {
                            append(" | Precio: ")
                            append(formatMoney(realCost))
                            append(" EUR")
                        }
                    }
                },
                enabled = canSave && !saveState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 88.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (saveState.isSaving) "GUARDANDO..." else "REGISTRAR",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }

            if (saveState.errorMessage != null) {
                Text(
                    text = "Error al guardar: ${saveState.errorMessage}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFFF6B6B)
                )
            }

            if (saveState.isSuccess) {
                Text(
                    text = "Guardado en Supabase",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (lastSaved != null) {
                Text(
                    text = "Ultima consumicion: $lastSaved",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

private fun appendPriceInput(currentValue: String, key: String): String {
    if (key == ".") {
        return if (currentValue.contains('.')) currentValue else "$currentValue."
    }

    if (key !in "0123456789") return currentValue
    if (currentValue == "0") return key
    return currentValue + key
}

private fun removeLastDigit(currentValue: String): String {
    if (currentValue.length <= 1) return "0"
    val updated = currentValue.dropLast(1)
    return if (updated == "" || updated == "-") "0" else updated
}

private fun formatMoney(value: Double): String = String.format(Locale.US, "%.2f", value)

@Composable
private fun NumericPad(onKeyPress: (String) -> Unit) {
    val keypadRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "<-")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        keypadRows.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowKeys.forEach { key ->
                    Button(
                        onClick = { onKeyPress(key) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 88.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onKeyPress("C") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(
                text = "BORRAR TODO",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun GiantOptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

