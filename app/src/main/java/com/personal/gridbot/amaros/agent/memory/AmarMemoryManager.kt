package com.personal.gridbot.amaros.agent.memory

/** User-controlled memory commands: explicit "احفظ هذا" becomes permanent memory. */
class AmarMemoryManager(private val repository: AmarMemoryRepository) {
    fun rememberExplicitly(text: String, type: AmarMemoryType = AmarMemoryType.FACT, tags: List<String> = emptyList()): AmarMemoryEntry {
        require(text.isNotBlank())
        val entry = AmarMemoryEntry(
            id = "mem_${System.currentTimeMillis()}_${text.hashCode()}",
            type = type,
            text = text.trim(),
            tags = tags.distinct(),
            source = AmarMemorySource.USER,
            permanent = true
        )
        repository.save(entry)
        return entry
    }

    fun recall(query: String, limit: Int = 20): List<AmarMemoryEntry> = repository.search(query, limit)

    fun recent(limit: Int = 50): List<AmarMemoryEntry> = repository.recent(limit)

    fun forget(id: String): Boolean = repository.delete(id)
}
