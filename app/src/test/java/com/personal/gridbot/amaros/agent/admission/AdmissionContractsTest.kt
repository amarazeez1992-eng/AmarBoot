package com.personal.gridbot.amaros.agent.admission

import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdmissionContractsTest {

    private fun finding(id: String = "default") = ResearchFinding(
        sourceTitle = "Test source",
        sourceUri = "https://example.test/$id",
        evidence = "Test evidence",
        authority = Authority.UNKNOWN,
        stance = EvidenceStance.UNKNOWN
    )

    @Test
    fun admission_state_contains_only_admitted_and_rejected() {
        assertEquals(
            setOf(AdmissionState.ADMITTED, AdmissionState.REJECTED),
            AdmissionState.entries.toSet()
        )
        assertEquals(2, AdmissionState.entries.size)
    }

    @Test
    fun admitted_finding_accepts_boundary_relevance_scores() {
        AdmittedFinding(finding(), AdmissionState.REJECTED, 0.0, "test")
        AdmittedFinding(finding("one"), AdmissionState.ADMITTED, 1.0, "test")
    }

    @Test
    fun admitted_finding_rejects_invalid_relevance_scores() {
        assertFails { AdmittedFinding(finding(), AdmissionState.REJECTED, -0.01, "test") }
        assertFails { AdmittedFinding(finding("one"), AdmissionState.REJECTED, 1.01, "test") }
        assertFails { AdmittedFinding(finding("two"), AdmissionState.REJECTED, Double.NaN, "test") }
        assertFails {
            AdmittedFinding(
                finding("three"),
                AdmissionState.REJECTED,
                Double.POSITIVE_INFINITY,
                "test"
            )
        }
    }

    @Test
    fun admission_result_enforces_state_invariants() {
        val admitted = AdmittedFinding(finding(), AdmissionState.ADMITTED, 0.8, "matched")
        val rejected = AdmittedFinding(
            finding("rejected"),
            AdmissionState.REJECTED,
            0.2,
            "missing required facet"
        )
        val result = AdmissionResult(admitted = listOf(admitted), rejected = listOf(rejected))
        assertTrue(result.admitted.all { it.state == AdmissionState.ADMITTED })
        assertTrue(result.rejected.all { it.state == AdmissionState.REJECTED })
    }

    @Test
    fun admission_result_rejects_wrong_state_in_either_partition() {
        val rejectedState = AdmittedFinding(finding(), AdmissionState.REJECTED, 0.2, "rejected")
        assertFails {
            AdmissionResult(admitted = listOf(rejectedState), rejected = emptyList())
        }

        val admittedState = AdmittedFinding(
            finding("admitted"),
            AdmissionState.ADMITTED,
            0.8,
            "admitted"
        )
        assertFails {
            AdmissionResult(admitted = emptyList(), rejected = listOf(admittedState))
        }
    }

    @Test
    fun empty_admission_result_is_empty_on_both_sides() {
        val result = AdmissionResult(emptyList(), emptyList())
        assertTrue(result.admitted.isEmpty())
        assertTrue(result.rejected.isEmpty())
    }

    private fun assertFails(block: () -> Unit) {
        try {
            block()
            throw AssertionError("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected contract violation.
        }
    }
}
