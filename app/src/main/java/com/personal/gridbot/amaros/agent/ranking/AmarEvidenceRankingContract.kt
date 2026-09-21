package com.personal.gridbot.amaros.agent.ranking

import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence
import com.personal.gridbot.amaros.agent.status.QueryContext

data class AmarEvidenceRankingInput(
    val classifiedEvidence: List<ClassifiedEvidence>,
    val relevanceScores: Map<String, Double>,
    val authorityScores: Map<String, Double>,
    val freshnessScores: Map<String, Double>,
    val userQuestion: String,
    val queryContext: QueryContext
)

interface AmarEvidenceRankingContract {
    fun rank(input: AmarEvidenceRankingInput): EvidenceRankingResult
}
