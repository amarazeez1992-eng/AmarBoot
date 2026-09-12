package com.personal.gridbot.amaros.ai

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Independent AI strategy notebook; does not replace the Bot Vault. */
class AmarAiStrategyNotesRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    data class Note(
        val name: String,
        val version: Int,
        val content: String,
        val status: String = "DRAFT",
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )

    fun list(): List<Note> = runCatching {
        val array = JSONArray(prefs.getString(KEY_NOTES, "[]"))
        buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(Note(
                    name = o.optString("name"),
                    version = o.optInt("version", 1),
                    content = o.optString("content"),
                    status = o.optString("status", "DRAFT"),
                    createdAt = o.optLong("createdAt"),
                    updatedAt = o.optLong("updatedAt")
                ))
            }
        }.sortedBy { it.name }
    }.getOrDefault(emptyList())

    fun save(name: String, content: String, status: String = "APPROVED"): Note? {
        val cleanName = name.trim()
        val cleanContent = content.trim()
        if (cleanName.isBlank() || cleanContent.isBlank()) return null
        val now = System.currentTimeMillis()
        val current = list().firstOrNull { it.name.equals(cleanName, ignoreCase = true) }
        val note = Note(cleanName, (current?.version ?: 0) + 1, cleanContent, status, current?.createdAt ?: now, now)
        val remaining = list().filterNot { it.name.equals(cleanName, ignoreCase = true) }
        val array = JSONArray()
        (remaining + note).forEach { n ->
            array.put(JSONObject()
                .put("name", n.name)
                .put("version", n.version)
                .put("content", n.content)
                .put("status", n.status)
                .put("createdAt", n.createdAt)
                .put("updatedAt", n.updatedAt))
        }
        return if (prefs.edit().putString(KEY_NOTES, array.toString()).commit()) note else null
    }

    fun find(name: String): Note? = list().firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    fun delete(name: String): Boolean {
        val remaining = list().filterNot { it.name.equals(name.trim(), ignoreCase = true) }
        return prefs.edit().putString(KEY_NOTES, JSONArray().apply {
            remaining.forEach { n -> put(JSONObject().put("name", n.name).put("version", n.version).put("content", n.content).put("status", n.status).put("createdAt", n.createdAt).put("updatedAt", n.updatedAt)) }
        }.toString()).commit()
    }

    companion object {
        private const val PREFS = "amar_ai_strategy_notes_v1"
        private const val KEY_NOTES = "notes"
    }
}
