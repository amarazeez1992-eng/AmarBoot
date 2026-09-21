package com.personal.gridbot.amaros.agent.relevance

import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate

interface AmarQuestionRelevanceContract {
    fun evaluate(
        question: String,
        candidates: List<NormalizedCandidate>
    ): RelevanceResult
}
