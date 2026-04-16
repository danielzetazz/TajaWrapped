package com.danieleivan.tajatracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

/**
 * Payload para la tabla `consumiciones` en Supabase.
 */
@Serializable
data class ConsumicionInsert(
    @SerialName("fecha_hora") val fechaHora: String = OffsetDateTime.now().toString(),
    @SerialName("lugar_nombre") val lugarNombre: String? = null,
    @SerialName("formato") val formato: String,
    @SerialName("alcohol_base") val alcoholBase: String,
    @SerialName("mezcla") val mezcla: String?,
    @SerialName("con_hielo") val conHielo: Boolean,
    @SerialName("precio_pagado") val precioPagado: Double,
    @SerialName("es_robado") val esRobado: Boolean,
    @SerialName("valor_estimado") val valorEstimado: Double?
)

