package com.danieleivan.tajatracker.data.remote

import com.danieleivan.tajatracker.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseProvider {
    val client by lazy {
        check(BuildConfig.SUPABASE_URL.isNotBlank()) {
            "Falta SUPABASE_URL en local.properties"
        }
        check(BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            "Falta SUPABASE_ANON_KEY en local.properties"
        }

        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}

