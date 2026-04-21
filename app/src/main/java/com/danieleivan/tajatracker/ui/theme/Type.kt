package com.danieleivan.tajatracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val SpeakeasySerif = FontFamily.Serif
private val SpeakeasySans = FontFamily.SansSerif

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = SpeakeasySerif,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SpeakeasySerif,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SpeakeasySans,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = TextStyle(
        fontFamily = SpeakeasySans,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontFamily = SpeakeasySans,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontFamily = SpeakeasySans,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontFamily = SpeakeasySans,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.15.sp
    )
)

