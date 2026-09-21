package com.personal.gridbot.amaros.agent.relevance

data class RelevanceResult(
    val admitted: List<RelevantCandidate>,
    val rejected: List<RelevanceRejectedCandidate>
) {
    companion object {
        fun empty() = RelevanceResult(emptyList(), emptyList())
    }
}
