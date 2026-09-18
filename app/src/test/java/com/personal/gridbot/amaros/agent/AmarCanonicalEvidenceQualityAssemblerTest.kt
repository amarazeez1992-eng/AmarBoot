package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarCanonicalEvidenceQualityAssemblerTest {
    private val assembler = AmarCanonicalEvidenceQualityAssembler()

    private fun finding(
        uri: String,
        evidence: String,
        authority: Authority = Authority.PRIMARY,
        retrievedAt: Long = 1_000L,
        fingerprint: String = AmarEvidence.fingerprintOf("$uri|$evidence")
    ) = ResearchFinding(
        sourceTitle = "source",
        sourceUri = uri,
        evidence = evidence,
        authority = authority,
        retrievedAtEpochMs = retrievedAt,
        fingerprint = fingerprint
    )

    @Test
    fun assembler_consumes_existing_point_contracts_without_reimplementing_them() {
        val a = finding("https://a.example/x", "gold trend rising")
        val b = finding("https://b.example/x", "gold momentum rising")
        val report = assembler.assemble(listOf(a, b), nowEpochMs = 1_500L)

        assertEquals(2, report.items.size)
        assertEquals(2, report.point5Verification.independentSources)
        assertTrue(report.point7FingerprintIntegrity.intact)
        assertTrue(report.point9Uniqueness.unique)
        assertEquals(1.0, report.aggregateScore, 0.0)
        assertEquals(listOf(1.0, 1.0), report.itemScores)
    }

    @Test
    fun unknown_authority_blocks_point_ten_but_preserves_owner_state() {
        val item = finding("https://a.example/x", "gold trend", authority = Authority.UNKNOWN)
        val report = assembler.assemble(listOf(item), nowEpochMs = 1_500L)

        assertFalse(report.items.single().authorityVerified)
        assertEquals(0.0, report.aggregateScore, 0.0)
        assertEquals(0.0, report.itemScores.single(), 0.0)
    }

    @Test
    fun future_evidence_is_blocked_without_repairing_freshness() {
        val item = finding("https://a.example/x", "future gold claim", retrievedAt = 2_000L)
        val report = assembler.assemble(listOf(item), nowEpochMs = 1_500L)

        assertFalse(report.items.single().freshnessVerified)
        assertEquals(0.0, report.items.single().freshnessScore, 0.0)
        assertEquals(0.0, report.aggregateScore, 0.0)
    }

    @Test
    fun duplicate_and_fingerprint_failures_remain_explicit_audit_facts() {
        val a = finding("https://a.example/x", "same evidence", retrievedAt = 1_000L)
        val b = finding(
            "https://b.example/x",
            "same evidence",
            retrievedAt = 1_000L,
            fingerprint = "tampered"
        )
        val report = assembler.assemble(listOf(a, b), nowEpochMs = 1_500L)

        assertTrue(report.point6Duplicates.hasDuplicates)
        assertEquals(1, report.point6Duplicates.duplicateGroupCount)
        assertFalse(report.point7FingerprintIntegrity.intact)
        assertTrue(report.point9Uniqueness.unique)
        assertEquals(0.0, report.aggregateScore, 0.0)
    }

    @Test
    fun empty_input_is_fail_closed_and_deterministic() {
        val first = assembler.assemble(emptyList(), 1_500L)
        val second = assembler.assemble(emptyList(), 1_500L)

        assertEquals(0.0, first.aggregateScore, 0.0)
        assertEquals(first, second)
        assertEquals(0, first.point5Verification.totalSources)
    }
}
