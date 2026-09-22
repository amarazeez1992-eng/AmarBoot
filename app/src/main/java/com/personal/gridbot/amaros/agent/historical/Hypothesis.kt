package com.personal.gridbot.amaros.agent.historical

/** Canonical Point 19 hypothesis representation. */
data class Hypothesis(
    val id: String,
    val market: String,
    val timeframe: String,
    val attributes: Map<String, String> = emptyMap()
) {
    init {
        require(id.isNotBlank())
        require(market.isNotBlank())
        require(timeframe.isNotBlank())
    }
}
