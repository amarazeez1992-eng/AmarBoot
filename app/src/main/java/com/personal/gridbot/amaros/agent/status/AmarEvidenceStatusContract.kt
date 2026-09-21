package com.personal.gridbot.amaros.agent.status

import com.personal.gridbot.amaros.agent.AmarEvidenceQualityUpstreamState
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate

enum class ConflictState {
    NOT_AVAILABLE,
    NO_CONFLICT,
    CONFLICTED
}

data class AmarEvidenceStatusInput(
    val candidates: List<RelevantCandidate>,
    val upstreamState: AmarEvidenceQualityUpstreamState,
    val admissionStates: Map<String, Boolean>,
    val freshnessStates: Map<String, Boolean>,
    val conflictState: ConflictState = ConflictState.NOT_AVAILABLE,
    val minimumCount: Int = DEFAULT_MIN_EVIDENCE_COUNT
) {
    companion object {
        const val DEFAULT_MIN_EVIDENCE_COUNT = 1
    }
}

interface AmarEvidenceStatusContract {
    fun classify(input: AmarEvidenceStatusInput): EvidenceStatusResult
}
