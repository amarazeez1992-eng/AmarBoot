package com.personal.gridbot.amaros.agent.deterministic

import com.personal.gridbot.amaros.agent.protection.FutureEvidence
import com.personal.gridbot.amaros.agent.protection.InvalidEvidence

data class DeterministicEvidenceResult(
    val canonicalEvidence: List<CanonicalEvidence>,
    val invalidEvidence: List<InvalidEvidence>,
    val futureEvidence: List<FutureEvidence>,
    val isDownstreamReady: Boolean,
    val handlingReason: DeterministicHandlingReason
) {
    companion object {
        fun failed(reason: DeterministicHandlingReason): DeterministicEvidenceResult =
            DeterministicEvidenceResult(
                canonicalEvidence = emptyList(),
                invalidEvidence = emptyList(),
                futureEvidence = emptyList(),
                isDownstreamReady = false,
                handlingReason = reason
            )
    }
}
