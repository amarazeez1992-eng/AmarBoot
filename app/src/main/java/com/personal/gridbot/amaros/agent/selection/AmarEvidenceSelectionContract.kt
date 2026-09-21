package com.personal.gridbot.amaros.agent.selection

import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate

interface AmarEvidenceSelectionContract {
    fun select(
        candidates: List<RelevantCandidate>,
        maxSelected: Int = DEFAULT_MAX_SELECTED
    ): EvidenceSelectionResult

    companion object {
        const val DEFAULT_MAX_SELECTED = 10
    }
}
