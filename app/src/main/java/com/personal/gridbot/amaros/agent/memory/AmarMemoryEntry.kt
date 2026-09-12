package com.personal.gridbot.amaros.agent.memory

/** A durable, explicit memory item. Explicit user saves are never treated as temporary context. */
data class AmarMemoryEntry(
    val id: String,
    val type: AmarMemoryType,
    val text: String,
    val tags: List<String> = emptyList(),
    val source: AmarMemorySource = AmarMemorySource.USER,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = createdAtEpochMs,
    val permanent: Boolean = true
)

enum class AmarMemoryType { FACT, PREFERENCE, PROJECT, STRATEGY, RESEARCH, DECISION, CONVERSATION, AUDIT }
enum class AmarMemorySource { USER, RESEARCH, SYSTEM, AUDIT }
