package com.personal.gridbot.amaros.agent.admission

interface AmarEvidenceIntakeContract {
    fun intake(
        question: String,
        candidates: List<EvidenceCandidate>
    ): EvidenceIntakeResult
}
