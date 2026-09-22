package com.personal.gridbot.amaros.agent.historical

import com.personal.gridbot.amaros.intelligence.advanced.AmarRegimeObservation

/** Historical state for comparison; not an OHLC/bar representation. */
data class HistoricalCase(
    val id: String,
    val market: String,
    val timeframe: String,
    val decisionTimeMs: Long,
    val regimeObservation: AmarRegimeObservation,
    val hypothesisId: String,
    val outcome: String,
    val attributes: Map<String, String> = emptyMap()
) {
    init {
        require(id.isNotBlank())
        require(market.isNotBlank())
        require(timeframe.isNotBlank())
        require(decisionTimeMs >= 0L)
        require(hypothesisId.isNotBlank())
        require(outcome.isNotBlank())
    }
}
