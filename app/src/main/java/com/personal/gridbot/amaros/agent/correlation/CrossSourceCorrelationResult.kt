package com.personal.gridbot.amaros.agent.correlation

data class CrossSourceCorrelationResult(
    val correlatedGroups: List<CorrelatedGroup>,
    val isDownstreamReady: Boolean,
    val reason: CorrelationReason
)
