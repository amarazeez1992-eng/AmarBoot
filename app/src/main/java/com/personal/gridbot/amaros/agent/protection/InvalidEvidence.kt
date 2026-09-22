package com.personal.gridbot.amaros.agent.protection

import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence

data class InvalidEvidence(
    val evidence: ClassifiedEvidence,
    val reason: InvalidReason,
    val originalSource: String
)
