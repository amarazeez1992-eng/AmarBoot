package com.personal.gridbot.amaros.agent.claim

data class RejectedClaim(
    val claim: StructuredClaim,
    val state: ClaimVerificationState,
    val reason: String
)
