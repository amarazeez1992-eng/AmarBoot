package com.personal.gridbot.amaros.agent.admission

import com.personal.gridbot.amaros.agent.ResearchFinding

data class AdmittedFinding(
    val finding: ResearchFinding,
    val state: AdmissionState,
    val relevanceScore: Double,
    val admissionReason: String
)
