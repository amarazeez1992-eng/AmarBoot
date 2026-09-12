package com.personal.gridbot.amaros.agent

/** Structured memory: no hidden autonomous execution state. */
class AmarMemoryStore {
    private val records = mutableListOf<MemoryRecord>()

    fun remember(record: MemoryRecord) { records += record }

    fun recent(limit: Int = 50): List<MemoryRecord> = records.takeLast(limit)

    fun clear() { records.clear() }
}

data class MemoryRecord(
    val id: String,
    val type: MemoryType,
    val text: String,
    val source: String = "user",
    val timestampEpochMs: Long = System.currentTimeMillis()
)

enum class MemoryType { CONVERSATION, FACT, STRATEGY_DRAFT, RESEARCH, DECISION, AUDIT }
