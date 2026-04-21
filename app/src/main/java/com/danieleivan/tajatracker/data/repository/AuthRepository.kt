package com.danieleivan.tajatracker.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthRepository(
    private val supabaseClient: SupabaseClient
) {
    fun hasActiveSession(): Boolean = supabaseClient.auth.currentSessionOrNull() != null

    suspend fun signInWithUsername(username: String, password: String): Result<Unit> = runCatching {
        val email = resolveEmailByUsername(username)
            ?: error("No existe ningún usuario con ese nombre")

        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUpWithUsername(email: String, username: String, password: String): Result<Unit> = runCatching {
        val normalizedUsername = username.trim().lowercase()

        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        // Si existe sesión inmediata (sin confirmación por email), persistimos perfil localmente.
        upsertUsuarioIfSessionExists(email.trim(), normalizedUsername)
    }

    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        supabaseClient.auth.resetPasswordForEmail(email)
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        supabaseClient.auth.signOut()
    }

    private suspend fun resolveEmailByUsername(username: String): String? {
        val normalizedUsername = username.trim().lowercase()
        if (normalizedUsername.isBlank()) return null

        val rawJson = supabaseClient
            .from("usuarios")
            .select()
            .data

        return Json.parseToJsonElement(rawJson)
            .jsonArray
            .asSequence()
            .map { it.jsonObject }
            .firstOrNull { row ->
                row.readString("username")
                    ?.trim()
                    ?.lowercase() == normalizedUsername
            }
            ?.readString("email")
    }

    private suspend fun upsertUsuarioIfSessionExists(email: String, username: String) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        supabaseClient.from("usuarios").upsert(
            UsuarioUpsertPayload(
                id = userId,
                email = email,
                username = username
            )
        )
    }
}

@Serializable
private data class UsuarioUpsertPayload(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String,
    @SerialName("username") val username: String
)

private fun kotlinx.serialization.json.JsonObject.readString(key: String): String? {
    val value = this[key]?.jsonPrimitive?.content ?: return null
    return if (value == "null") null else value
}

