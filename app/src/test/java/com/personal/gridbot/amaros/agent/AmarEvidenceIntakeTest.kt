package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceIntakeTest {
    private val intake = AmarEvidenceIntake()

    private fun finding(sourceUri: String, evidence: String) = ResearchFinding(
        sourceTitle = "source",
        sourceUri = sourceUri,
        evidence = evidence,
        retrievedAtEpochMs = 1_000L,
        fingerprint = "test-fingerprint"
    )

    @Test
    fun admits_structurally_complete_findings_in_input_order() {
        val first = finding("https://a.example/1", "first")
        val second = finding("https://b.example/2", "second")

        val result = intake.admit(listOf(first, second))

        assertEquals(2, result.receivedCount)
        assertEquals(2, result.admittedCount)
        assertEquals(0, result.rejectedCount)
        assertEquals(listOf(first, second), result.admitted)
        assertTrue(result.rejected.isEmpty())
    }

    @Test
    fun rejects_blank_source_without_evaluating_downstream_quality() {
        val invalid = finding("   ", "evidence")
        val valid = finding("https://a.example/1", "evidence")

        val result = intake.admit(listOf(invalid, valid))

        assertEquals(1, result.admittedCount)
        assertEquals(1, result.rejectedCount)
        assertSame(invalid, result.rejected.single())
        assertSame(valid, result.admitted.single())
    }

    @Test
    fun rejects_blank_evidence_without_mutating_the_candidate() {
        val invalid = finding("https://a.example/1", "  ")

        val result = intake.admit(listOf(invalid))

        assertEquals(0, result.admittedCount)
        assertEquals(1, result.rejectedCount)
        assertSame(invalid, result.rejected.single())
    }

    @Test
    fun does_not_apply_authority_freshness_or_fingerprint_rules_at_intake() {
        val candidate = finding("https://a.example/1", "evidence").copy(
            authority = Authority.UNKNOWN,
            retrievedAtEpochMs = Long.MAX_VALUE,
            fingerprint = "not-the-integrity-fingerprint"
        )

        val result = intake.admit(listOf(candidate))

        assertEquals(1, result.admittedCount)
        assertSame(candidate, result.admitted.single())
    }

    @Test
    fun empty_input_is_a_valid_empty_intake() {
        val result = intake.admit(emptyList())

        assertEquals(0, result.receivedCount)
        assertEquals(0, result.admittedCount)
        assertEquals(0, result.rejectedCount)
    }
}
