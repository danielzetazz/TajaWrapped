package com.danieleivan.tajatracker.data.model

data class ConsumicionRow(
    val id: Long? = null,
    val fechaHora: String? = null,
    val formato: String = "",
    val alcoholBase: String = "",
    val mezcla: String? = null,
    val conHielo: Boolean = false,
    val precioPagado: Double? = null,
    val esRobado: Boolean = false,
    val valorEstimado: Double? = null
)
