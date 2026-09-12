package com.personal.gridbot.amaros.agent.memory

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Android persistence adapter. Memory survives process restarts and app launches. */
class AmarPersistentMemoryRepository(context: Context) : AmarMemoryRepository {
    private val prefs = context.applicationContext.getSharedPreferences("amar_ai_memory", Context.MODE_PRIVATE)
    private val key = "records"

    override fun save(entry: AmarMemoryEntry) = write(entry)
    override fun update(entry: AmarMemoryEntry) = write(entry)

    override fun get(id: String): AmarMemoryEntry? = readAll().firstOrNull { it.id == id }

    override fun search(query: String, limit: Int): List<AmarMemoryEntry> {
        val q = query.trim().lowercase()
        return readAll()
            .filter { q.isEmpty() || it.text.lowercase().contains(q) || it.tags.any { tag -> tag.lowercase().contains(q) } }
            .sortedByDescending { it.updatedAtEpochMs }
            .take(limit.coerceAtLeast(1))
    }

    override fun recent(limit: Int): List<AmarMemoryEntry> =
        readAll().sortedByDescending { it.updatedAtEpochMs }.take(limit.coerceAtLeast(1))

    override fun delete(id: String): Boolean {
        val all = readAll()
        val changed = all.removeIf { it.id == id }
        if (changed) prefs.edit().putString(key, encode(all)).apply()
        return changed
    }

    private fun write(entry: AmarMemoryEntry) {
        val all = readAll().filterNot { it.id == entry.id }.toMutableList()
        all += entry
        prefs.edit().putString(key, encode(all)).apply()
    }

    private fun readAll(): MutableList<AmarMemoryEntry> {
        val raw = prefs.getString(key, "[]") ?: "[]"
        val array = JSONArray(raw)
        val result = mutableListOf<AmarMemoryEntry>()
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            val tagsArray = o.optJSONArray("tags") ?: JSONArray()
            val tags = buildList { for (j in 0 until tagsArray.length()) add(tagsArray.optString(j)) }
            result += AmarMemoryEntry(
                id = o.getString("id"),
                type = AmarMemoryType.valueOf(o.getString("type")),
                text = o.getString("text"),
                tags = tags,
                source = AmarMemorySource.valueOf(o.optString("source", AmarMemorySource.USER.name)),
                createdAtEpochMs = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAtEpochMs = o.optLong("updatedAt", System.currentTimeMillis()),
                permanent = o.optBoolean("permanent", true)
            )
        }
        return result
    }

    private fun encode(entries: List<AmarMemoryEntry>): String = JSONArray().apply {
        entries.forEach { e ->
            put(JSONObject().apply {
                put("id", e.id)
                put("type", e.type.name)
                put("text", e.text)
                put("tags", JSONArray(e.tags))
                put("source", e.source.name)
                put("createdAt", e.createdAtEpochMs)
                put("updatedAt", e.updatedAtEpochMs)
                put("permanent", e.permanent)
            })
        }
    }.toString()
}
