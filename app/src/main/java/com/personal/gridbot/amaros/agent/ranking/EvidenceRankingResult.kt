package com.personal.gridbot.amaros.agent.ranking

import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence

data class EvidenceRankingResult(
    val ranked: List<RankedEvidence>,
    val unranked: List<ClassifiedEvidence>,
    val isDownstreamReady: Boolean
) {
    init {
        require(ranked.map { it.rank } == ranked.indices.map { it + 1 }) {
            "ranked evidence must use contiguous 1-based ranks"
        }
        require(isDownstreamReady == (ranked.isNotEmpty() && unranked.isEmpty())) {
            "downstream readiness must be fail-closed"
        }
    }

    companion object {
        fun empty(): EvidenceRankingResult =
            EvidenceRankingResult(emptyList(), emptyList(), false)
    }
}
