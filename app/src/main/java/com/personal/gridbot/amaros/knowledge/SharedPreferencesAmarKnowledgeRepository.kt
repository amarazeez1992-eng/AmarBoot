package com.personal.gridbot.amaros.knowledge

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Durable Android-backed implementation of the B14 repository contract. */
class SharedPreferencesAmarKnowledgeRepository(
    context: Context,
    private val prefsName: String = "amar_knowledge_v1"
) : AmarKnowledgeRepository {
    private val prefs = context.applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val type = object : TypeToken<List<AmarKnowledgeItem>>() {}.type

    override suspend fun upsert(item: AmarKnowledgeItem) = synchronized(this) {
        val current = load().associateBy { it.id }.toMutableMap()
        current[item.id] = item
        save(current.values.toList())
    }

    override suspend fun findByTopic(topic: String, limit: Int): List<AmarKnowledgeItem> = synchronized(this) {
        load().filter { it.topic.equals(topic.trim(), ignoreCase = true) }
            .sortedByDescending { it.confidence }
            .take(limit.coerceIn(1, 500))
    }

    override suspend fun search(query: String, limit: Int): List<AmarKnowledgeItem> = synchronized(this) {
        val q = query.trim().lowercase()
        if (q.isBlank()) return@synchronized emptyList()
        load().mapNotNull { item ->
            val haystack = "${item.topic} ${item.title} ${item.content} ${item.tags.joinToString(" ")}".lowercase()
            if (!haystack.contains(q)) null else item to haystack.split(q).size - 1
        }.sortedWith(compareByDescending<Pair<AmarKnowledgeItem, Int>> { it.second }.thenByDescending { it.first.confidence })
            .take(limit.coerceIn(1, 500)).map { it.first }
    }

    private fun load(): List<AmarKnowledgeItem> =
        prefs.getString(KEY, null)?.let { gson.fromJson<List<AmarKnowledgeItem>>(it, type) } ?: emptyList()

    private fun save(items: List<AmarKnowledgeItem>) {
        prefs.edit().putString(KEY, gson.toJson(items, type)).apply()
    }

    private companion object { const val KEY = "items" }
}
