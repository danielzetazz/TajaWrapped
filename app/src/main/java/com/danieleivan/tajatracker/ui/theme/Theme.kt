package com.danieleivan.tajatracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DrunkWrappedColors = darkColorScheme(
    primary = AppYellow,
    onPrimary = AppBlack,
    secondary = AppBlue,
    onSecondary = AppBlack,
    background = AppBlack,
    onBackground = AppWhite,
    surface = AppSurface,
    onSurface = AppWhite,
    tertiary = AppWhite,
    onTertiary = AppBlack
)

@Composable
fun DrunkWrappedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DrunkWrappedColors,
        typography = AppTypography,
        content = content
    )
}

