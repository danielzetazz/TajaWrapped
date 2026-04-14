package com.danieleivan.tajatracker.data.repository

import com.danieleivan.tajatracker.data.model.ConsumicionInsert
import com.danieleivan.tajatracker.data.model.ConsumicionRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ConsumicionesRepository(
    private val supabaseClient: SupabaseClient
) {
    suspend fun insertConsumicion(consumicion: ConsumicionInsert): Result<Unit> = runCatching {
        supabaseClient.from("consumiciones").insert(consumicion)
        Unit
    }

    suspend fun getConsumiciones(): Result<List<ConsumicionRow>> = runCatching {
        val rawJson = supabaseClient
            .from("consumiciones")
            .select()
            .data

        Json.parseToJsonElement(rawJson)
            .jsonArray
            .map { item ->
                val obj = item.jsonObject
                ConsumicionRow(
                    id = obj.readLong("id"),
                    fechaHora = obj.readString("fecha_hora"),
                    formato = obj.readString("formato").orEmpty(),
                    alcoholBase = obj.readString("alcohol_base").orEmpty(),
                    mezcla = obj.readString("mezcla"),
                    conHielo = obj.readBoolean("con_hielo") ?: false,
                    precioPagado = obj.readDouble("precio_pagado"),
                    esRobado = obj.readBoolean("es_robado") ?: false,
                    valorEstimado = obj.readDouble("valor_estimado")
                )
            }
    }
}

private fun JsonObject.readString(key: String): String? {
    val value = this[key]?.jsonPrimitive?.content ?: return null
    return if (value == "null") null else value
}

private fun JsonObject.readLong(key: String): Long? = readString(key)?.toLongOrNull()

private fun JsonObject.readDouble(key: String): Double? = readString(key)?.toDoubleOrNull()

private fun JsonObject.readBoolean(key: String): Boolean? = readString(key)?.toBooleanStrictOrNull()

