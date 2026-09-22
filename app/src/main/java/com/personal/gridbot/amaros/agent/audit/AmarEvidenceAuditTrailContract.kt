package com.personal.gridbot.amaros.agent.audit

interface AmarEvidenceAuditTrailContract {
    fun build(input: EvidenceAuditTrailInput): EvidenceAuditTrailResult
}
