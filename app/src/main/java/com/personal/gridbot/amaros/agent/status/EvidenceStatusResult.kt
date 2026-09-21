package com.personal.gridbot.amaros.agent.status

import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate

data class ClassifiedEvidence(
    val candidate: RelevantCandidate,
    val status: EvidenceStatus,
    val reason: RejectionReason?,
    val explanation: String
)

data class EvidenceStatusResult(
    val classified: List<ClassifiedEvidence>,
    val unclassified: List<RelevantCandidate>,
    val distribution: Map<EvidenceStatus, Int>,
    val overallStatus: EvidenceStatus,
    val isDownstreamReady: Boolean
) {
    companion object {
        fun empty(): EvidenceStatusResult {
            val zeroDistribution = EvidenceStatus.entries.associateWith { 0 }
            return EvidenceStatusResult(
                classified = emptyList(),
                unclassified = emptyList(),
                distribution = zeroDistribution,
                overallStatus = EvidenceStatus.UNVERIFIED,
                isDownstreamReady = false
            )
        }
    }
}
