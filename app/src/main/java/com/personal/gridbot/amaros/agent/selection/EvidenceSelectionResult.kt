package com.personal.gridbot.amaros.agent.selection

import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate

data class EvidenceSelectionResult(
    val selected: List<SelectedEvidence>,
    val dropped: List<RelevantCandidate>
) {
    companion object {
        fun empty() = EvidenceSelectionResult(emptyList(), emptyList())
    }
}
