package com.personal.gridbot.amaros.agent.conflict

import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.status.ConflictState
import com.personal.gridbot.amaros.intelligence.verification.AmarConflict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceConflictAwarenessTest {
    private val awareness = AmarEvidenceConflictAwareness()

    private fun finding(
        evidence: String,
        fingerprint: String = "fp-" + evidence.hashCode()
    ) = ResearchFinding(
        sourceTitle = "source",
        sourceUri = "https://example.com/" + fingerprint,
        evidence = evidence,
        fingerprint = fingerprint
    )

    private fun conflict(
        supporting: List<String> = listOf("support-fp"),
        opposing: List<String> = listOf("oppose-fp"),
        reason: String = "test conflict"
    ) = AmarConflict(supporting, opposing, reason)

    @Test fun empty_input_returns_no_conflict() {
        val result = awareness.evaluate(AmarEvidenceConflictAwarenessInput(emptyList(), emptyList()))
        assertEquals(ConflictState.NO_CONFLICT, result.globalConflictState)
        assertEquals(ConflictAwarenessReason.NO_CONFLICTS_DETECTED, result.reason)
        assertTrue(result.isDownstreamReady)
    }

    @Test fun single_evidence_no_conflict() {
        val result = awareness.evaluate(AmarEvidenceConflictAwarenessInput(listOf(finding("one")), emptyList()))
        assertEquals(ConflictState.NO_CONFLICT, result.globalConflictState)
    }

    @Test fun multiple_evidences_no_conflict() {
        val result = awareness.evaluate(AmarEvidenceConflictAwarenessInput(
            listOf(finding("one"), finding("two")), emptyList()))
        assertEquals(ConflictState.NO_CONFLICT, result.globalConflictState)
    }

    @Test fun two_conflicting_evidences_returns_conflicted() {
        val result = awareness.evaluate(AmarEvidenceConflictAwarenessInput(
            listOf(finding("support"), finding("oppose")), listOf(conflict())))
        assertEquals(ConflictState.CONFLICTED, result.globalConflictState)
        assertTrue(result.isDownstreamReady)
    }

    @Test fun conflicted_state_includes_fingerprints() {
        val result = awareness.evaluate(AmarEvidenceConflictAwarenessInput(
            listOf(finding("x")), listOf(conflict(listOf("s1", "s2"), listOf("o1")))))
        assertEquals(setOf("s1", "s2", "o1"), result.conflictingFingerprints)
    }

    @Test fun conflict_details_preserved() {
        val supplied = conflict(reason = "preserve this")
        val result = awareness.evaluate(AmarEvidenceConflictAwarenessInput(listOf(finding("x")), listOf(supplied)))
        assertEquals(listOf(supplied), result.conflictDetails)
        assertEquals("preserve this", result.conflictDetails.single().reason)
    }

    @Test fun no_conflict_when_detector_returns_empty() {
        val result = awareness.evaluate(AmarEvidenceConflictAwarenessInput(listOf(finding("x")), emptyList()))
        assertEquals(ConflictState.NO_CONFLICT, result.globalConflictState)
        assertEquals(ConflictAwarenessReason.NO_CONFLICTS_DETECTED, result.reason)
    }

    @Test fun conflicted_when_detector_returns_any() {
        val result = awareness.evaluate(AmarEvidenceConflictAwarenessInput(listOf(finding("x")), listOf(conflict())))
        assertEquals(ConflictState.CONFLICTED, result.globalConflictState)
    }

    @Test fun is_deterministic() {
        val input = AmarEvidenceConflictAwarenessInput(listOf(finding("x")), listOf(conflict()))
        assertEquals(awareness.evaluate(input), awareness.evaluate(input))
    }

    @Test fun is_stateless() {
        val first = awareness.evaluate(AmarEvidenceConflictAwarenessInput(listOf(finding("x")), listOf(conflict())))
        val second = awareness.evaluate(AmarEvidenceConflictAwarenessInput(listOf(finding("y")), emptyList()))
        assertEquals(ConflictState.CONFLICTED, first.globalConflictState)
        assertEquals(ConflictState.NO_CONFLICT, second.globalConflictState)
        assertNotSame(first, second)
    }

    @Test fun does_not_recalculate_conflict() {
        val supplied = conflict(listOf("authoritative-support"), listOf("authoritative-opposition"), "supplied detector result")
        val result = awareness.evaluate(AmarEvidenceConflictAwarenessInput(listOf(finding("unrelated evidence")), listOf(supplied)))
        assertEquals(setOf("authoritative-support", "authoritative-opposition"), result.conflictingFingerprints)
        assertEquals("supplied detector result", result.conflictDetails.single().reason)
    }

    @Test fun does_not_resolve_conflict() {
        val supplied = conflict()
        val result = awareness.evaluate(AmarEvidenceConflictAwarenessInput(listOf(finding("x")), listOf(supplied)))
        assertEquals(listOf(supplied), result.conflictDetails)
        assertEquals(ConflictState.CONFLICTED, result.globalConflictState)
    }

    @Test fun fail_closed_when_detector_unavailable() {
        val result = awareness.evaluate(AmarEvidenceConflictAwarenessInput(listOf(finding("x")), null))
        assertEquals(ConflictState.NOT_AVAILABLE, result.globalConflictState)
        assertEquals(ConflictAwarenessReason.DETECTOR_UNAVAILABLE, result.reason)
        assertFalse(result.isDownstreamReady)
    }

    @Test fun preserves_evidence() {
        val findings = listOf(finding("evidence-a"), finding("evidence-b"))
        val original = findings.toList()
        awareness.evaluate(AmarEvidenceConflictAwarenessInput(findings, emptyList()))
        assertEquals(original, findings)
    }

    @Test fun result_partitions_correctly() {
        val noConflict = awareness.evaluate(AmarEvidenceConflictAwarenessInput(listOf(finding("x")), emptyList()))
        val conflicted = awareness.evaluate(AmarEvidenceConflictAwarenessInput(listOf(finding("x")), listOf(conflict())))
        val unavailable = awareness.evaluate(AmarEvidenceConflictAwarenessInput(listOf(finding("x")), null))
        assertEquals(ConflictState.NO_CONFLICT, noConflict.globalConflictState)
        assertEquals(ConflictState.CONFLICTED, conflicted.globalConflictState)
        assertEquals(ConflictState.NOT_AVAILABLE, unavailable.globalConflictState)
        assertTrue(noConflict.isDownstreamReady)
        assertTrue(conflicted.isDownstreamReady)
        assertFalse(unavailable.isDownstreamReady)
    }
}
