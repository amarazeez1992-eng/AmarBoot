package com.personal.gridbot.amaros.agent.historical

import com.personal.gridbot.amaros.intelligence.advanced.AmarMarketRegime

data class MatchCriteria(
    val requireMarket: Boolean = true,
    val requireTimeframe: Boolean = true,
    val requireRegime: Boolean = true,
    val requireHypothesis: Boolean = true,
    val requiredAttributes: Set<String> = emptySet(),
    val minimumComparableCases: Int = 1
) {
    init { require(minimumComparableCases > 0) }

    fun matches(hypothesis: Hypothesis, historicalCase: HistoricalCase, currentRegime: AmarMarketRegime): Boolean =
        (!requireMarket || historicalCase.market == hypothesis.market) &&
        (!requireTimeframe || historicalCase.timeframe == hypothesis.timeframe) &&
        (!requireRegime || historicalCase.regimeObservation.regime == currentRegime) &&
        (!requireHypothesis || historicalCase.hypothesisId == hypothesis.id) &&
        requiredAttributes.all { key -> historicalCase.attributes[key] == hypothesis.attributes[key] }
}
