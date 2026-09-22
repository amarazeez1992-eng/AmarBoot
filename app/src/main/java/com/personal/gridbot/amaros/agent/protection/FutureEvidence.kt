package com.personal.gridbot.amaros.agent.protection

import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence

data class FutureEvidence(
    val evidence: ClassifiedEvidence,
    val futureTimestamp: Long,
    val reason: String
)
