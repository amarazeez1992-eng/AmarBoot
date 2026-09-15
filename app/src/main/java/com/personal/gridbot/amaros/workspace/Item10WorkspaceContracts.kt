package com.personal.gridbot.amaros.workspace

/** Explicit web permission selected by the user. OPEN_SEARCH never bypasses security or OS permissions. */
enum class AmarWebMode { RESTRICTED_SEARCH, OPEN_SEARCH }

data class ConversationRecord(
    val id: String,
    val title: String,
    val topic: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val summary: String = "",
    val keywords: Set<String> = emptySet(),
    val content: String,
    val archived: Boolean = false,
    val pinned: Boolean = false
)

data class MemoryRecord(
    val id: String,
    val conversationId: String?,
    val text: String,
    val topic: String,
    val createdAtEpochMs: Long,
    val source: String,
    val validated: Boolean,
    val supersededBy: String? = null,
    val deleted: Boolean = false
)

data class KnowledgeRecord(
    val id: String,
    val text: String,
    val topic: String,
    val sourceIds: Set<String>,
    val validated: Boolean,
    val supersedesId: String? = null,
    val deleted: Boolean = false
)

data class EvidenceRecord(
    val id: String,
    val source: String,
    val statement: String,
    val independent: Boolean = true,
    val valid: Boolean = true
)

data class ReasoningResult(
    val answer: String,
    val evidence: List<EvidenceRecord>,
    val confidence: Double,
    val fabricated: Boolean = false,
    val blocked: Boolean = false,
    val blockers: List<String> = emptyList()
)

/**
 * Small deterministic policy/state authority for Item 10. Persistence adapters can implement
 * durable storage later without changing the contracts or permission semantics.
 */
class AmarConversationMemoryStore {
    private val conversations = linkedMapOf<String, ConversationRecord>()
    private val memories = linkedMapOf<String, MemoryRecord>()
    private val knowledge = linkedMapOf<String, KnowledgeRecord>()

    fun saveConversation(record: ConversationRecord) {
        conversations[record.id] = record
    }

    fun conversation(id: String): ConversationRecord? = conversations[id]

    fun searchConversations(query: String): List<ConversationRecord> {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return emptyList()
        return conversations.values.filter { record ->
            !record.archived && listOf(record.title, record.topic, record.summary, record.content)
                .any { it.lowercase().contains(normalized) || record.keywords.any { k -> k.lowercase().contains(normalized) } }
        }
    }

    fun saveMemory(record: MemoryRecord) {
        memories[record.id] = record.copy(deleted = false)
    }

    fun memory(id: String): MemoryRecord? = memories[id]?.takeUnless { it.deleted }

    /** Tombstone deletion prevents the deleted id from silently returning from this authority. */
    fun deleteMemory(id: String): Boolean {
        val current = memories[id] ?: return false
        memories[id] = current.copy(deleted = true)
        knowledge.entries.filter { it.value.sourceIds.contains(id) }.forEach { (key, value) ->
            knowledge[key] = value.copy(deleted = true)
        }
        return true
    }

    fun promoteToKnowledge(record: KnowledgeRecord): Boolean {
        if (!record.validated || record.sourceIds.isEmpty()) return false
        if (record.sourceIds.any { sourceId -> memories[sourceId]?.deleted == true }) return false
        knowledge[record.id] = record.copy(deleted = false)
        return true
    }

    fun knowledge(id: String): KnowledgeRecord? = knowledge[id]?.takeUnless { it.deleted }

    fun deleteKnowledge(id: String): Boolean {
        val current = knowledge[id] ?: return false
        knowledge[id] = current.copy(deleted = true)
        return true
    }
}

/** User-facing command semantics are explicit and side-effect scoped. */
class AmarMemoryCommandRouter(private val store: AmarConversationMemoryStore) {
    fun execute(command: String, memory: MemoryRecord? = null, memoryId: String? = null): Boolean {
        return when (command.trim().lowercase()) {
            "save", "save this", "احفظ هذا" -> memory?.let { store.saveMemory(it); true } ?: false
            "delete", "delete this", "احذف هذا" -> memoryId?.let { store.deleteMemory(it) } ?: false
            else -> false
        }
    }
}

/** Fail-closed result fusion boundary for multimodal/research/knowledge engines. */
class AmarEvidenceFusionEngine {
    fun fuse(answer: String, evidence: List<EvidenceRecord>, confidence: Double): ReasoningResult {
        val valid = evidence.filter { it.valid && it.statement.isNotBlank() && it.source.isNotBlank() }
        val blockers = buildList {
            if (answer.isBlank()) add("empty_answer")
            if (evidence.isNotEmpty() && valid.isEmpty()) add("no_valid_evidence")
            if (confidence !in 0.0..1.0) add("invalid_confidence")
        }
        return ReasoningResult(
            answer = answer,
            evidence = valid,
            confidence = confidence.coerceIn(0.0, 1.0),
            fabricated = false,
            blocked = blockers.isNotEmpty(),
            blockers = blockers
        )
    }
}

/** Sensitive device/app actions are denied until explicit permission and confirmation exist. */
data class AmarActionAuthorization(
    val permissionGranted: Boolean,
    val userConfirmed: Boolean,
    val sensitive: Boolean
)

class AmarActionSafetyGate {
    fun authorize(auth: AmarActionAuthorization): Boolean =
        auth.permissionGranted && (!auth.sensitive || auth.userConfirmed)
}
