package com.personal.gridbot.amaros.agent.lifecycle

data class EvidenceLifecycleSnapshot(
    val evidenceFingerprint: String,
    val state: EvidenceLifecycleState,
    val createdAt: Long,
    val lastTransitionAt: Long,
    val transitions: List<LifecycleTransition>
)
