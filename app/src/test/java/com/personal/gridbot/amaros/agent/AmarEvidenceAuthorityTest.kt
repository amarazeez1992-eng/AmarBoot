package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceAuthorityTest {

    private fun createAuthority() = AmarEvidenceAuthority(clock = { 42L })

    private fun finding(
        uri: String,
        evidence: String = "verified evidence",
        authority: Authority = Authority.PRIMARY
    ) = ResearchFinding(
        sourceTitle = "source",
        sourceUri = uri,
        evidence = evidence,
        authority = authority
    )

    @Test fun evaluate_returns_verification() {
        val r = createAuthority().evaluate(listOf(
            finding("https://alpha.example/a"),
            finding("https://beta.example/b")
        ))
        assertTrue(r.verification.accepted)
    }

    @Test fun evaluate_detects_duplicates() {
        val r = createAuthority().evaluate(listOf(
            finding("https://alpha.example/a", "same"),
            finding("https://beta.example/b", "same")
        ))
        assertTrue(r.duplicates.hasDuplicates)
        assertFalse(r.accepted)
    }

    @Test fun evaluate_accepts_clean_findings() {
        val r = createAuthority().evaluate(listOf(
            finding("https://alpha.example/a", "evidence A"),
            finding("https://beta.example/b", "evidence B")
        ))
        assertTrue(r.accepted)
    }

    @Test fun evaluate_rejects_duplicate_findings() {
        val r = createAuthority().evaluate(listOf(
            finding("https://alpha.example/a", "same evidence"),
            finding("https://beta.example/b", "same evidence")
        ))
        assertFalse(r.accepted)
    }

    @Test fun evaluate_returns_findings_count() {
        val r = createAuthority().evaluate(listOf(
            finding("https://alpha.example/a"),
            finding("https://beta.example/b"),
            finding("https://gamma.example/c")
        ))
        assertEquals(3, r.findingsCount)
    }

    @Test fun evaluate_empty_findings_rejected() {
        val r = createAuthority().evaluate(emptyList())
        assertFalse(r.accepted)
        assertEquals(0, r.findingsCount)
    }

    @Test fun evaluate_records_timestamp() {
        val r = createAuthority().evaluate(listOf(finding("https://alpha.example/a")))
        assertEquals(42L, r.evaluatedAtEpochMs)
    }

    @Test fun evaluate_rationale_populated() {
        val r = createAuthority().evaluate(listOf(
            finding("https://alpha.example/a"),
            finding("https://beta.example/b")
        ))
        assertTrue(r.rationale.contains("verification="))
    }

    @Test fun isIndependent_delegates_to_verifier() {
        val auth = createAuthority()
        val findings = listOf(
            finding("https://alpha.example/a"),
            finding("https://beta.example/b")
        )
        assertTrue(auth.isIndependent(findings.first(), findings))
    }

    @Test fun authorityVerified_delegates() {
        val auth = createAuthority()
        assertTrue(auth.authorityVerified(listOf(finding("https://alpha.example/a"))))
        assertFalse(auth.authorityVerified(listOf(finding("https://alpha.example/a", authority = Authority.UNKNOWN))))
    }

    @Test fun authorityScore_matches_verifier() {
        val auth = createAuthority()
        assertEquals(1.0, auth.authorityScore(Authority.PRIMARY), 0.0)
        assertEquals(0.95, auth.authorityScore(Authority.OFFICIAL), 0.0)
    }

    @Test fun evaluate_repeated_is_deterministic() {
        val auth = createAuthority()
        val findings = listOf(finding("https://alpha.example/a"), finding("https://beta.example/b"))
        assertEquals(auth.evaluate(findings), auth.evaluate(findings))
    }
}
