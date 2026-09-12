package com.personal.gridbot.amaros.agent.memory

interface AmarMemoryRepository {
    fun save(entry: AmarMemoryEntry)
    fun update(entry: AmarMemoryEntry)
    fun get(id: String): AmarMemoryEntry?
    fun search(query: String, limit: Int = 20): List<AmarMemoryEntry>
    fun recent(limit: Int = 50): List<AmarMemoryEntry>
    fun delete(id: String): Boolean
}

class AmarInMemoryRepository : AmarMemoryRepository {
    private val records = linkedMapOf<String, AmarMemoryEntry>()

    override fun save(entry: AmarMemoryEntry) { records[entry.id] = entry }
    override fun update(entry: AmarMemoryEntry) { records[entry.id] = entry }
    override fun get(id: String): AmarMemoryEntry? = records[id]

    override fun search(query: String, limit: Int): List<AmarMemoryEntry> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return recent(limit)
        return records.values.asSequence()
            .filter { it.text.lowercase().contains(q) || it.tags.any { tag -> tag.lowercase().contains(q) } }
            .sortedByDescending { it.updatedAtEpochMs }
            .take(limit.coerceAtLeast(1))
            .toList()
    }

    override fun recent(limit: Int): List<AmarMemoryEntry> =
        records.values.sortedByDescending { it.updatedAtEpochMs }.take(limit.coerceAtLeast(1))

    override fun delete(id: String): Boolean = records.remove(id) != null
}
