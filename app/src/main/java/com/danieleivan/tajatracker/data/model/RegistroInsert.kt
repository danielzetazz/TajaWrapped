package com.danieleivan.tajatracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistroInsert(
    @SerialName("id") val id: String,
    @SerialName("fecha_hora") val fechaHora: String,
    @SerialName("lugar_nombre") val lugarNombre: String,
    @SerialName("cubatas_hidalgo_total") val cubatasHidalgoTotal: Int,
    @SerialName("vomitos_total") val vomitosTotal: Int
)

