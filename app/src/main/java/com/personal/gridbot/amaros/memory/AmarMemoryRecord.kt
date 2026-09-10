package com.personal.gridbot.amaros.memory

import java.util.UUID

/** Immutable memory record. B13 foundation is storage-agnostic and DEMO-safe. */
data class AmarMemoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val namespace: String,
    val key: String,
    val value: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long = createdAtEpochMs,
    val importance: Double = 0.0,
    val tags: Set<String> = emptySet(),
    val source: String = "runtime"
)

interface AmarMemoryRepository {
    suspend fun put(record: AmarMemoryRecord)
    suspend fun get(namespace: String, key: String): AmarMemoryRecord?
    suspend fun query(namespace: String, tags: Set<String> = emptySet(), limit: Int = 50): List<AmarMemoryRecord>
    suspend fun delete(namespace: String, key: String): Boolean
    suspend fun clear(namespace: String? = null)
}

class InMemoryAmarMemoryRepository : AmarMemoryRepository {
    private val records = LinkedHashMap<String, AmarMemoryRecord>()

    override suspend fun put(record: AmarMemoryRecord) {
        synchronized(this) {
            records[compositeKey(record.namespace, record.key)] = record
        }
    }

    override suspend fun get(namespace: String, key: String): AmarMemoryRecord? =
        synchronized(this) {
            records[compositeKey(namespace, key)]
        }

    override suspend fun query(namespace: String, tags: Set<String>, limit: Int): List<AmarMemoryRecord> =
        synchronized(this) {
            val safeLimit = limit.coerceIn(1, 500)
            records.values
                .asSequence()
                .filter { it.namespace == namespace }
                .filter { tags.isEmpty() || tags.all(it.tags::contains) }
                .sortedByDescending { it.importance }
                .take(safeLimit)
                .toList()
        }

    override suspend fun delete(namespace: String, key: String): Boolean =
        synchronized(this) {
            records.remove(compositeKey(namespace, key)) != null
        }

    override suspend fun clear(namespace: String?) {
        synchronized(this) {
            if (namespace == null) records.clear()
            else records.keys.removeIf { it.startsWith("$namespace::") }
        }
    }

    private fun compositeKey(namespace: String, key: String) = "$namespace::$key"
}
