package com.personal.gridbot.amaros.memory

/** Controlled memory service. Keeps memory access behind one contract for future Room/remote adapters. */
class AmarMemoryService(
    private val repository: AmarMemoryRepository = InMemoryAmarMemoryRepository()
) {
    suspend fun remember(
        namespace: String,
        key: String,
        value: String,
        nowEpochMs: Long,
        importance: Double = 0.0,
        tags: Set<String> = emptySet(),
        source: String = "runtime"
    ) {
        require(namespace.isNotBlank()) { "namespace must not be blank" }
        require(key.isNotBlank()) { "key must not be blank" }
        require(value.isNotBlank()) { "value must not be blank" }
        repository.put(
            AmarMemoryRecord(
                namespace = namespace.trim(),
                key = key.trim(),
                value = value,
                createdAtEpochMs = repository.get(namespace.trim(), key.trim())?.createdAtEpochMs ?: nowEpochMs,
                updatedAtEpochMs = nowEpochMs,
                importance = importance.coerceIn(0.0, 1.0),
                tags = tags.map(String::trim).filter(String::isNotBlank).toSet(),
                source = source.trim().ifBlank { "runtime" }
            )
        )
    }

    suspend fun recall(namespace: String, key: String): AmarMemoryRecord? =
        repository.get(namespace.trim(), key.trim())

    suspend fun recallRelevant(namespace: String, tags: Set<String> = emptySet(), limit: Int = 20): List<AmarMemoryRecord> =
        repository.query(namespace.trim(), tags.map(String::trim).filter(String::isNotBlank).toSet(), limit)
}
