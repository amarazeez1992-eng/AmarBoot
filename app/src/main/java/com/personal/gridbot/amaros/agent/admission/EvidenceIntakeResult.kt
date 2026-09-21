package com.personal.gridbot.amaros.agent.admission

data class EvidenceIntakeResult(
    val candidates: List<NormalizedCandidate>,
    val rejectedCandidates: List<IntakeRejectedCandidate>
) {
    val intakeVerified: Boolean
        get() = candidates.isNotEmpty() && rejectedCandidates.isEmpty()
    companion object {
        fun empty() = EvidenceIntakeResult(emptyList(), emptyList())
    }
}
