package com.danieleivan.tajatracker.data.repository

import com.danieleivan.tajatracker.data.model.ConsumicionInsert
import com.danieleivan.tajatracker.data.model.ConsumicionRow
import com.danieleivan.tajatracker.data.model.LugarInsert
import com.danieleivan.tajatracker.data.model.LugarRow
import com.danieleivan.tajatracker.data.model.RegistroInsert
import com.danieleivan.tajatracker.data.model.RegistroRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ConsumicionesRepository(
    private val supabaseClient: SupabaseClient
) {
    suspend fun insertRegistro(registro: RegistroInsert): Result<Unit> = runCatching {
        supabaseClient.from("registros").insert(registro)
        Unit
    }

    suspend fun insertConsumicion(consumicion: ConsumicionInsert): Result<Unit> = runCatching {
        supabaseClient.from("consumiciones").insert(consumicion)
        Unit
    }

    suspend fun getRegistros(): Result<List<RegistroRow>> = runCatching {
        val rawJson = supabaseClient
            .from("registros")
            .select()
            .data

        Json.parseToJsonElement(rawJson)
            .jsonArray
            .map { item ->
                val obj = item.jsonObject
                RegistroRow(
                    id = obj.readString("id").orEmpty(),
                    fechaHora = obj.readString("fecha_hora"),
                    lugarNombre = obj.readString("lugar_nombre").orEmpty(),
                    cubatasHidalgoTotal = obj.readInt("cubatas_hidalgo_total") ?: 0,
                    vomitosTotal = obj.readInt("vomitos_total") ?: 0
                )
            }
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
                    registroId = obj.readString("registro_id"),
                    lugarNombre = obj.readString("lugar_nombre"),
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

    suspend fun getLugares(): Result<List<LugarRow>> = runCatching {
        val rawJson = supabaseClient
            .from("lugares")
            .select()
            .data

        Json.parseToJsonElement(rawJson)
            .jsonArray
            .map { item ->
                val obj = item.jsonObject
                LugarRow(
                    id = obj.readString("id").orEmpty(),
                    nombre = obj.readString("nombre").orEmpty(),
                    nombreNormalizado = obj.readString("nombre_normalizado")
                )
            }
    }

    suspend fun insertLugar(lugar: LugarInsert): Result<Unit> = runCatching {
        supabaseClient.from("lugares").upsert(lugar) {
            onConflict = "usuario_id,nombre_normalizado"
            ignoreDuplicates = true
        }
        Unit
    }

    suspend fun deleteLugar(lugarId: String): Result<Unit> = runCatching {
        supabaseClient.postgrest.rpc(
            function = "delete_my_lugar",
            parameters = buildJsonObject {
                put("p_id", JsonPrimitive(lugarId))
            }
        )
        Unit
    }
}

private fun JsonObject.readString(key: String): String? {
    val value = this[key]?.jsonPrimitive?.content ?: return null
    return if (value == "null") null else value
}

private fun JsonObject.readLong(key: String): Long? = readString(key)?.toLongOrNull()

private fun JsonObject.readDouble(key: String): Double? = readString(key)?.toDoubleOrNull()

private fun JsonObject.readInt(key: String): Int? = readString(key)?.toIntOrNull()

private fun JsonObject.readBoolean(key: String): Boolean? = readString(key)?.toBooleanStrictOrNull()

