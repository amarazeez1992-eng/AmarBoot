package com.personal.gridbot.amaros.agent.correlation

import com.personal.gridbot.amaros.agent.status.ConflictState

/**
 * Stateless, deterministic Point 20 correlation boundary.
 *
 * Point 5 owns independence calculation.
 * Point 14 owns conflict detection.
 * Point 18 owns canonical evidence.
 */
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

        val fingerprints = canonical.map { it.evidence.evidence.candidate.fingerprint }.toSet()
        val missingState = fingerprints.any { it !in input.independenceStates }
        if (missingState) {
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

        val byFingerprint = canonical.groupBy { it.evidence.evidence.candidate.fingerprint }
        val groups = byFingerprint.values.mapNotNull { entries ->
            if (entries.size < 2) return@mapNotNull null

            val providers = entries.map { it.evidence.evidence.candidate.provider }.toSet()
            if (providers.size < 2) return@mapNotNull null

            val keys = entries.map { it.evidence.evidence.candidate.fingerprint }
            val independent = keys.all { input.independenceStates.getValue(it) }

            CorrelatedGroup(
                evidenceFingerprints = keys.toSet(),
                correlationType = if (independent) {
                    CorrelationType.AGREEMENT
                } else {
                    CorrelationType.DEPENDENCY
                },
                sharedClaims = emptyList()
            )
        }

        if (groups.isEmpty()) {
            return CrossSourceCorrelationResult(
                correlatedGroups = emptyList(),
                isDownstreamReady = true,
                reason = CorrelationReason.INSUFFICIENT_DATA
            )
        }

        return CrossSourceCorrelationResult(
            correlatedGroups = groups.sortedBy { it.evidenceFingerprints.minOrNull().orEmpty() },
            isDownstreamReady = true,
            reason = CorrelationReason.VALID_CORRELATION
        )
    }
}
