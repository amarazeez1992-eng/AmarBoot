package com.personal.gridbot.amaros.knowledge

/** Controlled access layer for knowledge. Future persistent/indexed adapters can replace the repository. */
class AmarKnowledgeService(
    private val repository: AmarKnowledgeRepository = InMemoryAmarKnowledgeRepository()
) {
    suspend fun publish(
        topic: String,
        title: String,
        content: String,
        source: String,
        nowEpochMs: Long,
        version: String = "1.0",
        confidence: Double = 1.0,
        tags: Set<String> = emptySet()
    ) {
        require(topic.isNotBlank()) { "topic must not be blank" }
        require(title.isNotBlank()) { "title must not be blank" }
        require(content.isNotBlank()) { "content must not be blank" }
        require(source.isNotBlank()) { "source must not be blank" }
        repository.upsert(
            AmarKnowledgeItem(
                topic = topic.trim(),
                title = title.trim(),
                content = content,
                source = source.trim(),
                version = version.trim().ifBlank { "1.0" },
                confidence = confidence.coerceIn(0.0, 1.0),
                tags = tags.map(String::trim).filter(String::isNotBlank).toSet(),
                createdAtEpochMs = nowEpochMs,
                updatedAtEpochMs = nowEpochMs
            )
        )
    }

    suspend fun byTopic(topic: String, limit: Int = 20): List<AmarKnowledgeItem> =
        repository.findByTopic(topic, limit)

    suspend fun search(query: String, limit: Int = 20): List<AmarKnowledgeItem> =
        repository.search(query, limit)
}
