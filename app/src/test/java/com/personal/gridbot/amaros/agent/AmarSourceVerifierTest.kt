package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarSourceVerifierTest {
    private val verifier = AmarSourceVerifier()

    private fun finding(
        authority: Authority,
        uri: String = "https://example.com/source",
        evidence: String = "evidence",
        relevance: Double = 1.0
    ) = ResearchFinding(
        sourceTitle = "source",
        sourceUri = uri,
        evidence = evidence,
        authority = authority,
        relevanceScore = relevance
    )

    @Test fun authority_verified_is_true_when_all_findings_have_known_authority() {
        assertTrue(verifier.authorityVerified(listOf(finding(Authority.PRIMARY), finding(Authority.OFFICIAL))))
    }

    @Test fun authority_verified_is_false_when_any_finding_has_unknown_authority() {
        assertFalse(verifier.authorityVerified(listOf(finding(Authority.PRIMARY), finding(Authority.UNKNOWN))))
    }

    @Test fun authority_verified_is_false_for_empty_findings() {
        assertFalse(verifier.authorityVerified(emptyList()))
    }

    @Test fun authority_verified_is_boolean_owner_state_independent_of_numeric_score() {
        assertTrue(verifier.authorityVerified(listOf(finding(Authority.COMMUNITY))))
        assertTrue(verifier.authorityScore(Authority.COMMUNITY) > 0.0)
        assertFalse(verifier.authorityVerified(listOf(finding(Authority.UNKNOWN))))
    }

    @Test fun verify_accepts_when_authority_strong_and_two_independent_sources() {
        val result = verifier.verify(listOf(
            finding(Authority.PRIMARY, uri = "https://alpha.example/a"),
            finding(Authority.OFFICIAL, uri = "https://beta.example/b")
        ))
        assertTrue(result.accepted)
        assertEquals(2, result.independentSources)
        assertEquals(2, result.totalSources)
    }

    @Test fun verify_rejects_when_only_one_independent_source() {
        val result = verifier.verify(listOf(
            finding(Authority.PRIMARY, uri = "https://alpha.example/a"),
            finding(Authority.PRIMARY, uri = "https://alpha.example/b")
        ))
        assertFalse(result.accepted)
        assertEquals(1, result.independentSources)
    }

    @Test fun verify_rejects_when_relevance_below_minimum() {
        val result = verifier.verify(listOf(
            finding(Authority.PRIMARY, uri = "https://alpha.example/a", relevance = -1.0),
            finding(Authority.OFFICIAL, uri = "https://beta.example/b", relevance = -1.0)
        ))
        assertFalse(result.accepted)
        assertEquals(0, result.totalSources)
    }

    @Test fun verify_rejects_when_evidence_blank() {
        val result = verifier.verify(listOf(
            finding(Authority.PRIMARY, uri = "https://alpha.example/a", evidence = " "),
            finding(Authority.OFFICIAL, uri = "https://beta.example/b")
        ))
        assertEquals(1, result.totalSources)
        assertFalse(result.accepted)
    }

    @Test fun verify_rejects_when_source_uri_blank() {
        val result = verifier.verify(listOf(
            finding(Authority.PRIMARY, uri = "", evidence = "real"),
            finding(Authority.OFFICIAL, uri = "https://beta.example/b")
        ))
        assertEquals(1, result.totalSources)
    }

    @Test fun verify_authority_score_uses_canonical_weights() {
        val result = verifier.verify(listOf(
            finding(Authority.PRIMARY, uri = "https://alpha.example/a"),
            finding(Authority.OFFICIAL, uri = "https://beta.example/b")
        ))
        assertEquals((1.0 + 0.95) / 2.0, result.authorityScore, 0.0001)
    }

    @Test fun verify_confidence_combines_authority_and_independence() {
        val result = verifier.verify(listOf(
            finding(Authority.PRIMARY, uri = "https://alpha.example/a"),
            finding(Authority.OFFICIAL, uri = "https://beta.example/b")
        ))
        assertTrue(result.confidence in 0.0..1.0)
        assertEquals(1.0, result.confidence, 0.05)
    }

    @Test fun verify_rationale_is_populated() {
        val result = verifier.verify(listOf(
            finding(Authority.PRIMARY, uri = "https://alpha.example/a"),
            finding(Authority.OFFICIAL, uri = "https://beta.example/b")
        ))
        assertTrue(result.rationale.contains("authority="))
        assertTrue(result.rationale.contains("independence="))
    }

    @Test fun verify_returns_total_sources_count() {
        val result = verifier.verify(listOf(
            finding(Authority.PRIMARY, uri = "https://alpha.example/a"),
            finding(Authority.OFFICIAL, uri = "https://beta.example/b"),
            finding(Authority.REPUTABLE, uri = "https://gamma.example/c")
        ))
        assertEquals(3, result.totalSources)
        assertEquals(3, result.independentSources)
    }

    @Test fun verify_repeated_evaluation_is_deterministic() {
        val findings = listOf(
            finding(Authority.PRIMARY, uri = "https://alpha.example/a"),
            finding(Authority.OFFICIAL, uri = "https://beta.example/b")
        )
        assertEquals(verifier.verify(findings), verifier.verify(findings))
    }
}
