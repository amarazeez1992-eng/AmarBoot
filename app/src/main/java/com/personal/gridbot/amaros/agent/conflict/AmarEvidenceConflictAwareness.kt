package com.personal.gridbot.amaros.agent.conflict

import com.personal.gridbot.amaros.agent.status.ConflictState

class AmarEvidenceConflictAwareness : AmarEvidenceConflictAwarenessContract {
    override fun evaluate(input: AmarEvidenceConflictAwarenessInput): EvidenceConflictAwarenessResult {
        val conflicts = input.conflicts
            ?: return EvidenceConflictAwarenessResult.notAvailable(
                ConflictAwarenessReason.DETECTOR_UNAVAILABLE
            )

        if (conflicts.isEmpty()) {
            return EvidenceConflictAwarenessResult(
                globalConflictState = ConflictState.NO_CONFLICT,
                conflictingFingerprints = emptySet(),
                conflictDetails = emptyList(),
                isDownstreamReady = true,
                reason = ConflictAwarenessReason.NO_CONFLICTS_DETECTED
            )
        }

        val fingerprints = conflicts.flatMap {
            it.supportingFingerprints + it.opposingFingerprints
        }.toSet()

        return EvidenceConflictAwarenessResult(
            globalConflictState = ConflictState.CONFLICTED,
            conflictingFingerprints = fingerprints,
            conflictDetails = conflicts,
            isDownstreamReady = true,
            reason = ConflictAwarenessReason.CONFLICTS_DETECTED
        )
    }
}
