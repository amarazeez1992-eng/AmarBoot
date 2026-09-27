package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceGateTest {

    private fun gate() = AmarEvidenceGate(clock = { 42L })

    private fun finding(uri: String, evidence: String = "verified evidence", authority: Authority = Authority.PRIMARY) =
        ResearchFinding(sourceTitle = "source", sourceUri = uri, evidence = evidence, authority = authority)

    @Test fun evaluate_returns_verification() {
        val r = gate().evaluate(listOf(finding("https://alpha.example/a"), finding("https://beta.example/b")))
        assertTrue(r.verification.accepted)
    }

    @Test fun evaluate_detects_duplicates() {
        val r = gate().evaluate(listOf(finding("https://a.example/x", "same"), finding("https://b.example/y", "same")))
        assertTrue(r.duplicates.hasDuplicates)
        assertFalse(r.accepted)
    }

    @Test fun evaluate_accepts_clean_findings() {
        val r = gate().evaluate(listOf(finding("https://a.example/x", "A"), finding("https://b.example/y", "B")))
        assertTrue(r.accepted)
    }

    @Test fun evaluate_rejects_duplicate_findings() {
        val r = gate().evaluate(listOf(finding("https://a.example/x", "same"), finding("https://b.example/y", "same")))
        assertFalse(r.accepted)
    }

    @Test fun evaluate_returns_findings_count() {
        val r = gate().evaluate(listOf(finding("https://a.example/x"), finding("https://b.example/y"), finding("https://c.example/z")))
        assertEquals(3, r.findingsCount)
    }

    @Test fun evaluate_empty_findings_rejected() {
        val r = gate().evaluate(emptyList())
        assertFalse(r.accepted)
        assertEquals(0, r.findingsCount)
    }

    @Test fun evaluate_records_timestamp() {
        val r = gate().evaluate(listOf(finding("https://a.example/x")))
        assertEquals(42L, r.evaluatedAtEpochMs)
    }

    @Test fun evaluate_rationale_populated() {
        val r = gate().evaluate(listOf(finding("https://a.example/x"), finding("https://b.example/y")))
        assertTrue(r.rationale.contains("verification="))
    }

    @Test fun isIndependent_delegates() {
        val g = gate()
        val f = listOf(finding("https://a.example/x"), finding("https://b.example/y"))
        assertTrue(g.isIndependent(f.first(), f))
    }

    @Test fun authorityVerified_delegates() {
        val g = gate()
        assertTrue(g.authorityVerified(listOf(finding("https://a.example/x"))))
        assertFalse(g.authorityVerified(listOf(finding("https://a.example/x", authority = Authority.UNKNOWN))))
    }

    @Test fun authorityScore_matches_verifier() {
        val g = gate()
        assertEquals(1.0, g.authorityScore(Authority.PRIMARY), 0.0)
        assertEquals(0.95, g.authorityScore(Authority.OFFICIAL), 0.0)
    }

    @Test fun evaluate_repeated_is_deterministic() {
        val g = gate()
        val f = listOf(finding("https://a.example/x"), finding("https://b.example/y"))
        assertEquals(g.evaluate(f), g.evaluate(f))
    }
}
