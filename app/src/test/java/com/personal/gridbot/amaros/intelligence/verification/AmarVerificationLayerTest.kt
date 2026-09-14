package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarVerificationLayerTest {
    private fun finding(
        title: String,
        uri: String,
        evidence: String,
        stance: EvidenceStance,
        retrievedAt: Long = 1_000L
    ) = ResearchFinding(
        sourceTitle = title,
        sourceUri = uri,
        evidence = evidence,
        authority = Authority.OFFICIAL,
        stance = stance,
        retrievedAtEpochMs = retrievedAt
    )

    @Test
    fun verifies_clean_evidence_and_builds_provenance_chain() {
        val findings = listOf(
            finding("Official", "https://official.example/a", "The policy requires verification before release.", EvidenceStance.SUPPORTS),
            finding("Independent", "https://independent.example/b", "The policy requires verification before release.", EvidenceStance.SUPPORTS, 2_000L)
        )
        val report = AmarVerificationLayer().verify(
            "The policy requires verification before release.",
            findings,
            nowEpochMs = 2_000L
        )
        assertEquals(AmarVerificationStatus.VERIFIED, report.status)
        assertTrue(report.score >= 0.70)
        assertEquals(2, report.provenance.size)
        assertEquals("GENESIS", report.provenance.first().previousHash)
        assertNotEquals(report.provenance.first().chainHash, report.provenance.last().chainHash)
    }

    @Test
    fun evidence_only_verification_does_not_fabricate_a_claim_and_can_verify_clean_sources() {
        val findings = listOf(
            finding("Official", "https://official.example/a", "Evidence supports the release gate.", EvidenceStance.SUPPORTS),
            finding("Independent", "https://independent.example/b", "Evidence supports the release gate.", EvidenceStance.SUPPORTS)
        )
        val report = AmarVerificationLayer().verifyEvidenceOnly(findings, nowEpochMs = 2_000L)
        assertEquals(AmarVerificationStatus.VERIFIED, report.status)
        assertTrue(report.claimVerification.claims.isEmpty())
        assertEquals(2, report.usableEvidenceCount)
    }

    @Test
    fun opposing_evidence_is_detected_and_prevents_verified_status() {
        val findings = listOf(
            finding("Support", "https://a.example/support", "The change is safe and approved for release.", EvidenceStance.SUPPORTS),
            finding("Oppose", "https://b.example/oppose", "The change is unsafe and should not be released.", EvidenceStance.OPPOSES)
        )
        val report = AmarVerificationLayer().verify(
            "The change is safe and approved for release.",
            findings,
            nowEpochMs = 2_000L
        )
        assertTrue(report.conflicts.isNotEmpty())
        assertNotEquals(AmarVerificationStatus.VERIFIED, report.status)
    }

    @Test
    fun malformed_source_or_empty_evidence_cannot_be_verified() {
        val findings = listOf(
            finding("Bad", "not-a-uri", "", EvidenceStance.UNKNOWN)
        )
        val report = AmarVerificationLayer().verify("A claim that cannot be verified from evidence.", findings)
        assertEquals(AmarVerificationStatus.UNVERIFIABLE, report.status)
        assertEquals(0, report.usableEvidenceCount)
        assertEquals(1, report.invalidEvidenceCount)
    }

    @Test
    fun provenance_is_deterministic_for_identical_inputs() {
        val findings = listOf(
            finding("A", "https://a.example/a", "Evidence one is reproducible.", EvidenceStance.SUPPORTS, 10L),
            finding("B", "https://b.example/b", "Evidence two is reproducible.", EvidenceStance.SUPPORTS, 20L)
        )
        val first = AmarProvenanceChain.build(findings)
        val second = AmarProvenanceChain.build(findings)
        assertEquals(first, second)
    }
}
