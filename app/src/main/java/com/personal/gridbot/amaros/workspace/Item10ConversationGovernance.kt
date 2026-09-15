package com.personal.gridbot.amaros.workspace

/** Conversation lifecycle and provenance controls; archive is never promoted as authoritative knowledge. */
data class AmarConversationLink(val fromConversationId: String, val toConversationId: String, val topic: String)

data class AmarMemoryRevision(
    val memoryId: String,
    val previousId: String?,
    val updatedAtEpochMs: Long,
    val reason: String
)

data class AmarRetentionPolicy(val maxAgeDays: Int, val userControlled: Boolean = true)

class AmarConversationGovernance {
    private val links = linkedSetOf<AmarConversationLink>()
    private val revisions = mutableListOf<AmarMemoryRevision>()
    private var retention: AmarRetentionPolicy? = null

    fun link(fromConversationId: String, toConversationId: String, topic: String): Boolean {
        if (fromConversationId.isBlank() || toConversationId.isBlank() || fromConversationId == toConversationId || topic.isBlank()) return false
        return links.add(AmarConversationLink(fromConversationId, toConversationId, topic))
    }

    fun linked(conversationId: String): List<AmarConversationLink> =
        links.filter { it.fromConversationId == conversationId || it.toConversationId == conversationId }

    fun recordRevision(revision: AmarMemoryRevision): Boolean {
        if (revision.memoryId.isBlank() || revision.updatedAtEpochMs < 0L || revision.reason.isBlank()) return false
        revisions += revision
        return true
    }

    fun revisions(memoryId: String): List<AmarMemoryRevision> = revisions.filter { it.memoryId == memoryId }

    fun setRetention(policy: AmarRetentionPolicy): Boolean {
        if (!policy.userControlled || policy.maxAgeDays <= 0) return false
        retention = policy
        return true
    }

    fun retentionPolicy(): AmarRetentionPolicy? = retention
}
