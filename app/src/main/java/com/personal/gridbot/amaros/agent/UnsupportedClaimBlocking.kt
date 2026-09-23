package com.personal.gridbot.amaros.agent

enum class BlockingReason {
    NO_CLAIMS,
    UNSUPPORTED_CLAIM,
    MIXED_CLAIMS,
    VERIFICATION_ACCEPTED
}

data class BlockedClaim(
    val claim: String,
    val supportingEvidence: Int,
    val opposingEvidence: Int,
    val matchedEvidence: Int
)

data class BlockingResult(
    val blocked: Boolean,
    val reason: BlockingReason,
    val blockedClaims: List<BlockedClaim>
) {
    companion object {
        fun passed() = BlockingResult(
            blocked = false,
            reason = BlockingReason.VERIFICATION_ACCEPTED,
            blockedClaims = emptyList()
        )
    }
}

object UnsupportedClaimBlocking {
    fun evaluate(report: AmarClaimVerificationReport): BlockingResult {
        val claims = report.claims

        if (claims.isEmpty()) {
            return BlockingResult(
                blocked = true,
                reason = BlockingReason.NO_CLAIMS,
                blockedClaims = emptyList()
            )
        }

        val unsupported = claims.filter { !it.accepted }

        if (unsupported.isEmpty() && report.accepted) {
            return BlockingResult.passed()
        }

        if (unsupported.isEmpty() && !report.accepted) {
            return BlockingResult(
                blocked = true,
                reason = BlockingReason.UNSUPPORTED_CLAIM,
                blockedClaims = claims.map { it.toBlockedClaim() }
            )
        }

        val allUnsupported = unsupported.size == claims.size
        val reason = if (allUnsupported)
            BlockingReason.UNSUPPORTED_CLAIM
        else
            BlockingReason.MIXED_CLAIMS

        return BlockingResult(
            blocked = true,
            reason = reason,
            blockedClaims = unsupported.map { it.toBlockedClaim() }
        )
    }

    private fun AmarClaimVerification.toBlockedClaim() = BlockedClaim(
        claim = claim,
        supportingEvidence = supportingEvidence,
        opposingEvidence = opposingEvidence,
        matchedEvidence = matchedEvidence
    )
}
