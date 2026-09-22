package com.personal.gridbot.amaros.agent.historical

import com.personal.gridbot.amaros.agent.deterministic.DeterministicEvidenceResult
import com.personal.gridbot.amaros.intelligence.advanced.AmarRegimeObservation

interface AmarHistoricalValidationContract {
    fun validate(
        hypothesis: Hypothesis,
        historicalCases: List<HistoricalCase>,
        matchCriteria: MatchCriteria,
        regimeContext: AmarRegimeObservation,
        deterministicEvidenceResult: DeterministicEvidenceResult,
        decisionCutoffMs: Long
    ): HistoricalValidationResult
}
