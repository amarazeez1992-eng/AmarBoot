package com.personal.gridbot.amaros.agent.deterministic

import com.personal.gridbot.amaros.agent.protection.ProtectedEvidence

data class CanonicalEvidence(
    val evidence: ProtectedEvidence,
    val canonicalKey: String
)
