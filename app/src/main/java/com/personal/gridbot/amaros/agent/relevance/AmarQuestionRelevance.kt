package com.personal.gridbot.amaros.agent.relevance

import com.personal.gridbot.amaros.agent.AmarRetrievalRelevanceEngine
import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate

class AmarQuestionRelevance(
    private val engine: AmarRetrievalRelevanceEngine = AmarRetrievalRelevanceEngine()
) : AmarQuestionRelevanceContract {
    override fun evaluate(
        question: String,
        candidates: List<NormalizedCandidate>
    ): RelevanceResult {
        val admitted = mutableListOf<RelevantCandidate>()
        val rejected = mutableListOf<RelevanceRejectedCandidate>()

        for (candidate in candidates) {
            val result = engine.score(question, candidate.title, candidate.normalizedExcerpt)
            val score = result.score

            if (score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE) {
                admitted.add(RelevantCandidate(candidate, score, "RELEVANCE_ACCEPTED"))
            } else {
                rejected.add(RelevanceRejectedCandidate(candidate, score, "RELEVANCE_REJECTED"))
            }
        }

        return RelevanceResult(admitted, rejected)
    }
}
