package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.claim.ClaimVerificationResult
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationState
import com.personal.gridbot.amaros.agent.claim.StructuredClaim

/**
 * Addition 7 — deterministic final-answer claim coverage detector.
 *
 * Flag-only: this component does not block, calculate confidence, or
 * re-extract/re-match claims. It consumes existing Point 15 results.
 */
class FinalAnswerClaimCoverageGate {

    fun evaluate(
        structuredClaims: List<StructuredClaim>,
        result: ClaimVerificationResult
    ): FinalAnswerClaimCoverageResult {
        if (structuredClaims.isEmpty() || !result.isDownstreamReady) {
            return FinalAnswerClaimCoverageResult(
                status = FinalAnswerClaimCoverageStatus.INSUFFICIENT_COVERAGE_DATA,
                uncoveredClaimIds = emptyList()
            )
        }

        val ids = structuredClaims.map { it.id }
        if (ids.toSet().size != ids.size) {
            return FinalAnswerClaimCoverageResult(
                status = FinalAnswerClaimCoverageStatus.INSUFFICIENT_COVERAGE_DATA,
                uncoveredClaimIds = emptyList()
            )
        }

        val verifiedById = result.verifiedClaims.associateBy { it.claim.id }
        val rejectedById = result.rejectedClaims.associateBy { it.claim.id }
        val uncovered = structuredClaims
            .filter { claim ->
                val state = verifiedById[claim.id]?.state ?: rejectedById[claim.id]?.state
                state == null || state == ClaimVerificationState.INSUFFICIENT
            }
            .map { it.id }
            .sorted()

        return FinalAnswerClaimCoverageResult(
            status = if (uncovered.isEmpty()) {
                FinalAnswerClaimCoverageStatus.COVERAGE_COMPLETE
            } else {
                FinalAnswerClaimCoverageStatus.COVERAGE_INCOMPLETE
            },
            uncoveredClaimIds = uncovered
        )
    }
}

enum class FinalAnswerClaimCoverageStatus {
    COVERAGE_COMPLETE,
    COVERAGE_INCOMPLETE,
    INSUFFICIENT_COVERAGE_DATA
}

data class FinalAnswerClaimCoverageResult(
    val status: FinalAnswerClaimCoverageStatus,
    val uncoveredClaimIds: List<String>
)
