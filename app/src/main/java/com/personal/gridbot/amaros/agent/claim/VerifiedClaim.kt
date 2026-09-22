package com.personal.gridbot.amaros.agent.claim

data class VerifiedClaim(
    val claim: StructuredClaim,
    val state: ClaimVerificationState,
    val supportingEvidenceIds: List<String>,
    val opposingEvidenceIds: List<String>,
    val explanation: String
)
