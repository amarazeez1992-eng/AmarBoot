package com.personal.gridbot.amaros.agent.historical

import com.personal.gridbot.amaros.agent.deterministic.DeterministicEvidenceResult
import com.personal.gridbot.amaros.intelligence.advanced.AmarRegimeObservation

/** Stateless, deterministic Point 19 validation boundary. */
class AmarHistoricalValidator : AmarHistoricalValidationContract {
    override fun validate(
        hypothesis: Hypothesis,
        historicalCases: List<HistoricalCase>,
        matchCriteria: MatchCriteria,
        regimeContext: AmarRegimeObservation,
        deterministicEvidenceResult: DeterministicEvidenceResult,
        decisionCutoffMs: Long
    ): HistoricalValidationResult {
        if (decisionCutoffMs < 0L ||
            hypothesis.id.isBlank() ||
            hypothesis.market.isBlank() ||
            hypothesis.timeframe.isBlank() ||
            !deterministicEvidenceResult.isDownstreamReady
        ) return emptyResult(HistoricalValidationReason.INVALID_INPUT)

        if (historicalCases.isEmpty()) return emptyResult(HistoricalValidationReason.DATA_UNAVAILABLE)

        val eligible = historicalCases.filter { it.decisionTimeMs <= decisionCutoffMs }
        val structural = eligible.filter {
            it.market == hypothesis.market &&
            it.timeframe == hypothesis.timeframe &&
            it.hypothesisId == hypothesis.id
        }
        val regimeMatches = structural.filter { it.regimeObservation.regime == regimeContext.regime }
        val comparable = regimeMatches
            .filter { matchCriteria.matches(hypothesis, it, regimeContext.regime) }
            .map { ComparableCase(it, differencesFor(hypothesis, it)) }

        if (comparable.isEmpty()) {
            val reason = if (structural.isNotEmpty() && regimeMatches.isEmpty())
                HistoricalValidationReason.REGIME_MISMATCH
            else HistoricalValidationReason.NO_COMPARABLE_CASES
            return emptyResult(reason)
        }

        val counts = comparable.groupingBy { it.historicalCase.outcome }.eachCount()
        val differences = comparable.flatMap { it.differences }.distinct()
        val ready = comparable.size >= matchCriteria.minimumComparableCases
        return HistoricalValidationResult(
            comparableCases = comparable,
            observedOutcomes = OutcomeSummary(comparable.size, counts),
            frequency = comparable.size,
            differences = differences,
            isDownstreamReady = ready,
            reason = if (ready) HistoricalValidationReason.VALID_COMPARABLE_CASES
                     else HistoricalValidationReason.INSUFFICIENT_COMPARABLE_CASES
        )
    }

    private fun differencesFor(hypothesis: Hypothesis, historicalCase: HistoricalCase): List<String> =
        hypothesis.attributes.keys
            .filter { historicalCase.attributes[it] != hypothesis.attributes[it] }
            .map { "attribute:$it" }

    private fun emptyResult(reason: HistoricalValidationReason) = HistoricalValidationResult(
        comparableCases = emptyList(),
        observedOutcomes = OutcomeSummary(0, emptyMap()),
        frequency = 0,
        differences = emptyList(),
        isDownstreamReady = false,
        reason = reason
    )
}
