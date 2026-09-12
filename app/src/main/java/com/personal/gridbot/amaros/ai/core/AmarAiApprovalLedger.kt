package com.personal.gridbot.amaros.ai.core

import android.content.Context
import java.security.MessageDigest

/** Durable human-approval boundary for AI proposals. AI may create proposals, never approve them. */
class AmarAiApprovalLedger(context: Context) {
    enum class Status { PROPOSED, APPROVED, REJECTED, EXPIRED }

    data class Proposal(
        val id: String,
        val subject: String,
        val fingerprint: String,
        val status: Status,
        val createdAtMs: Long,
        val decidedAtMs: Long? = null,
        val decisionBy: String? = null
    )

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun propose(subject: String): Proposal {
        require(subject.isNotBlank()) { "Approval subject is required" }
        val now = System.currentTimeMillis()
        val fingerprint = sha256(subject.trim())
        val proposal = Proposal("AP-${now}-${fingerprint.take(12)}", subject.trim(), fingerprint, Status.PROPOSED, now)
        prefs.edit()
            .putString("${KEY_PREFIX}${proposal.id}", encode(proposal))
            .putString(KEY_LATEST, proposal.id)
            .commit()
        return proposal
    }

    fun approve(id: String, actor: String = "USER"): Proposal? = decide(id, Status.APPROVED, actor)
    fun reject(id: String, actor: String = "USER"): Proposal? = decide(id, Status.REJECTED, actor)

    fun latest(): Proposal? = prefs.getString(KEY_LATEST, null)?.let(::find)

    fun find(id: String): Proposal? = prefs.getString("$KEY_PREFIX$id", null)?.let(::decode)

    fun isApproved(id: String): Boolean = find(id)?.status == Status.APPROVED

    private fun decide(id: String, status: Status, actor: String): Proposal? {
        val current = find(id) ?: return null
        if (current.status != Status.PROPOSED) return current
        val decided = current.copy(status = status, decidedAtMs = System.currentTimeMillis(), decisionBy = actor)
        return if (prefs.edit().putString("$KEY_PREFIX$id", encode(decided)).commit()) decided else null
    }

    private fun encode(p: Proposal): String = listOf(
        p.id, p.subject.replace("|", " "), p.fingerprint, p.status.name,
        p.createdAtMs.toString(), p.decidedAtMs?.toString() ?: "", p.decisionBy.orEmpty()
    ).joinToString("|")

    private fun decode(raw: String): Proposal? = runCatching {
        val p = raw.split("|", limit = 7)
        Proposal(p[0], p[1], p[2], Status.valueOf(p[3]), p[4].toLong(), p[5].ifBlank { null }?.toLong(), p[6].ifBlank { null })
    }.getOrNull()

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }

    companion object {
        private const val PREFS = "amar_ai_approval_ledger_v1"
        private const val KEY_PREFIX = "proposal_"
        private const val KEY_LATEST = "latest"
    }
}
