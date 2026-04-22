@file:Suppress("UnusedImport")

package com.danieleivan.tajatracker.ui.home

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    confirmBeforeRegister: Boolean = true,
    onRegistroGuardado: () -> Unit = {},
    onBackToMenu: () -> Unit = {},
    onOpenStats: () -> Unit = {}
) {
    val context = LocalContext.current

    var selectedFormat by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<DrinkFormat?>(null) }
    var selectedQuantity by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(1) }
    var selectedAlcohol by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<BaseAlcohol?>(null) }
    var selectedMixer by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Mixer?>(null) }
    var withIce by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Boolean?>(null) }
    var priceInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("0") }
    var selectedPlaceName by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var newPlaceInput by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var showPlacesManagerDialog by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var selectedPhotoPath by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var photoInfoMessage by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var selectedRegisterDate by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(LocalDate.now().toString()) }
    var vomitosCount by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    var isRobbed by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var lastSaved by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var draftedDrinks by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(emptyList<DrinkDraft>()) }
    var showSummaryDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showRegisterConfirmationDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val saveState by viewModel.saveState.collectAsState()
    val placesState by viewModel.placesState.collectAsState()

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val path = uri?.let { RegistroPhotoStorage.copyFromUri(context, it) }
        if (path != null) {
            selectedPhotoPath = path
            photoInfoMessage = "Foto añadida al registro"
        } else {
            photoInfoMessage = "No se pudo cargar la foto"
        }
    }

    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        val path = bitmap?.let { RegistroPhotoStorage.saveBitmap(context, it) }
        if (path != null) {
            selectedPhotoPath = path
            photoInfoMessage = "Foto capturada"
        } else {
            photoInfoMessage = "No se pudo capturar la foto"
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickPhotoLauncher.launch("image/*")
        } else {
            photoInfoMessage = "Permiso de galería denegado"
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraPreviewLauncher.launch(null)
        } else {
            photoInfoMessage = "Permiso de cámara denegado"
        }
    }

    fun openGalleryPicker() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                pickPhotoLauncher.launch("image/*")
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                pickPhotoLauncher.launch("image/*")
            }
            else -> {
                galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    fun openCameraCapture() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(placesState.places) {
        if (selectedPlaceName.isBlank() && placesState.places.isNotEmpty()) {
            selectedPlaceName = placesState.places.first().nombre
        }
    }

    LaunchedEffect(saveState.isSuccess) {
        if (saveState.isSuccess) {
            onRegistroGuardado()
            draftedDrinks = emptyList()
            selectedFormat = null
            selectedQuantity = 1
            selectedAlcohol = null
            selectedMixer = null
            withIce = null
            priceInput = "0"
            selectedPlaceName = ""
            newPlaceInput = ""
            selectedPhotoPath = null
            photoInfoMessage = null
            selectedRegisterDate = LocalDate.now().toString()
            vomitosCount = 0
            isRobbed = false
            lastSaved = null
            showSummaryDialog = false
            showRegisterConfirmationDialog = false
            showPlacesManagerDialog = false
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

                SectionTitle("Día del registro")
                OutlinedTextField(
                    value = selectedRegisterDate,
                    onValueChange = { selectedRegisterDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Fecha") },
                    placeholder = { Text("yyyy-MM-dd") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Ascii
                    )
                )
                Text(
                    text = "Formato recomendado: 2026-04-21",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SectionTitle("Lugar del registro")
                PlaceSelector(
                    places = placesState.places,
                    selectedPlaceName = selectedPlaceName,
                    onPlaceSelected = { selectedPlaceName = it },
                    onManagePlaces = { showPlacesManagerDialog = true }
                )

                SectionTitle("Foto de recuerdo")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { openGalleryPicker() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text("GALERIA", textAlign = TextAlign.Center)
                    }
                    Button(
                        onClick = { openCameraCapture() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("CAMARA", textAlign = TextAlign.Center)
                    }
                }

                if (selectedPhotoPath != null) {
                    PhotoPreviewCard(
                        photoPath = selectedPhotoPath.orEmpty(),
                        onRemove = {
                            selectedPhotoPath = null
                            photoInfoMessage = "Foto eliminada del borrador"
                        }
                    )
                }

                if (photoInfoMessage != null) {
                    Text(
                        text = photoInfoMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                        canAdjustHidalgo = isHidalgoEligibleFormat(draft.formato),
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
                    text = "Hidalgos totales: ${draftedDrinks.sumOf { it.hidalgoCount }}",
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
                    onClick = {
                        if (confirmBeforeRegister) {
                            showRegisterConfirmationDialog = true
                        } else {
                            viewModel.guardarRegistro(
                                draftedDrinks,
                                selectedPlaceName,
                                vomitosCount,
                                selectedPhotoPath,
                                selectedRegisterDate
                            )
                        }
                    },
                    enabled = !saveState.isSaving && selectedPlaceName.trim().isNotEmpty() && isValidDateInput(selectedRegisterDate),
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

            if (showRegisterConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showRegisterConfirmationDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                showRegisterConfirmationDialog = false
                                viewModel.guardarRegistro(
                                    draftedDrinks,
                                    selectedPlaceName,
                                    vomitosCount,
                                    selectedPhotoPath,
                                    selectedRegisterDate
                                )
                            }
                        ) {
                            Text("CONFIRMAR")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showRegisterConfirmationDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("CANCELAR")
                        }
                    },
                    title = { Text("Confirmar registro") },
                    text = {
                        Text("¿Estás seguro de que quieres confirmar el registro del día?")
                    }
                )
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
                    lugarNombre = selectedPlaceName,
                    fotoUri = selectedPhotoPath,
                    fechaRegistro = selectedRegisterDate,
                    vomitosTotal = vomitosCount,
                    canConfirm = selectedPlaceName.trim().isNotEmpty() && isValidDateInput(selectedRegisterDate),
                    onDismiss = { showSummaryDialog = false },
                    onConfirm = {
                        viewModel.guardarRegistro(
                            draftedDrinks,
                            selectedPlaceName,
                            vomitosCount,
                            selectedPhotoPath,
                            selectedRegisterDate
                        )
                        showSummaryDialog = false
                    }
                )
            }

            if (showPlacesManagerDialog) {
                PlacesManagerDialog(
                    placesState = placesState,
                    newPlaceInput = newPlaceInput,
                    onNewPlaceInputChange = { newPlaceInput = it },
                    onAddPlace = { placeName ->
                        viewModel.addPlace(placeName) { canonicalName ->
                            selectedPlaceName = canonicalName
                            newPlaceInput = ""
                        }
                    },
                    onDeletePlace = { place ->
                        viewModel.deletePlace(place.id) {
                            if (selectedPlaceName == place.nombre) {
                                selectedPlaceName = placesState.places.firstOrNull { it.id != place.id }?.nombre.orEmpty()
                            }
                        }
                    },
                    onDismiss = { showPlacesManagerDialog = false }
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
    fotoUri: String?,
    fechaRegistro: String,
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
                    text = "Fecha: ${formatRegistroDateLabel(fechaRegistro)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (fotoUri.isNullOrBlank()) "Foto de recuerdo: no añadida" else "Foto de recuerdo: añadida",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            if (isHidalgoEligibleFormat(draft.formato)) {
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

private fun isValidDateInput(value: String): Boolean {
    val input = value.trim()
    if (input.isBlank()) return false
    return runCatching { LocalDate.parse(input) }.isSuccess ||
        runCatching { OffsetDateTime.parse(input) }.isSuccess
}

private fun formatRegistroDateLabel(value: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return runCatching { LocalDate.parse(value.trim()).format(formatter) }
        .recoverCatching { OffsetDateTime.parse(value.trim()).toLocalDate().format(formatter) }
        .getOrDefault(value.ifBlank { "Sin fecha" })
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

private fun isHidalgoEligibleFormat(formato: String): Boolean {
    return formato.equals("Copa", ignoreCase = true) ||
        formato.equals("Garrafa", ignoreCase = true) ||
        formato.equals("Cerveza", ignoreCase = true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceSelector(
    places: List<com.danieleivan.tajatracker.data.model.LugarRow>,
    selectedPlaceName: String,
    onPlaceSelected: (String) -> Unit,
    onManagePlaces: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedPlaceName.ifBlank { "" },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                label = { Text("Lugar") },
                placeholder = { Text("Selecciona un lugar") },
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )

            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (places.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay lugares guardados") },
                        onClick = {
                            expanded = false
                            onManagePlaces()
                        }
                    )
                } else {
                    places.forEach { place ->
                        DropdownMenuItem(
                            text = { Text(place.nombre) },
                            onClick = {
                                expanded = false
                                onPlaceSelected(place.nombre)
                            }
                        )
                    }
                }
            }
        }

        TextButton(onClick = onManagePlaces) {
            Text("Añadir o eliminar lugares")
        }
    }
}

@Composable
private fun PlacesManagerDialog(
    placesState: PlacesUiState,
    newPlaceInput: String,
    onNewPlaceInputChange: (String) -> Unit,
    onAddPlace: (String) -> Unit,
    onDeletePlace: (com.danieleivan.tajatracker.data.model.LugarRow) -> Unit,
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
            Text(
                text = "Gestionar lugares",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Usa nombres canónicos para evitar duplicados como 'El comedia' y 'Sala la comedia'.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = newPlaceInput,
                    onValueChange = onNewPlaceInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nuevo lugar") },
                    placeholder = { Text("Ej. El Comedia") },
                    singleLine = true
                )

                Button(
                    onClick = { onAddPlace(newPlaceInput) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newPlaceInput.trim().isNotBlank() && !placesState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("AÑADIR LUGAR")
                }

                if (placesState.errorMessage != null) {
                    Text(
                        text = placesState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (placesState.infoMessage != null) {
                    Text(
                        text = placesState.infoMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (placesState.places.isEmpty()) {
                    Text(
                        text = "Todavia no hay lugares guardados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .heightIn(max = 280.dp)
                            .fillMaxWidth()
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(placesState.places) { place ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = androidx.compose.material3.CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = place.nombre,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = { onDeletePlace(place) }) {
                                            Text("Eliminar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun PhotoPreviewCard(
    photoPath: String,
    onRemove: () -> Unit
) {
    val bitmap = androidx.compose.runtime.remember(photoPath) { loadLocalBitmap(photoPath) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Tu foto de recuerdo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto de recuerdo seleccionada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Text(
                    text = "La foto se guardó, pero no se pudo previsualizar en este dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onRemove) {
                Text("Quitar foto")
            }
        }
    }
}

private fun loadLocalBitmap(photoPath: String): android.graphics.Bitmap? {
    if (photoPath.isBlank()) return null
    val file = File(photoPath)
    if (!file.exists()) return null
    return runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
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

