package com.personal.gridbot.amaros.agent

interface AmarEvidenceAdmissionContract {
    fun admit(question: String, findings: List<ResearchFinding>): AdmissionResult
}
