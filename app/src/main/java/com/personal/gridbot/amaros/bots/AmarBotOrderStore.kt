package com.personal.gridbot.amaros.bots

import android.content.Context

/** Persists the visual order of BOT 1..10 without changing bot identity or runtime data. */
class AmarBotOrderStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("amar_bot_ui_v1", Context.MODE_PRIVATE)

    fun load(existing: List<Int> = (1..10).toList()): List<Int> {
        val allowed = existing.distinct().filter { it in 1..10 }
        val saved = prefs.getString(KEY_ORDER, null)
            ?.split(',')
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.filter { it in allowed }
            ?.distinct()
            .orEmpty()
        return (saved + allowed).distinct().filter { it in allowed }
    }

    fun save(order: List<Int>) {
        val normalized = order.filter { it in 1..10 }.distinct()
        if (normalized.isNotEmpty()) prefs.edit().putString(KEY_ORDER, normalized.joinToString(",")).apply()
    }

    companion object { private const val KEY_ORDER = "bot_order" }
}
