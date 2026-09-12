package com.personal.gridbot.amaros.agent

/** Local-first trading knowledge registry. Content is versioned and provenance-aware. */
class AmarKnowledgeEngine {
    private val entries = mutableListOf<KnowledgeEntry>()

    fun register(entry: KnowledgeEntry) { entries.removeAll { it.id == entry.id }; entries += entry }

    fun search(query: String, limit: Int = 8): List<KnowledgeEntry> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return emptyList()
        return entries.asSequence()
            .filter { (it.title + " " + it.tags.joinToString(" ")).lowercase().contains(q) }
            .take(limit)
            .toList()
    }
}

data class KnowledgeEntry(
    val id: String,
    val title: String,
    val tags: Set<String>,
    val source: String,
    val license: String,
    val version: String = "1"
)
