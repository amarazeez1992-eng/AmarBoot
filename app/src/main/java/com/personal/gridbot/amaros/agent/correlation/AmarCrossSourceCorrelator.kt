package com.personal.gridbot.amaros.agent.correlation

import com.personal.gridbot.amaros.agent.status.ConflictState

class AmarCrossSourceCorrelator : AmarCrossSourceCorrelationContract {

    override fun correlate(input: CrossSourceCorrelationInput): CrossSourceCorrelationResult {
        if (input.classifiedEvidence.any { it.candidate.candidate.fingerprint.isBlank() }) {
            return CrossSourceCorrelationResult(
                correlatedGroups = emptyList(),
                isDownstreamReady = false,
                reason = CorrelationReason.INVALID_INPUT
            )
        }

        if (!input.deterministicEvidence.isDownstreamReady ||
            !input.conflictAwareness.isDownstreamReady
        ) {
            return CrossSourceCorrelationResult(
                correlatedGroups = emptyList(),
                isDownstreamReady = false,
                reason = CorrelationReason.INSUFFICIENT_DATA
            )
        }

        val canonical = input.deterministicEvidence.canonicalEvidence
        if (canonical.isEmpty()) {
            return CrossSourceCorrelationResult(
                correlatedGroups = emptyList(),
                isDownstreamReady = false,
                reason = CorrelationReason.INSUFFICIENT_DATA
            )
        }

        val fingerprints = canonical
            .map { it.evidence.evidence.candidate.candidate.fingerprint }
            .toSet()

        if (fingerprints.any { it !in input.independenceStates }) {
            return CrossSourceCorrelationResult(
                correlatedGroups = emptyList(),
                isDownstreamReady = false,
                reason = CorrelationReason.MISSING_INDEPENDENCE_STATE
            )
        }

        if (input.conflictAwareness.globalConflictState == ConflictState.CONFLICTED) {
            val conflicting = input.conflictAwareness.conflictingFingerprints
                .filter { it in fingerprints }
                .toSet()
            if (conflicting.size >= 2) {
                return CrossSourceCorrelationResult(
                    correlatedGroups = listOf(
                        CorrelatedGroup(
                            evidenceFingerprints = conflicting,
                            correlationType = CorrelationType.DISAGREEMENT,
                            sharedClaims = emptyList()
                        )
                    ),
                    isDownstreamReady = true,
                    reason = CorrelationReason.CONFLICT_UPSTREAM
                )
            }
            return CrossSourceCorrelationResult(
                correlatedGroups = emptyList(),
                isDownstreamReady = false,
                reason = CorrelationReason.CONFLICT_UPSTREAM
            )
        }

        val verification = input.claimVerification
            ?: return CrossSourceCorrelationResult(
                correlatedGroups = emptyList(),
                isDownstreamReady = true,
                reason = CorrelationReason.INSUFFICIENT_DATA
            )

        val groups = mutableListOf<CorrelatedGroup>()

        verification.verifiedClaims.forEach { vc ->
            val claimText = vc.claim.text
            val supporting = vc.supportingEvidenceIds.filter { it in fingerprints }.toSet()
            val opposing = vc.opposingEvidenceIds.filter { it in fingerprints }.toSet()

            if (supporting.size >= 2) {
                val independent = supporting.all { input.independenceStates.getValue(it) }
                groups += CorrelatedGroup(
                    evidenceFingerprints = supporting,
                    correlationType = if (independent) CorrelationType.AGREEMENT else CorrelationType.DEPENDENCY,
                    sharedClaims = listOf(claimText)
                )
            }

            if (opposing.size >= 2) {
                groups += CorrelatedGroup(
                    evidenceFingerprints = opposing,
                    correlationType = CorrelationType.DISAGREEMENT,
                    sharedClaims = listOf(claimText)
                )
            }
        }

        if (groups.isEmpty()) {
            return CrossSourceCorrelationResult(
                correlatedGroups = emptyList(),
                isDownstreamReady = true,
                reason = CorrelationReason.INSUFFICIENT_DATA
            )
        }

        return CrossSourceCorrelationResult(
            correlatedGroups = groups.sortedWith(
                compareBy<CorrelatedGroup> { it.correlationType.ordinal }
                    .thenBy { it.evidenceFingerprints.minOrNull().orEmpty() }
                    .thenBy { it.sharedClaims.firstOrNull().orEmpty() }
            ),
            isDownstreamReady = true,
            reason = CorrelationReason.VALID_CORRELATION
        )
    }
}
