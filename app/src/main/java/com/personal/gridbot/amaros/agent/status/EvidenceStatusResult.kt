package com.personal.gridbot.amaros.agent.status

import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate

data class ClassifiedEvidence(
    val candidate: RelevantCandidate,
    val status: EvidenceStatus,
    val reason: RejectionReason?
)

data class EvidenceStatusResult(
    val classified: List<ClassifiedEvidence>,
    val unclassified: List<RelevantCandidate>
) {
    companion object {
        fun empty() = EvidenceStatusResult(emptyList(), emptyList())
    }
}

enum class RejectionReason {
    UNVERIFIED_ADMISSION,
    INSUFFICIENT_EVIDENCE,
    CONFLICT_DETECTED,
    STALE_EVIDENCE,
    PARTIAL_UPSTREAM_VERIFICATION
}
