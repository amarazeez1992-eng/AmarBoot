package com.personal.gridbot.amaros.agent.status

import com.personal.gridbot.amaros.agent.AmarEvidenceQualityUpstreamState
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate

class AmarEvidenceStatusClassifier : AmarEvidenceStatusContract {

    override fun classify(input: AmarEvidenceStatusInput): EvidenceStatusResult {
        if (input.candidates.isEmpty()) {
            return EvidenceStatusResult.empty()
        }

        val classified = mutableListOf<ClassifiedEvidence>()
        val unclassified = mutableListOf<RelevantCandidate>()

        // Canonical deterministic ordering, NOT ranking.
        for (candidate in input.candidates.sortedBy { it.candidate.fingerprint }) {
            val fingerprint = candidate.candidate.fingerprint
            val admission = input.admissionStates[fingerprint]
            val freshness = input.freshnessStates[fingerprint]

            if (admission == null || freshness == null) {
                unclassified.add(candidate)
                continue
            }

            val classification = classifyCandidate(
                admission = admission,
                freshness = freshness,
                upstream = input.upstreamState,
                conflict = input.conflictState,
                candidateCount = input.candidates.size,
                minimumEvidenceCount = input.queryContext.minimumEvidenceCount
            )

            classified.add(
                ClassifiedEvidence(
                    candidate = candidate,
                    status = classification.status,
                    reason = classification.reason,
                    explanation = classification.explanation
                )
            )
        }

        val distribution = buildDistribution(classified)
        val overallStatus = determineOverallStatus(classified, unclassified)
        val downstreamReady = determineDownstreamReadiness(
            classified = classified,
            unclassified = unclassified,
            overallStatus = overallStatus
        )

        return EvidenceStatusResult(
            classified = classified,
            unclassified = unclassified,
            distribution = distribution,
            overallStatus = overallStatus,
            isDownstreamReady = downstreamReady
        )
    }

    private data class Classification(
        val status: EvidenceStatus,
        val reason: RejectionReason?,
        val explanation: String
    )

    private fun classifyCandidate(
        admission: Boolean,
        freshness: Boolean,
        upstream: AmarEvidenceQualityUpstreamState,
        conflict: ConflictState,
        candidateCount: Int,
        minimumEvidenceCount: Int
    ): Classification {
        if (!admission) {
            return Classification(
                status = EvidenceStatus.UNVERIFIED,
                reason = RejectionReason.UNVERIFIED_ADMISSION,
                explanation = "Evidence is unverified because admission verification failed."
            )
        }

        if (candidateCount < minimumEvidenceCount) {
            return Classification(
                status = EvidenceStatus.INSUFFICIENT,
                reason = RejectionReason.INSUFFICIENT_EVIDENCE,
                explanation = "Evidence is insufficient because the available evidence count is below the required minimum."
            )
        }

        if (conflict == ConflictState.CONFLICTED) {
            return Classification(
                status = EvidenceStatus.CONFLICTED,
                reason = RejectionReason.CONFLICT_DETECTED,
                explanation = "Evidence is conflicted because the supplied canonical conflict state reports a conflict."
            )
        }

        if (!freshness) {
            return Classification(
                status = EvidenceStatus.STALE,
                reason = RejectionReason.STALE_EVIDENCE,
                explanation = "Evidence is stale because freshness verification failed."
            )
        }

        if (upstream.fullyVerified) {
            return Classification(
                status = EvidenceStatus.COMPLETE,
                reason = null,
                explanation = "Evidence is complete because all supplied upstream verification states are verified."
            )
        }

        return Classification(
            status = EvidenceStatus.PARTIAL,
            reason = RejectionReason.PARTIAL_UPSTREAM_VERIFICATION,
            explanation = "Evidence is partially verified because one or more upstream verification states are not verified."
        )
    }

    private fun buildDistribution(
        classified: List<ClassifiedEvidence>
    ): Map<EvidenceStatus, Int> {
        val counts = EvidenceStatus.entries.associateWith { 0 }.toMutableMap()
        for (item in classified) {
            counts[item.status] = counts.getValue(item.status) + 1
        }
        return counts.toMap()
    }

    private fun determineOverallStatus(
        classified: List<ClassifiedEvidence>,
        unclassified: List<RelevantCandidate>
    ): EvidenceStatus {
        if (classified.isEmpty() && unclassified.isEmpty()) {
            return EvidenceStatus.UNVERIFIED
        }

        if (unclassified.isNotEmpty()) {
            return EvidenceStatus.UNVERIFIED
        }

        if (classified.any { it.status == EvidenceStatus.UNVERIFIED }) {
            return EvidenceStatus.UNVERIFIED
        }

        if (classified.any { it.status == EvidenceStatus.INSUFFICIENT }) {
            return EvidenceStatus.INSUFFICIENT
        }

        if (classified.any { it.status == EvidenceStatus.CONFLICTED }) {
            return EvidenceStatus.CONFLICTED
        }

        if (classified.any { it.status == EvidenceStatus.STALE }) {
            return EvidenceStatus.PARTIAL
        }

        if (classified.all { it.status == EvidenceStatus.COMPLETE }) {
            return EvidenceStatus.COMPLETE
        }

        return EvidenceStatus.PARTIAL
    }

    private fun determineDownstreamReadiness(
        classified: List<ClassifiedEvidence>,
        unclassified: List<RelevantCandidate>,
        overallStatus: EvidenceStatus
    ): Boolean {
        if (classified.isEmpty()) return false
        if (unclassified.isNotEmpty()) return false
        return overallStatus == EvidenceStatus.COMPLETE ||
            overallStatus == EvidenceStatus.PARTIAL
    }
}
