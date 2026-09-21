package com.personal.gridbot.amaros.agent.selection

import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate

data class SelectedEvidence(
    val candidate: RelevantCandidate,
    val rank: Int
) {
    init {
        require(rank >= 1) { "rank must be >= 1" }
    }
}
