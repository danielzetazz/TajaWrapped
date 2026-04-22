package com.danieleivan.tajatracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LugarInsert(
    @SerialName("nombre") val nombre: String,
    @SerialName("nombre_normalizado") val nombreNormalizado: String
)

