package com.jmaa.segundoprototipo

import android.content.Context
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object OfflineMermaManager {
    private const val PREFS_NAME = "merma_offline_prefs"
    private const val KEY_PENDING_MERMAS = "pending_mermas"

    fun saveOffline(context: Context, merma: Merma) {
        val pending = getPendingMermas(context).toMutableList()
        pending.add(merma)
        val jsonString = Json.encodeToString(pending)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_MERMAS, jsonString)
            .apply()
    }

    fun getPendingMermas(context: Context): List<Merma> {
        val jsonString = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PENDING_MERMAS, null) ?: return emptyList()
        return try {
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun syncPendingMermas(context: Context): Int {
        val pending = getPendingMermas(context)
        if (pending.isEmpty()) return 0

        var syncedCount = 0
        val remaining = mutableListOf<Merma>()

        for (merma in pending) {
            try {
                SupabaseManager.client.from("mermas").insert(merma)
                syncedCount++
            } catch (e: Exception) {
                remaining.add(merma)
            }
        }

        val jsonString = Json.encodeToString(remaining)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_MERMAS, jsonString)
            .apply()

        return syncedCount
    }
}