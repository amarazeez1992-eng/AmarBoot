package com.personal.gridbot.amaros.agent.selection

import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate

class AmarEvidenceSelection : AmarEvidenceSelectionContract {
    override fun select(
        candidates: List<RelevantCandidate>,
        maxSelected: Int
    ): EvidenceSelectionResult {
        require(maxSelected > 0) { "maxSelected must be > 0" }

        if (candidates.isEmpty()) return EvidenceSelectionResult.empty()

        val sorted = candidates.sortedWith(
            compareByDescending<RelevantCandidate> { it.relevanceScore }
                .thenBy { it.candidate.fingerprint }
        )

        val selected = sorted.take(maxSelected)
            .mapIndexed { index, c -> SelectedEvidence(c, index + 1) }
        val dropped = sorted.drop(maxSelected)

        return EvidenceSelectionResult(selected, dropped)
    }
}
