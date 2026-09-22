package com.personal.gridbot.amaros.agent.lifecycle

data class LifecycleTransition(
    val fromState: EvidenceLifecycleState,
    val toState: EvidenceLifecycleState,
    val timestamp: Long,
    val reason: String
)
