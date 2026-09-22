package com.personal.gridbot.amaros.agent.audit

import java.util.Collections

data class EvidenceAuditEntry(
    val auditId: String,
    val evidenceFingerprint: String,
    val eventType: AuditEventType,
    val timestamp: Long,
    val actor: String,
    val details: Map<String, String>
) {
    init {
        require(auditId.isNotBlank())
        require(evidenceFingerprint.isNotBlank())
        require(timestamp >= 0L)
        require(actor.isNotBlank())
    }

    companion object {
        fun immutable(
            auditId: String,
            evidenceFingerprint: String,
            eventType: AuditEventType,
            timestamp: Long,
            actor: String,
            details: Map<String, String>
        ): EvidenceAuditEntry = EvidenceAuditEntry(
            auditId = auditId,
            evidenceFingerprint = evidenceFingerprint,
            eventType = eventType,
            timestamp = timestamp,
            actor = actor,
            details = Collections.unmodifiableMap(details.toSortedMap())
        )
    }
}
