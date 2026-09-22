package com.personal.gridbot.amaros.agent.correlation

import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate
import com.personal.gridbot.amaros.agent.conflict.ConflictAwarenessReason
import com.personal.gridbot.amaros.agent.conflict.EvidenceConflictAwarenessResult
import com.personal.gridbot.amaros.agent.deterministic.CanonicalEvidence
import com.personal.gridbot.amaros.agent.deterministic.DeterministicEvidenceResult
import com.personal.gridbot.amaros.agent.deterministic.DeterministicHandlingReason
import com.personal.gridbot.amaros.agent.protection.ProtectedEvidence
import com.personal.gridbot.amaros.agent.protection.ProtectionReason
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate
import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence
import com.personal.gridbot.amaros.agent.status.ConflictState
import com.personal.gridbot.amaros.agent.status.EvidenceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarCrossSourceCorrelationTest {
    private val correlator = AmarCrossSourceCorrelator()

    @Test fun empty_input_returns_empty_result() {
        val result = correlate(emptyList(), emptyMap())
        assertTrue(result.correlatedGroups.isEmpty())
        assertEquals(CorrelationReason.INSUFFICIENT_DATA, result.reason)
        assertFalse(result.isDownstreamReady)
    }

    @Test fun single_evidence_no_correlation() {
        val result = correlate(listOf(evidence("a", "source-a")), mapOf(fp("a") to true))
        assertTrue(result.correlatedGroups.isEmpty())
        assertEquals(CorrelationReason.INSUFFICIENT_DATA, result.reason)
    }

    @Test fun two_independent_evidence_agreeing() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("a", "source-b")),
            mapOf(fp("a") to true)
        )
        assertEquals(CorrelationType.AGREEMENT, result.correlatedGroups.single().correlationType)
        assertEquals(CorrelationReason.VALID_CORRELATION, result.reason)
    }

    @Test fun two_independent_evidence_disagreeing() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b")),
            mapOf(fp("a") to true, fp("b") to true),
            conflict = conflict(setOf(fp("a"), fp("b")))
        )
        assertEquals(CorrelationType.DISAGREEMENT, result.correlatedGroups.single().correlationType)
    }

    @Test fun two_dependent_evidence_not_correlated() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("a", "source-b")),
            mapOf(fp("a") to false)
        )
        assertEquals(CorrelationType.DEPENDENCY, result.correlatedGroups.single().correlationType)
    }

    @Test fun agreement_relationship_is_deterministic() {
        val input = listOf(evidence("a", "source-a"), evidence("a", "source-b"))
        val states = mapOf(fp("a") to true)
        assertEquals(correlate(input, states), correlate(input, states))
    }

    @Test fun disagreement_detected() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b")),
            mapOf(fp("a") to true, fp("b") to true),
            conflict = conflict(setOf(fp("a"), fp("b")))
        )
        assertTrue(result.correlatedGroups.any { it.correlationType == CorrelationType.DISAGREEMENT })
    }

    @Test fun conflict_from_point14_propagated() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b")),
            mapOf(fp("a") to true, fp("b") to true),
            conflict = conflict(setOf(fp("a"), fp("b")))
        )
        assertEquals(CorrelationReason.CONFLICT_UPSTREAM, result.reason)
    }

    @Test fun no_recalculation_of_independence() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("a", "source-b")),
            mapOf(fp("a") to false)
        )
        assertEquals(CorrelationType.DEPENDENCY, result.correlatedGroups.single().correlationType)
    }

    @Test fun no_recalculation_of_conflict() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b")),
            mapOf(fp("a") to true, fp("b") to true)
        )
        assertTrue(result.correlatedGroups.isEmpty())
        assertEquals(CorrelationReason.INSUFFICIENT_DATA, result.reason)
    }

    @Test fun is_deterministic() {
        val input = listOf(evidence("a", "source-b"), evidence("a", "source-a"))
        val states = mapOf(fp("a") to true)
        assertEquals(correlate(input, states), correlate(input, states))
    }

    @Test fun is_stateless() {
        val input = listOf(evidence("a", "source-a"), evidence("a", "source-b"))
        val states = mapOf(fp("a") to true)
        val first = correlator.correlate(inputContract(input, states))
        val second = correlator.correlate(inputContract(input, states))
        assertEquals(first, second)
    }

    @Test fun preserves_evidence() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("a", "source-b")),
            mapOf(fp("a") to true)
        )
        assertEquals(setOf(fp("a")), result.correlatedGroups.single().evidenceFingerprints)
    }

    @Test fun fail_closed_on_missing_independence() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("a", "source-b")),
            emptyMap()
        )
        assertEquals(CorrelationReason.MISSING_INDEPENDENCE_STATE, result.reason)
        assertFalse(result.isDownstreamReady)
    }

    @Test fun is_downstream_ready_when_valid() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("a", "source-b")),
            mapOf(fp("a") to true)
        )
        assertTrue(result.isDownstreamReady)
    }

    private fun correlate(
        evidence: List<ClassifiedEvidence>,
        states: Map<String, Boolean>,
        conflict: EvidenceConflictAwarenessResult = noConflict()
    ) = correlator.correlate(inputContract(evidence, states, conflict))

    private fun inputContract(
        evidence: List<ClassifiedEvidence>,
        states: Map<String, Boolean>,
        conflict: EvidenceConflictAwarenessResult = noConflict()
    ) = CrossSourceCorrelationInput(
        classifiedEvidence = evidence,
        independenceStates = states,
        conflictAwareness = conflict,
        deterministicEvidence = deterministic(evidence)
    )

    private fun deterministic(evidence: List<ClassifiedEvidence>) =
        DeterministicEvidenceResult(
            canonicalEvidence = evidence.map {
                CanonicalEvidence(
                    evidence = ProtectedEvidence(it, ProtectionReason.PASSED_ALL_GATES),
                    canonicalKey = it.candidate.candidate.fingerprint
                )
            },
            invalidEvidence = emptyList(),
            futureEvidence = emptyList(),
            isDownstreamReady = true,
            handlingReason = DeterministicHandlingReason.CANONICAL_ORDER_APPLIED
        )

    private fun noConflict() = EvidenceConflictAwarenessResult(
        globalConflictState = ConflictState.NO_CONFLICT,
        conflictingFingerprints = emptySet(),
        conflictDetails = emptyList(),
        isDownstreamReady = true,
        reason = ConflictAwarenessReason.NO_CONFLICTS_DETECTED
    )

    private fun conflict(fingerprints: Set<String>) = EvidenceConflictAwarenessResult(
        globalConflictState = ConflictState.CONFLICTED,
        conflictingFingerprints = fingerprints,
        conflictDetails = emptyList(),
        isDownstreamReady = true,
        reason = ConflictAwarenessReason.CONFLICTS_DETECTED
    )

    private fun evidence(fingerprintSeed: String, provider: String): ClassifiedEvidence {
        val fingerprint = fp(fingerprintSeed)
        val candidate = NormalizedCandidate(
            provider = provider,
            title = "Title",
            canonicalUrl = "https://$provider.example/evidence",
            normalizedExcerpt = "Excerpt",
            retrievedAtEpochMs = 1L,
            fingerprint = fingerprint,
            normalizationFlags = emptySet()
        )
        return ClassifiedEvidence(
            candidate = RelevantCandidate(candidate, 1.0, "test"),
            status = EvidenceStatus.VERIFIED,
            reason = null,
            explanation = "test"
        )
    }

    private fun fp(seed: String): String = seed.repeat(64).take(64)
}
