package com.personal.gridbot.amaros.agent.correlation

/**
 * Addition 5 — Consumer of the existing cross-source correlator.
 *
 * This component consumes correlation output only. It does not recalculate
 * fingerprinting, grouping, provider counts, independence, or conflict.
 *
 * Flag-only: agreement is informational and never blocks downstream flow.
 */
class AmarCrossSourceAgreementConsumer {

    fun consume(correlation: CrossSourceCorrelationResult): CrossSourceAgreementResult {
        if (!correlation.isDownstreamReady) {
            return CrossSourceAgreementResult(
                status = CrossSourceAgreementStatus.INSUFFICIENT_AGREEMENT_DATA,
                agreementGroups = emptyList(),
                dependencyGroups = emptyList(),
                disagreementGroups = emptyList(),
                reason = correlation.reason
            )
        }

        val agreement = correlation.correlatedGroups
            .filter { it.correlationType == CorrelationType.AGREEMENT }
            .sortedBy { it.evidenceFingerprints.minOrNull().orEmpty() }

        val dependency = correlation.correlatedGroups
            .filter { it.correlationType == CorrelationType.DEPENDENCY }
            .sortedBy { it.evidenceFingerprints.minOrNull().orEmpty() }

        val disagreement = correlation.correlatedGroups
            .filter { it.correlationType == CorrelationType.DISAGREEMENT }
            .sortedBy { it.evidenceFingerprints.minOrNull().orEmpty() }

        return CrossSourceAgreementResult(
            status = CrossSourceAgreementStatus.READY,
            agreementGroups = agreement,
            dependencyGroups = dependency,
            disagreementGroups = disagreement,
            reason = correlation.reason
        )
    }
}
