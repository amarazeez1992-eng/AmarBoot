package com.personal.gridbot.amaros.workspace

/** Audit records are append-only at this contract boundary; sensitive actions must remain traceable. */
data class AmarWorkspaceAuditEvent(
    val id: String,
    val action: String,
    val actor: String,
    val timestampEpochMs: Long,
    val allowed: Boolean,
    val reason: String
)

class AmarWorkspaceAuditLog {
    private val events = mutableListOf<AmarWorkspaceAuditEvent>()

    fun record(event: AmarWorkspaceAuditEvent) {
        if (event.id.isBlank() || event.action.isBlank() || event.actor.isBlank()) return
        events += event
    }

    fun snapshot(): List<AmarWorkspaceAuditEvent> = events.toList()
}

data class AmarKnowledgeUpdate(
    val newRecord: KnowledgeRecord,
    val replacesId: String?,
    val evidence: List<EvidenceRecord>
)

class AmarKnowledgeUpdateGate {
    fun allow(update: AmarKnowledgeUpdate): Boolean {
        if (!update.newRecord.validated || update.newRecord.sourceIds.isEmpty()) return false
        if (update.evidence.isEmpty()) return false
        if (update.evidence.any { !it.valid || it.statement.isBlank() || it.source.isBlank() }) return false
        if (update.replacesId == update.newRecord.id) return false
        return true
    }
}

class AmarSensitiveActionController(private val audit: AmarWorkspaceAuditLog) {
    fun authorize(
        actionId: String,
        actor: String,
        permissionGranted: Boolean,
        sensitive: Boolean,
        confirmed: Boolean,
        nowEpochMs: Long
    ): Boolean {
        val allowed = permissionGranted && (!sensitive || confirmed)
        audit.record(
            AmarWorkspaceAuditEvent(
                id = actionId,
                action = "authorize",
                actor = actor,
                timestampEpochMs = nowEpochMs,
                allowed = allowed,
                reason = when {
                    !permissionGranted -> "permission_required"
                    sensitive && !confirmed -> "explicit_confirmation_required"
                    else -> "authorized"
                }
            )
        )
        return allowed
    }
}
