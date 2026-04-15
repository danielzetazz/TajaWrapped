package com.danieleivan.tajatracker.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository(
    private val supabaseClient: SupabaseClient
) {
    fun hasActiveSession(): Boolean = supabaseClient.auth.currentSessionOrNull() != null

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        supabaseClient.auth.resetPasswordForEmail(email)
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        supabaseClient.auth.signOut()
    }
}

