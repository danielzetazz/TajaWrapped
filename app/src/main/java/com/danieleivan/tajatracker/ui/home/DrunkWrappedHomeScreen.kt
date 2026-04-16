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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

private enum class DrinkFormat(
    val label: String,
    val requiresAlcoholBase: Boolean,
    val requiresMixer: Boolean,
    val requiresIce: Boolean
) {
    COPA("Copa", true, true, true),
    CHUPITO("Chupito", true, false, false),
    CERVEZA("Cerveza", false, false, false),
    VINO("Vino", false, false, false),
    GARRAFA("Garrafa", true, true, true)
}

private enum class BaseAlcohol(val label: String) {
    WHISKY("Whisky"),
    RON("Ron"),
    JAGGERMEISTER("Jaggermeister"),
    GINEBRA("Ginebra"),
    VODKA("Vodka")
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
    onBackToMenu: () -> Unit = {},
    onOpenStats: () -> Unit = {}
) {
    var selectedFormat by remember { mutableStateOf<DrinkFormat?>(null) }
    var selectedQuantity by remember { mutableStateOf(1) }
    var selectedAlcohol by remember { mutableStateOf<BaseAlcohol?>(null) }
    var selectedMixer by remember { mutableStateOf<Mixer?>(null) }
    var withIce by remember { mutableStateOf<Boolean?>(null) }
    var priceInput by remember { mutableStateOf("0") }
    var placeInput by remember { mutableStateOf("") }
    var vomitosCount by remember { mutableStateOf(0) }
    var isRobbed by remember { mutableStateOf(false) }
    var lastSaved by remember { mutableStateOf<String?>(null) }
    var draftedDrinks by remember { mutableStateOf(emptyList<DrinkDraft>()) }
    var showSummaryDialog by remember { mutableStateOf(false) }
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState.isSuccess) {
        if (saveState.isSuccess) {
            draftedDrinks = emptyList()
            selectedFormat = null
            selectedQuantity = 1
            selectedAlcohol = null
            selectedMixer = null
            withIce = null
            priceInput = "0"
            placeInput = ""
            vomitosCount = 0
            isRobbed = false
            lastSaved = null
            showSummaryDialog = false
            viewModel.limpiarEstadoGuardado()
            onBackToMenu()
        }
    }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onBackToMenu,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 62.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = "MENU",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = onOpenStats,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 62.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(
                        text = "WRAPPED",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            SectionTitle("1) Elige formato")
            DrinkFormat.entries.forEach { format ->
                GiantOptionButton(
                    text = format.label,
                    selected = selectedFormat == format,
                    onClick = {
                        selectedFormat = format
                        selectedQuantity = 1
                        selectedAlcohol = null
                        selectedMixer = null
                        withIce = null
                    }
                )
            }

            if (selectedFormat != null) {
                SectionTitle("2) Cuantas consumiste?")
                QuantitySelector(
                    quantity = selectedQuantity,
                    onDecrease = {
                        if (selectedQuantity > 1) {
                            selectedQuantity -= 1
                        }
                    },
                    onIncrease = {
                        selectedQuantity += 1
                    }
                )
            }

            if (selectedFormat?.requiresAlcoholBase == true) {
                SectionTitle("Alcohol base")
                BaseAlcohol.entries.forEach { alcohol ->
                    GiantOptionButton(
                        text = alcohol.label,
                        selected = selectedAlcohol == alcohol,
                        onClick = { selectedAlcohol = alcohol }
                    )
                }
            }

            if (selectedFormat?.requiresMixer == true) {
                SectionTitle("Mezcla")
                Mixer.entries.forEach { mixer ->
                    GiantOptionButton(
                        text = mixer.label,
                        selected = selectedMixer == mixer,
                        onClick = { selectedMixer = mixer }
                    )
                }
            }

            if (selectedFormat?.requiresIce == true) {
                SectionTitle("Hielo")
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
                SectionTitle("Coste")
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
                        .heightIn(min = 72.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRobbed) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        contentColor = if (isRobbed) {
                            MaterialTheme.colorScheme.onError
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                ) {
                    Text(
                        text = if (isRobbed) "¡Robado! ACTIVADO" else "¡Robado!",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            val selected = selectedFormat
            val requiresAlcoholBase = selected?.requiresAlcoholBase == true
            val requiresMixer = selected?.requiresMixer == true
            val requiresIce = selected?.requiresIce == true
            val enteredCost = priceInput.toDoubleOrNull() ?: 0.0
            val hasCost = enteredCost > 0.0
            val priceIsOptional = isRobbed
            val canBuildDraft = selected != null &&
                selectedQuantity >= 1 &&
                (requiresAlcoholBase.not() || selectedAlcohol != null) &&
                (requiresMixer.not() || selectedMixer != null) &&
                (requiresIce.not() || withIce != null) &&
                (priceIsOptional || hasCost)

            val currentDraft = if (canBuildDraft) {
                DrinkDraft(
                    formato = selected.label,
                    alcoholBase = if (requiresAlcoholBase) selectedAlcohol?.label.orEmpty() else "",
                    mezcla = if (requiresMixer) selectedMixer?.label else null,
                    conHielo = if (requiresIce) withIce == true else false,
                    precioCapturado = enteredCost,
                    esRobado = isRobbed,
                    cantidad = selectedQuantity
                )
            } else {
                null
            }

            if (selectedFormat != null) {
                Button(
                    onClick = {
                        if (currentDraft != null) {
                            draftedDrinks = draftedDrinks + currentDraft
                            lastSaved = buildString {
                                append(currentDraft.formato)
                                append(" x")
                                append(currentDraft.cantidad)
                                if (currentDraft.alcoholBase.isNotBlank()) {
                                    append(" | ")
                                    append(currentDraft.alcoholBase)
                                }
                                if (currentDraft.mezcla != null) {
                                    append(" + ")
                                    append(currentDraft.mezcla)
                                }
                            }
                            selectedQuantity = 1
                            selectedAlcohol = null
                            selectedMixer = null
                            withIce = null
                            priceInput = "0"
                            isRobbed = false
                        }
                    },
                    enabled = currentDraft != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 68.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(
                        text = "AÑADIR AL REGISTRO",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (draftedDrinks.isNotEmpty()) {
                SectionTitle("Registro actual")
                draftedDrinks.forEachIndexed { index, draft ->
                    DraftSummaryCard(
                        draft = draft,
                        canAdjustHidalgo = isCubataFormat(draft.formato),
                        onIncreaseHidalgo = {
                            draftedDrinks = draftedDrinks.mapIndexed { mapIndex, item ->
                                if (mapIndex != index) item
                                else item.copy(
                                    hidalgoCount = (item.hidalgoCount + 1)
                                        .coerceAtMost(item.cantidad.coerceAtLeast(0))
                                )
                            }
                        },
                        onDecreaseHidalgo = {
                            draftedDrinks = draftedDrinks.mapIndexed { mapIndex, item ->
                                if (mapIndex != index) item
                                else item.copy(hidalgoCount = (item.hidalgoCount - 1).coerceAtLeast(0))
                            }
                        }
                    )
                }

                SectionTitle("Trucos especiales de la noche")
                Text(
                    text = "Cubatas Hidalgo totales: ${draftedDrinks.sumOf { it.hidalgoCount }}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GiantOptionButton(
                        text = "-",
                        selected = false,
                        onClick = { vomitosCount = (vomitosCount - 1).coerceAtLeast(0) },
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Vomitos: $vomitosCount",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(2f)
                    )
                    GiantOptionButton(
                        text = "+",
                        selected = false,
                        onClick = { vomitosCount += 1 },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = placeInput,
                    onValueChange = { placeInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lugar donde has bebido") },
                    placeholder = { Text("Bar, casa de un colega, discoteca, terraza...") },
                    singleLine = true
                )

                Button(
                    onClick = { showSummaryDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = "VER RESUMEN",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { viewModel.guardarRegistro(draftedDrinks, placeInput, vomitosCount) },
                    enabled = !saveState.isSaving && placeInput.trim().isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 68.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (saveState.isSaving) "GUARDANDO REGISTRO..." else "REGISTRAR DÍA",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (saveState.errorMessage != null) {
                Text(
                    text = "Error al guardar: ${saveState.errorMessage}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
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
                    text = "Ultima bebida añadida: $lastSaved",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (showSummaryDialog) {
                SummaryDialog(
                    draftedDrinks = draftedDrinks,
                    lugarNombre = placeInput,
                    vomitosTotal = vomitosCount,
                    canConfirm = placeInput.trim().isNotEmpty(),
                    onDismiss = { showSummaryDialog = false },
                    onConfirm = {
                        viewModel.guardarRegistro(draftedDrinks, placeInput, vomitosCount)
                        showSummaryDialog = false
                    }
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
private fun SummaryDialog(
    draftedDrinks: List<DrinkDraft>,
    lugarNombre: String,
    vomitosTotal: Int,
    canConfirm: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val totalUnits = draftedDrinks.sumOf { it.cantidad.coerceAtLeast(1) }
    val totalHidalgos = draftedDrinks.sumOf { it.hidalgoCount.coerceAtLeast(0) }
    val totalValue = draftedDrinks.sumOf { draft ->
        val unitValue = if (draft.esRobado) draft.precioCapturado else draft.precioCapturado
        unitValue * draft.cantidad.coerceAtLeast(1)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("REGISTRAR")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("SEGUIR EDITANDO")
            }
        },
        title = {
            Text(
                text = "Resumen del registro",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Total de consumiciones: $totalUnits",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Total estimado: ${formatMoney(totalValue)} EUR",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Lugar: ${lugarNombre.trim().ifBlank { "Sin especificar" }}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Cubatas Hidalgo: $totalHidalgos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Vomitos registrados: ${vomitosTotal.coerceAtLeast(0)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                draftedDrinks.forEach { draft ->
                    Text(
                        text = buildString {
                            append(draft.cantidad)
                            append(" x ")
                            append(draft.formato)
                            if (draft.alcoholBase.isNotBlank()) {
                                append(" | ")
                                append(draft.alcoholBase)
                            }
                            if (draft.mezcla != null) {
                                append(" + ")
                                append(draft.mezcla)
                            }
                            if (draft.conHielo) {
                                append(" | Hielo")
                            }
                            if (isCubataFormat(draft.formato)) {
                                append(" | Hidalgo: ")
                                append(draft.hidalgoCount)
                            }
                            append(" | ")
                            append(if (draft.esRobado) "Robo" else "Precio")
                            append(": ")
                            append(formatMoney(draft.precioCapturado))
                            append(" EUR")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Composable
private fun DraftSummaryCard(
    draft: DrinkDraft,
    canAdjustHidalgo: Boolean,
    onIncreaseHidalgo: () -> Unit,
    onDecreaseHidalgo: () -> Unit
) {
    val priceLabel = if (draft.esRobado) "Robo" else "Precio"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${draft.cantidad} x ${draft.formato}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            if (draft.alcoholBase.isNotBlank()) {
                Text(
                    text = draft.alcoholBase,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (draft.mezcla != null) {
                Text(
                    text = "Mezcla: ${draft.mezcla}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (draft.conHielo) {
                Text(
                    text = "Con hielo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$priceLabel: ${formatMoney(draft.precioCapturado)} EUR",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (canAdjustHidalgo) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDecreaseHidalgo,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("-")
                    }
                    Text(
                        text = "Hidalgo: ${draft.hidalgoCount}/${draft.cantidad}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(2f)
                    )
                    Button(
                        onClick = onIncreaseHidalgo,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("+")
                    }
                }
            }
        }
    }
}

private fun isCubataFormat(formato: String): Boolean {
    return formato.equals("Copa", ignoreCase = true) || formato.equals("Garrafa", ignoreCase = true)
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GiantOptionButton(
            text = "-",
            selected = false,
            onClick = onDecrease,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        GiantOptionButton(
            text = "+",
            selected = false,
            onClick = onIncrease,
            modifier = Modifier.weight(1f)
        )
    }
}

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
                            .heightIn(min = 72.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.titleMedium,
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
                .heightIn(min = 64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(
                text = "BORRAR TODO",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
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
            .heightIn(min = 66.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

