package com.personal.gridbot.amaros.agent.relevance

import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate

data class RelevanceRejectedCandidate(
    val candidate: NormalizedCandidate,
    val relevanceScore: Double,
    val reason: String
) {
    init {
        require(!relevanceScore.isNaN()) { "relevanceScore must not be NaN" }
        require(relevanceScore.isFinite()) { "relevanceScore must be finite" }
        require(relevanceScore in 0.0..1.0) { "relevanceScore must be in [0.0, 1.0]" }
        require(reason.isNotBlank()) { "reason must not be blank" }
    }
}
