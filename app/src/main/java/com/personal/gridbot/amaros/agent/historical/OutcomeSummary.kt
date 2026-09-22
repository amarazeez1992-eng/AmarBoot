package com.personal.gridbot.amaros.agent.historical

data class OutcomeSummary(
    val totalOccurrences: Int,
    val outcomeCounts: Map<String, Int>
) {
    init {
        require(totalOccurrences >= 0)
        require(outcomeCounts.values.all { it >= 0 })
        require(outcomeCounts.values.sum() == totalOccurrences)
    }
}
