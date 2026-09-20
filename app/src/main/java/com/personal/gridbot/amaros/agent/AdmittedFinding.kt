package com.personal.gridbot.amaros.agent

data class AdmittedFinding(
    val finding: ResearchFinding,
    val state: AdmissionState,
    val relevanceScore: Double,
    val admissionReason: String
) {
    init {
        require(!relevanceScore.isNaN())
        require(!relevanceScore.isInfinite())
        require(relevanceScore in 0.0..1.0)
    }
}
