package com.personal.gridbot.amaros.agent.audit

import java.util.Collections

data class EvidenceAuditTrailResult(
    val entries: List<EvidenceAuditEntry>,
    val isDownstreamReady: Boolean,
    val reason: AuditReason
) {
    companion object {
        fun immutable(
            entries: List<EvidenceAuditEntry>,
            isDownstreamReady: Boolean,
            reason: AuditReason
        ): EvidenceAuditTrailResult = EvidenceAuditTrailResult(
            entries = Collections.unmodifiableList(entries.toList()),
            isDownstreamReady = isDownstreamReady,
            reason = reason
        )
    }
}
