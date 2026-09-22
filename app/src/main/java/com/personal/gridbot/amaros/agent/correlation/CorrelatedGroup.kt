package com.personal.gridbot.amaros.agent.correlation

data class CorrelatedGroup(
    val evidenceFingerprints: Set<String>,
    val correlationType: CorrelationType,
    val sharedClaims: List<String>
)
