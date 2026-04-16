package com.danieleivan.tajatracker.ui.home

data class DrinkDraft(
    val formato: String,
    val alcoholBase: String,
    val mezcla: String?,
    val conHielo: Boolean,
    val precioCapturado: Double,
    val esRobado: Boolean,
    val cantidad: Int,
    val hidalgoCount: Int = 0
)

