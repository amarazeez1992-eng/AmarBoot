package com.personal.gridbot.amaros.agent.conflict

import com.personal.gridbot.amaros.agent.status.ConflictState
import com.personal.gridbot.amaros.intelligence.verification.AmarConflict

data class EvidenceConflictAwarenessResult(
    val globalConflictState: ConflictState,
    val conflictingFingerprints: Set<String>,
    val conflictDetails: List<AmarConflict>,
    val isDownstreamReady: Boolean,
    val reason: ConflictAwarenessReason
) {
    companion object {
        fun notAvailable(reason: ConflictAwarenessReason) = EvidenceConflictAwarenessResult(
            globalConflictState = ConflictState.NOT_AVAILABLE,
            conflictingFingerprints = emptySet(),
            conflictDetails = emptyList(),
            isDownstreamReady = false,
            reason = reason
        )
    }
}
