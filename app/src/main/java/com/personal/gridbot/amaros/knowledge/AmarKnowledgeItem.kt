package com.personal.gridbot.amaros.knowledge

import java.util.UUID

/** Immutable knowledge item. B14 separates facts from runtime memory and trading decisions. */
data class AmarKnowledgeItem(
    val id: String = UUID.randomUUID().toString(),
    val topic: String,
    val title: String,
    val content: String,
    val source: String,
    val version: String = "1.0",
    val confidence: Double = 1.0,
    val tags: Set<String> = emptySet(),
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long = createdAtEpochMs
)

interface AmarKnowledgeRepository {
    suspend fun upsert(item: AmarKnowledgeItem)
    suspend fun findByTopic(topic: String, limit: Int = 50): List<AmarKnowledgeItem>
    suspend fun search(query: String, limit: Int = 50): List<AmarKnowledgeItem>
}

class InMemoryAmarKnowledgeRepository : AmarKnowledgeRepository {
    private val items = LinkedHashMap<String, AmarKnowledgeItem>()

    @Synchronized
    override suspend fun upsert(item: AmarKnowledgeItem) {
        items[item.id] = item
    }

    @Synchronized
    override suspend fun findByTopic(topic: String, limit: Int): List<AmarKnowledgeItem> =
        items.values.filter { it.topic.equals(topic.trim(), ignoreCase = true) }
            .sortedByDescending { it.confidence }
            .take(limit.coerceIn(1, 500))

    @Synchronized
    override suspend fun search(query: String, limit: Int): List<AmarKnowledgeItem> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return emptyList()
        return items.values
            .mapNotNull { item ->
                val haystack = "${item.topic} ${item.title} ${item.content} ${item.tags.joinToString(" ")}".lowercase()
                if (!haystack.contains(q)) null else item to relevance(haystack, q)
            }
            .sortedWith(compareByDescending<Pair<AmarKnowledgeItem, Int>> { it.second }.thenByDescending { it.first.confidence })
            .take(limit.coerceIn(1, 500))
            .map { it.first }
    }

    private fun relevance(text: String, query: String): Int =
        text.split(query).size - 1
}
