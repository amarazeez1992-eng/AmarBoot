package com.personal.gridbot.amaros.agent.protection

import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence

data class ProtectedEvidence(
    val evidence: ClassifiedEvidence,
    val protectionReason: ProtectionReason
)

enum class ProtectionReason {
    NO_PROTECTION_SIGNAL,
    PASSED_ALL_GATES
}
