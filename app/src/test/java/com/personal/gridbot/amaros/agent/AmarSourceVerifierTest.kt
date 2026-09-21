package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarSourceVerifierTest {
    private val verifier = AmarSourceVerifier()

    private fun finding(authority: Authority) = ResearchFinding(
        sourceTitle = "source",
        sourceUri = "https://example.com/source",
        evidence = "evidence",
        authority = authority
    )

    @Test
    fun authority_verified_is_true_when_all_findings_have_known_authority() {
        assertTrue(verifier.authorityVerified(listOf(finding(Authority.PRIMARY), finding(Authority.OFFICIAL))))
    }

    @Test
    fun authority_verified_is_false_when_any_finding_has_unknown_authority() {
        assertFalse(verifier.authorityVerified(listOf(finding(Authority.PRIMARY), finding(Authority.UNKNOWN))))
    }

    @Test
    fun authority_verified_is_false_for_empty_findings() {
        assertFalse(verifier.authorityVerified(emptyList()))
    }

    @Test
    fun authority_verified_is_boolean_owner_state_independent_of_numeric_score() {
        assertTrue(verifier.authorityVerified(listOf(finding(Authority.COMMUNITY))))
        assertTrue(verifier.authorityScore(Authority.COMMUNITY) > 0.0)
        assertFalse(verifier.authorityVerified(listOf(finding(Authority.UNKNOWN))))
    }
}
