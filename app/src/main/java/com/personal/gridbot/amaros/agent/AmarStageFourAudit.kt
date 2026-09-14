package com.personal.gridbot.amaros.agent

import java.security.MessageDigest

/** Thread-safe append-only hash-chained audit ledger for Stage 4 decisions. */
class AmarStageFourAuditLedger {
    private val lock = Any()
    private val events = mutableListOf<AmarAuditEvent>()
    private var nextSequence = 1L
    private var lastHash = "GENESIS"

    fun append(timestampEpochMs: Long, actor: String, action: String, decision: String, reason: String): AmarAuditEvent = synchronized(lock) {
        require(timestampEpochMs >= 0L)
        require(actor.isNotBlank())
        require(action.isNotBlank())
        require(decision.isNotBlank())
        require(reason.isNotBlank())
        val material = listOf(nextSequence, timestampEpochMs, actor, action, decision, reason, lastHash).joinToString("|")
        val hash = sha256(material)
        val event = AmarAuditEvent(nextSequence, timestampEpochMs, actor, action, decision, reason, lastHash, hash)
        events += event
        nextSequence++
        lastHash = hash
        event
    }

    fun snapshot(): List<AmarAuditEvent> = synchronized(lock) { events.toList() }

    fun verifyIntegrity(): Boolean = synchronized(lock) {
        var previous = "GENESIS"
        var expectedSequence = 1L
        for (event in events) {
            if (event.sequence != expectedSequence || event.timestampEpochMs < 0L) return@synchronized false
            val material = listOf(event.sequence, event.timestampEpochMs, event.actor, event.action, event.decision, event.reason, previous).joinToString("|")
            if (event.previousHash != previous || event.hash != sha256(material)) return@synchronized false
            previous = event.hash
            expectedSequence++
        }
        previous == lastHash && expectedSequence == nextSequence
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
