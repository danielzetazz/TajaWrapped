package com.danieleivan.tajatracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DrunkWrappedColors = darkColorScheme(
    primary = GoldAccent,
    onPrimary = NightBackground,
    primaryContainer = Color(0xFF3B342C),
    onPrimaryContainer = SoftWhite,
    secondary = BronzeAccent,
    onSecondary = SoftWhite,
    secondaryContainer = Color(0xFF2A3348),
    onSecondaryContainer = MistText,
    tertiary = WoodAccent,
    onTertiary = SoftWhite,
    background = NightBackground,
    onBackground = SoftWhite,
    surface = NightSurface,
    onSurface = SoftWhite,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = MistText,
    outline = Color(0xFF6F6B76),
    error = DangerMuted,
    onError = SoftWhite,
    inverseSurface = SoftWhite,
    inverseOnSurface = NightBackground
)

@Composable
fun DrunkWrappedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DrunkWrappedColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

