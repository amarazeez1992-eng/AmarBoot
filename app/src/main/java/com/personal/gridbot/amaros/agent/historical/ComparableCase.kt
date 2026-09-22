package com.personal.gridbot.amaros.agent.historical

data class ComparableCase(
    val historicalCase: HistoricalCase,
    val differences: List<String>
)
