package com.personal.gridbot.amaros.agent.lifecycle

data class EvidenceLifecycleResult(
    val currentStates: List<EvidenceLifecycleSnapshot>,
    val transitions: List<LifecycleTransition>,
    val isDownstreamReady: Boolean,
    val reason: LifecycleReason
)
