package com.personal.gridbot.amaros.agent.admission

data class EvidenceIntakeResult(
    val candidates: List<NormalizedCandidate>,
    val rejectedCandidates: List<IntakeRejectedCandidate>
) {
    companion object {
        fun empty() = EvidenceIntakeResult(emptyList(), emptyList())
    }
}
