package com.personal.gridbot.amaros.agent.admission

import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFailsWith
import org.junit.Test

class AdmissionContractsTest {
    private fun finding(id: String = "https://example.test/$id") = ResearchFinding(
        sourceTitle = "Test source",
        sourceUri = id,
        evidence = "Test evidence",
        authority = Authority.UNKNOWN,
        stance = EvidenceStance.UNKNOWN
    )

    @Test fun admission_state_contains_only_admitted_and_rejected() {
        assertEquals(setOf(AdmissionState.ADMITTED, AdmissionState.REJECTED), AdmissionState.entries.toSet())
        assertEquals(2, AdmissionState.entries.size)
    }

    @Test fun admitted_finding_accepts_boundary_relevance_scores() {
        AdmittedFinding(finding(), AdmissionState.REJECTED, 0.0, "test")
        AdmittedFinding(finding("https://example.test/one"), AdmissionState.ADMITTED, 1.0, "test")
    }

    @Test fun admitted_finding_rejects_invalid_relevance_scores() {
        assertFailsWith<IllegalArgumentException> { AdmittedFinding(finding(), AdmissionState.REJECTED, -0.01, "test") }
        assertFailsWith<IllegalArgumentException> { AdmittedFinding(finding("https://example.test/one"), AdmissionState.REJECTED, 1.01, "test") }
        assertFailsWith<IllegalArgumentException> { AdmittedFinding(finding("https://example.test/two"), AdmissionState.REJECTED, Double.NaN, "test") }
        assertFailsWith<IllegalArgumentException> { AdmittedFinding(finding("https://example.test/three"), AdmissionState.REJECTED, Double.POSITIVE_INFINITY, "test") }
    }

    @Test fun admission_reason_must_be_non_blank() {
        assertFailsWith<IllegalArgumentException> { AdmittedFinding(finding(), AdmissionState.REJECTED, 0.2, "") }
        assertFailsWith<IllegalArgumentException> { AdmittedFinding(finding("https://example.test/blank"), AdmissionState.REJECTED, 0.2, "   ") }
    }

    @Test fun admission_result_enforces_state_invariants() {
        val admitted = AdmittedFinding(finding(), AdmissionState.ADMITTED, 0.8, "matched")
        val rejected = AdmittedFinding(finding("https://example.test/rejected"), AdmissionState.REJECTED, 0.2, "missing required facet")
        val result = AdmissionResult(listOf(admitted), listOf(rejected))
        assertTrue(result.admitted.all { it.state == AdmissionState.ADMITTED })
        assertTrue(result.rejected.all { it.state == AdmissionState.REJECTED })
    }

    @Test fun admission_result_rejects_wrong_state_in_either_partition() {
        val rejectedState = AdmittedFinding(finding(), AdmissionState.REJECTED, 0.2, "rejected")
        assertFailsWith<IllegalArgumentException> { AdmissionResult(listOf(rejectedState), emptyList()) }
        val admittedState = AdmittedFinding(finding("https://example.test/admitted"), AdmissionState.ADMITTED, 0.8, "admitted")
        assertFailsWith<IllegalArgumentException> { AdmissionResult(emptyList(), listOf(admittedState)) }
    }

    @Test fun empty_admission_result_is_empty_on_both_sides() {
        val result = AdmissionResult(emptyList(), emptyList())
        assertTrue(result.admitted.isEmpty())
        assertTrue(result.rejected.isEmpty())
    }

    @Test fun admission_contract_is_deterministic_across_repeated_state_construction() {
        repeat(10) {
            val admitted = AdmittedFinding(finding(), AdmissionState.ADMITTED, 0.8, "matched")
            val rejected = AdmittedFinding(finding("https://example.test/rejected"), AdmissionState.REJECTED, 0.2, "missing required facet")
            val result = AdmissionResult(listOf(admitted), listOf(rejected))
            assertEquals(listOf(0.8), result.admitted.map { it.relevanceScore })
            assertEquals(listOf(0.2), result.rejected.map { it.relevanceScore })
            assertEquals(listOf("matched"), result.admitted.map { it.admissionReason })
            assertEquals(listOf("missing required facet"), result.rejected.map { it.admissionReason })
        }
    }
}
