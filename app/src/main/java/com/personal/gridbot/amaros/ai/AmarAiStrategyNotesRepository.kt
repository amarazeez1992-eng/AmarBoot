package com.personal.gridbot.amaros.ai

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Independent AI strategy notebook; drafts are never silently promoted to approved state. */
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

    fun save(name: String, content: String, status: String = "DRAFT"): Note? {
        val cleanName = name.trim()
        val cleanContent = content.trim()
        val cleanStatus = normalizeStatus(status)
        if (cleanName.isBlank() || cleanContent.isBlank()) return null
        if (cleanStatus == "APPROVED") return null
        return persist(cleanName, cleanContent, cleanStatus)
    }

    /** Explicit human-approval boundary. AI code must not call this implicitly. */
    fun approve(name: String): Note? {
        val current = find(name) ?: return null
        if (current.status == "REJECTED" || current.status == "RETIRED") return null
        return persist(current.name, current.content, "APPROVED", current.createdAt)
    }

    fun reject(name: String): Note? {
        val current = find(name) ?: return null
        return persist(current.name, current.content, "REJECTED", current.createdAt)
    }

    fun retire(name: String): Note? {
        val current = find(name) ?: return null
        return persist(current.name, current.content, "RETIRED", current.createdAt)
    }

    fun find(name: String): Note? = list().firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    fun delete(name: String): Boolean {
        val current = find(name) ?: return false
        if (current.status == "APPROVED") return false
        val remaining = list().filterNot { it.name.equals(name.trim(), ignoreCase = true) }
        return prefs.edit().putString(KEY_NOTES, encode(remaining)).commit()
    }

    private fun persist(name: String, content: String, status: String, createdAt: Long = System.currentTimeMillis()): Note? {
        val now = System.currentTimeMillis()
        val current = list().firstOrNull { it.name.equals(name, ignoreCase = true) }
        val note = Note(name, (current?.version ?: 0) + 1, content, status, current?.createdAt ?: createdAt, now)
        val remaining = list().filterNot { it.name.equals(name, ignoreCase = true) }
        return if (prefs.edit().putString(KEY_NOTES, encode(remaining + note)).commit()) note else null
    }

    private fun encode(notes: List<Note>): String = JSONArray().apply {
        notes.forEach { n ->
            put(JSONObject()
                .put("name", n.name)
                .put("version", n.version)
                .put("content", n.content)
                .put("status", n.status)
                .put("createdAt", n.createdAt)
                .put("updatedAt", n.updatedAt))
        }
    }.toString()

    private fun normalizeStatus(status: String): String = when (status.trim().uppercase()) {
        "DRAFT", "CHALLENGER", "REVIEW", "REJECTED", "RETIRED" -> status.trim().uppercase()
        else -> "DRAFT"
    }

    companion object {
        private const val PREFS = "amar_ai_strategy_notes_v1"
        private const val KEY_NOTES = "notes"
    }
}
