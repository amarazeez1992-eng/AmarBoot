package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.claim.ClaimVerificationResult
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationState
import com.personal.gridbot.amaros.agent.claim.RejectedClaim
import com.personal.gridbot.amaros.agent.claim.StructuredClaim
import com.personal.gridbot.amaros.agent.claim.VerifiedClaim
import org.junit.Assert.assertEquals
import org.junit.Test

class FinalAnswerClaimCoverageGateTest {

    private val gate = FinalAnswerClaimCoverageGate()

    @Test
    fun all_required_claims_with_non_insufficient_states_are_complete() {
        val claims = listOf(claim("c1"), claim("c2"))
        val result = result(
            verified = listOf(
                verified(claims[0], ClaimVerificationState.SUPPORTED),
                verified(claims[1], ClaimVerificationState.NEUTRAL)
            )
        )

        val actual = gate.evaluate(claims, result)

        assertEquals(FinalAnswerClaimCoverageStatus.COVERAGE_COMPLETE, actual.status)
        assertEquals(emptyList<String>(), actual.uncoveredClaimIds)
    }

    @Test
    fun missing_result_is_incomplete_not_insufficient() {
        val claims = listOf(claim("c1"), claim("c2"))
        val result = result(verified = listOf(verified(claims[0], ClaimVerificationState.SUPPORTED)))

        val actual = gate.evaluate(claims, result)

        assertEquals(FinalAnswerClaimCoverageStatus.COVERAGE_INCOMPLETE, actual.status)
        assertEquals(listOf("c2"), actual.uncoveredClaimIds)
    }

    @Test
    fun insufficient_claim_state_is_incomplete() {
        val claims = listOf(claim("c1"))
        val result = result(rejected = listOf(rejected(claims[0], ClaimVerificationState.INSUFFICIENT)))

        val actual = gate.evaluate(claims, result)

        assertEquals(FinalAnswerClaimCoverageStatus.COVERAGE_INCOMPLETE, actual.status)
        assertEquals(listOf("c1"), actual.uncoveredClaimIds)
    }

    @Test
    fun empty_structured_claims_is_insufficient_data() {
        val actual = gate.evaluate(emptyList(), result())

        assertEquals(FinalAnswerClaimCoverageStatus.INSUFFICIENT_COVERAGE_DATA, actual.status)
        assertEquals(emptyList<String>(), actual.uncoveredClaimIds)
    }

    @Test
    fun upstream_not_ready_is_insufficient_data() {
        val actual = gate.evaluate(listOf(claim("c1")), result(ready = false))

        assertEquals(FinalAnswerClaimCoverageStatus.INSUFFICIENT_COVERAGE_DATA, actual.status)
        assertEquals(emptyList<String>(), actual.uncoveredClaimIds)
    }

    @Test
    fun duplicate_structured_claim_ids_are_insufficient_data() {
        val claims = listOf(claim("c1"), claim("c1"))
        val actual = gate.evaluate(claims, result())

        assertEquals(FinalAnswerClaimCoverageStatus.INSUFFICIENT_COVERAGE_DATA, actual.status)
        assertEquals(emptyList<String>(), actual.uncoveredClaimIds)
    }

    @Test
    fun uncovered_ids_are_deterministically_sorted() {
        val claims = listOf(claim("z"), claim("a"), claim("m"))
        val result = result(
            verified = listOf(verified(claims[0], ClaimVerificationState.SUPPORTED))
        )

        val actual = gate.evaluate(claims, result)

        assertEquals(FinalAnswerClaimCoverageStatus.COVERAGE_INCOMPLETE, actual.status)
        assertEquals(listOf("a", "m"), actual.uncoveredClaimIds)
    }

    private fun claim(id: String) = StructuredClaim(
        id = id,
        text = "$id text",
        subject = "market",
        predicate = "state",
        objectValue = "value"
    )

    private fun verified(claim: StructuredClaim, state: ClaimVerificationState) =
        VerifiedClaim(claim, state, emptyList(), emptyList(), "test")

    private fun rejected(claim: StructuredClaim, state: ClaimVerificationState) =
        RejectedClaim(claim, state, "test")

    private fun result(
        verified: List<VerifiedClaim> = emptyList(),
        rejected: List<RejectedClaim> = emptyList(),
        ready: Boolean = true
    ) = ClaimVerificationResult(verified, rejected, ready)
}
