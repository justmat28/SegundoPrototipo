package com.jmaa.segundoprototipo

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseManager {
    val client = createSupabaseClient(
        supabaseUrl = "https://mighhshfkywnjjekugoj.supabase.co",
        supabaseKey = "sb_publishable_-Ko8JrY-XZFCF_n5rPQYEQ_w4LGHSF5"
    ) {
        install(Auth)
        install(Postgrest)
    }
}