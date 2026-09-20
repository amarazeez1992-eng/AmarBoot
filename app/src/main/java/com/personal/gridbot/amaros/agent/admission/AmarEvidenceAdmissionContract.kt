package com.personal.gridbot.amaros.agent.admission

import com.personal.gridbot.amaros.agent.ResearchFinding

interface AmarEvidenceAdmissionContract {
    fun admit(
        question: String,
        findings: List<ResearchFinding>
    ): AdmissionResult
}
