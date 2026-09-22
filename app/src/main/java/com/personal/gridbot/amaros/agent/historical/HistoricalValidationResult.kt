package com.personal.gridbot.amaros.agent.historical

data class HistoricalValidationResult(
    val comparableCases: List<ComparableCase>,
    val observedOutcomes: OutcomeSummary,
    val frequency: Int,
    val differences: List<String>,
    val isDownstreamReady: Boolean,
    val reason: HistoricalValidationReason
) {
    init {
        require(frequency >= 0)
        require(frequency == comparableCases.size)
    }
}
