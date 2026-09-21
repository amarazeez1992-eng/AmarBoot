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
            val decision = engine.accept(
                question,
                candidate.title,
                candidate.normalizedExcerpt
            )

            if (decision.accepted) {
                admitted.add(RelevantCandidate(candidate, decision.score, "ACCEPTED"))
            } else {
                rejected.add(
                    RelevanceRejectedCandidate(
                        candidate,
                        decision.score,
                        decision.reason.toString()
                    )
                )
            }
        }

        return RelevanceResult(admitted, rejected)
    }
}
