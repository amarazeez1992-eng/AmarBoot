package com.personal.gridbot.amaros.agent

import java.security.MessageDigest

/** Append-only hash-chained audit ledger for Stage 4 decisions. */
class AmarStageFourAuditLedger {
    private val events = mutableListOf<AmarAuditEvent>()
    private var nextSequence = 1L
    private var lastHash = "GENESIS"

    fun append(timestampEpochMs: Long, actor: String, action: String, decision: String, reason: String): AmarAuditEvent {
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
        return event
    }

    fun snapshot(): List<AmarAuditEvent> = events.toList()

    fun verifyIntegrity(): Boolean {
        var previous = "GENESIS"
        for (event in events) {
            val material = listOf(event.sequence, event.timestampEpochMs, event.actor, event.action, event.decision, event.reason, previous).joinToString("|")
            if (event.previousHash != previous || event.hash != sha256(material)) return false
            previous = event.hash
        }
        return true
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
