package com.personal.gridbot.amaros.ai.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Private AI workspace. AI research, evidence, bot references and strategy drafts live here,
 * not in application/runtime files. The workspace is advisory and never grants broker authority.
 */
class AmarAiWorkspace(private val context: Context) {
    companion object {
        private const val PREFS = "amar_ai_private_workspace_v1"
        private const val RESEARCH = "research"
        private const val BOTS = "bots"
        private const val STRATEGIES = "strategies"
    }

    data class Entry(val id: String, val title: String, val body: String, val source: String, val createdAtMs: Long)

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun saveResearch(title: String, body: String, source: String): Entry =
        save(RESEARCH, title, body, source)

    fun saveBotReference(title: String, body: String, source: String): Entry =
        save(BOTS, title, body, source)

    fun saveStrategyDraft(title: String, body: String, source: String = "AMAR AI"): Entry =
        save(STRATEGIES, title, body, source)

    fun list(kind: String, limit: Int = 50): List<Entry> = read(kind).takeLast(limit.coerceAtLeast(1))

    fun clear(kind: String) {
        prefs.edit().remove(kind).apply()
    }

    private fun save(kind: String, title: String, body: String, source: String): Entry {
        val entry = Entry(
            id = "AI-${System.currentTimeMillis()}-${title.hashCode().toUInt().toString(16)}",
            title = title.trim(),
            body = body.trim(),
            source = source.trim(),
            createdAtMs = System.currentTimeMillis()
        )
        val all = read(kind).toMutableList().apply { add(entry) }.takeLast(500)
        val array = JSONArray()
        all.forEach { e ->
            array.put(JSONObject().put("id", e.id).put("title", e.title).put("body", e.body).put("source", e.source).put("createdAtMs", e.createdAtMs))
        }
        prefs.edit().putString(kind, array.toString()).apply()
        return entry
    }

    private fun read(kind: String): List<Entry> {
        val raw = prefs.getString(kind, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val o = array.optJSONObject(i) ?: continue
                    add(Entry(o.optString("id"), o.optString("title"), o.optString("body"), o.optString("source"), o.optLong("createdAtMs")))
                }
            }
        }.getOrDefault(emptyList())
    }
}
