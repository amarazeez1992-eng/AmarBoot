package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnsupportedClaimBlockingTest {

    private fun verification(
        claim: String,
        accepted: Boolean,
        supportingEvidence: Int = 0,
        opposingEvidence: Int = 0,
        matchedEvidence: Int = 0
    ) = AmarClaimVerification(
        claim = claim,
        supportingEvidence = supportingEvidence,
        opposingEvidence = opposingEvidence,
        accepted = accepted,
        matchedEvidence = matchedEvidence
    )

    @Test
    fun all_claims_accepted_returns_passed() {
        val report = AmarClaimVerificationReport(
            claims = listOf(verification("supported claim", true, 1, 0, 1)),
            accepted = true
        )
        val result = UnsupportedClaimBlocking.evaluate(report)
        assertFalse(result.blocked)
        assertEquals(BlockingReason.VERIFICATION_ACCEPTED, result.reason)
        assertTrue(result.blockedClaims.isEmpty())
    }

    @Test
    fun one_unsupported_claim_returns_unsupported_reason() {
        val report = AmarClaimVerificationReport(
            claims = listOf(verification("unsupported claim", false, 0, 0, 0)),
            accepted = false
        )
        val result = UnsupportedClaimBlocking.evaluate(report)
        assertTrue(result.blocked)
        assertEquals(BlockingReason.UNSUPPORTED_CLAIM, result.reason)
        assertEquals(listOf("unsupported claim"), result.blockedClaims.map { it.claim })
    }

    @Test
    fun multiple_unsupported_claims_are_all_audited() {
        val report = AmarClaimVerificationReport(
            claims = listOf(
                verification("claim one", false, 0, 1, 1),
                verification("claim two", false, 0, 0, 0)
            ),
            accepted = false
        )
        val result = UnsupportedClaimBlocking.evaluate(report)
        assertTrue(result.blocked)
        assertEquals(BlockingReason.UNSUPPORTED_CLAIM, result.reason)
        assertEquals(2, result.blockedClaims.size)
        assertEquals(1, result.blockedClaims[0].opposingEvidence)
        assertEquals(1, result.blockedClaims[0].matchedEvidence)
    }

    @Test
    fun empty_claims_are_blocked() {
        val report = AmarClaimVerificationReport(emptyList(), accepted = false)
        val result = UnsupportedClaimBlocking.evaluate(report)
        assertTrue(result.blocked)
        assertEquals(BlockingReason.NO_CLAIMS, result.reason)
        assertTrue(result.blockedClaims.isEmpty())
    }

    @Test
    fun mixed_claims_returns_mixed_reason_and_only_unsupported_claims() {
        val report = AmarClaimVerificationReport(
            claims = listOf(
                verification("accepted claim", true, 1, 0, 1),
                verification("unsupported claim", false, 0, 1, 1)
            ),
            accepted = false
        )
        val result = UnsupportedClaimBlocking.evaluate(report)
        assertTrue(result.blocked)
        assertEquals(BlockingReason.MIXED_CLAIMS, result.reason)
        assertEquals(listOf("unsupported claim"), result.blockedClaims.map { it.claim })
    }

    @Test
    fun does_not_reverify_or_recalculate_evidence() {
        val report = AmarClaimVerificationReport(
            claims = listOf(verification("claim", false, 7, 3, 9)),
            accepted = false
        )
        val result = UnsupportedClaimBlocking.evaluate(report)
        assertEquals(7, result.blockedClaims.single().supportingEvidence)
        assertEquals(3, result.blockedClaims.single().opposingEvidence)
        assertEquals(9, result.blockedClaims.single().matchedEvidence)
    }

    @Test
    fun deterministic_for_identical_input() {
        val report = AmarClaimVerificationReport(
            claims = listOf(
                verification("claim one", false, 1, 0, 2),
                verification("claim two", true, 2, 0, 2)
            ),
            accepted = false
        )
        val first = UnsupportedClaimBlocking.evaluate(report)
        val second = UnsupportedClaimBlocking.evaluate(report)
        assertEquals(first, second)
    }

    @Test
    fun regression_verification_engine_contract_remains_usable() {
        val engine = AmarClaimVerificationEngine()
        val report = engine.verify(
            "Gold is a precious metal traded in financial markets.",
            emptyList()
        )
        assertFalse(report.accepted)
        assertTrue(report.claims.isNotEmpty())

        val blocking = UnsupportedClaimBlocking.evaluate(report)
        assertTrue(blocking.blocked)
        assertEquals(BlockingReason.UNSUPPORTED_CLAIM, blocking.reason)
    }
}
