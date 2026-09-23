package com.personal.gridbot.amaros.agent.change

data class EvidenceChangeDetectionResult(
    val changes: List<EvidenceChange>,
    val actionFlag: Boolean,
    val isDownstreamReady: Boolean,
    val reason: ChangeDetectionReason
) {
    companion object {
        fun immutable(
            changes: List<EvidenceChange>,
            actionFlag: Boolean,
            isDownstreamReady: Boolean,
            reason: ChangeDetectionReason
        ): EvidenceChangeDetectionResult =
            EvidenceChangeDetectionResult(
                changes = changes.toList(),
                actionFlag = actionFlag,
                isDownstreamReady = isDownstreamReady,
                reason = reason
            )
    }
}
