package com.personal.gridbot.amaros.agent.status

import com.personal.gridbot.amaros.agent.AmarEvidenceQualityUpstreamState
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate

enum class QueryContext(
    val minimumEvidenceCount: Int
) {
    GENERAL(1),
    FINANCIAL_LIVE(3),
    FINANCIAL_HIST(2),
    CRITICAL(5)
}

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
    val queryContext: QueryContext = QueryContext.GENERAL
)

interface AmarEvidenceStatusContract {
    fun classify(input: AmarEvidenceStatusInput): EvidenceStatusResult
}
