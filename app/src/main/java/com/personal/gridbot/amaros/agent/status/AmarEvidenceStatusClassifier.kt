package com.personal.gridbot.amaros.agent.status

import com.personal.gridbot.amaros.agent.AmarEvidenceQualityUpstreamState

class AmarEvidenceStatusClassifier : AmarEvidenceStatusContract {
    override fun classify(input: AmarEvidenceStatusInput): EvidenceStatusResult {
        require(input.minimumCount > 0) { "minimumCount must be > 0" }

        if (input.candidates.isEmpty()) {
            return EvidenceStatusResult.empty()
        }

        val classified = mutableListOf<ClassifiedEvidence>()
        val unclassified = mutableListOf<com.personal.gridbot.amaros.agent.relevance.RelevantCandidate>()

        for (candidate in input.candidates.sortedBy { it.candidate.fingerprint }) {
            val fp = candidate.candidate.fingerprint
            val admission = input.admissionStates[fp]
            val fresh = input.freshnessStates[fp]

            if (admission == null || fresh == null) {
                unclassified.add(candidate)
                continue
            }

            val (status, reason) = classifyOne(
                admission = admission,
                fresh = fresh,
                upstream = input.upstreamState,
                conflict = input.conflictState,
                candidateCount = input.candidates.size,
                minimumCount = input.minimumCount
            )

            classified.add(ClassifiedEvidence(candidate, status, reason))
        }

        return EvidenceStatusResult(classified, unclassified)
    }

    private fun classifyOne(
        admission: Boolean,
        fresh: Boolean,
        upstream: AmarEvidenceQualityUpstreamState,
        conflict: ConflictState,
        candidateCount: Int,
        minimumCount: Int
    ): Pair<EvidenceStatus, RejectionReason?> {
        if (!admission) {
            return EvidenceStatus.UNVERIFIED to RejectionReason.UNVERIFIED_ADMISSION
        }
        if (candidateCount < minimumCount) {
            return EvidenceStatus.INSUFFICIENT to RejectionReason.INSUFFICIENT_EVIDENCE
        }
        if (conflict == ConflictState.CONFLICTED) {
            return EvidenceStatus.CONFLICTED to RejectionReason.CONFLICT_DETECTED
        }
        if (!fresh) {
            return EvidenceStatus.STALE to RejectionReason.STALE_EVIDENCE
        }
        if (upstream.fullyVerified) {
            return EvidenceStatus.COMPLETE to null
        }
        return EvidenceStatus.PARTIAL to RejectionReason.PARTIAL_UPSTREAM_VERIFICATION
    }
}
