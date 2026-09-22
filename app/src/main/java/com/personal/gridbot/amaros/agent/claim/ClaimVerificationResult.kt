package com.personal.gridbot.amaros.agent.claim

data class ClaimVerificationResult(
    val verifiedClaims: List<VerifiedClaim>,
    val rejectedClaims: List<RejectedClaim>,
    val isDownstreamReady: Boolean
)
