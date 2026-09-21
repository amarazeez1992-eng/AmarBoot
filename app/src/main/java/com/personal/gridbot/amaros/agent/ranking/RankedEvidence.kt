package com.personal.gridbot.amaros.agent.ranking

import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence

data class RankedEvidence(
    val evidence: ClassifiedEvidence,
    val rank: Int,
    val rankingSignals: Map<String, Double>,
    val explanation: String
) {
    init {
        require(rank > 0) { "rank must be positive" }
        require(rankingSignals.keys.containsAll(RankingSignal.requiredKeys())) {
            "rankingSignals must contain all required signal keys"
        }
        require(explanation.isNotBlank()) { "explanation must not be blank" }
    }
}
