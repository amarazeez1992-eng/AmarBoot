package com.personal.gridbot.amaros.agent.deterministic

import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate
import com.personal.gridbot.amaros.agent.protection.FutureEvidence
import com.personal.gridbot.amaros.agent.protection.InvalidEvidence
import com.personal.gridbot.amaros.agent.protection.InvalidFutureEvidenceProtectionResult
import com.personal.gridbot.amaros.agent.protection.InvalidReason
import com.personal.gridbot.amaros.agent.protection.ProtectedEvidence
import com.personal.gridbot.amaros.agent.protection.ProtectionReason
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate
import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence
import com.personal.gridbot.amaros.agent.status.EvidenceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarDeterministicEvidenceHandlingTest {

    private val handler = AmarDeterministicEvidenceHandler()

    private fun evidence(fp: String, relevance: Double = 0.5): ClassifiedEvidence =
        ClassifiedEvidence(
            candidate = RelevantCandidate(
                candidate = NormalizedCandidate(
                    provider = "test",
                    title = "evidence-${fp.take(6)}",
                    canonicalUrl = "https://example.com/${fp.take(6)}",
                    normalizedExcerpt = "excerpt",
                    retrievedAtEpochMs = 1_000L,
                    fingerprint = fp,
                    normalizationFlags = emptySet()
                ),
                relevanceScore = relevance,
                reason = "test"
            ),
            status = EvidenceStatus.COMPLETE,
            reason = null,
            explanation = "test"
        )

    private fun protected(fp: String, relevance: Double = 0.5): ProtectedEvidence =
        ProtectedEvidence(evidence(fp, relevance), ProtectionReason.PASSED_ALL_GATES)

    private fun input(
        protected: List<ProtectedEvidence> = emptyList(),
        invalid: List<InvalidEvidence> = emptyList(),
        future: List<FutureEvidence> = emptyList()
    ) = InvalidFutureEvidenceProtectionResult(
        protected = protected,
        invalid = invalid,
        future = future,
        isDownstreamReady = protected.isNotEmpty()
    )

    @Test
    fun empty_input_returns_empty_result() {
        val result = handler.handle(input())
        assertEquals(
            DeterministicEvidenceResult.failed(DeterministicHandlingReason.MISSING_EVIDENCE),
            result
        )
    }

    @Test
    fun single_evidence_is_canonical() {
        val fp = "a".repeat(64)
        val result = handler.handle(input(protected = listOf(protected(fp))))
        assertEquals(1, result.canonicalEvidence.size)
        assertEquals(fp, result.canonicalEvidence.single().canonicalKey)
        assertTrue(result.isDownstreamReady)
    }

    @Test
    fun multiple_evidences_sorted_by_fingerprint_asc() {
        val a = "a".repeat(64)
        val b = "b".repeat(64)
        val c = "c".repeat(64)
        val result = handler.handle(input(protected = listOf(protected(c), protected(a), protected(b))))
        assertEquals(listOf(a, b, c), result.canonicalEvidence.map { it.canonicalKey })
    }

    @Test
    fun canonical_order_is_deterministic() {
        val a = "a".repeat(64)
        val b = "b".repeat(64)
        val c = "c".repeat(64)
        val value = input(protected = listOf(protected(b), protected(c), protected(a)))
        assertEquals(handler.handle(value), handler.handle(value))
    }

    @Test
    fun canonical_order_independent_of_input_order() {
        val a = "a".repeat(64)
        val b = "b".repeat(64)
        val c = "c".repeat(64)
        val first = handler.handle(input(protected = listOf(protected(c), protected(a), protected(b))))
        val second = handler.handle(input(protected = listOf(protected(b), protected(c), protected(a))))
        assertEquals(first, second)
    }

    @Test
    fun invalid_evidence_preserved() {
        val e = evidence("a".repeat(64))
        val invalid = InvalidEvidence(e, InvalidReason.TAMPERED, "source")
        val result = handler.handle(input(protected = listOf(protected("b".repeat(64))), invalid = listOf(invalid)))
        assertEquals(listOf(invalid), result.invalidEvidence)
    }

    @Test
    fun future_evidence_preserved() {
        val e = evidence("a".repeat(64))
        val future = FutureEvidence(e, 2_000L, "FUTURE_TIMESTAMP")
        val result = handler.handle(input(protected = listOf(protected("b".repeat(64))), future = listOf(future)))
        assertEquals(listOf(future), result.futureEvidence)
    }

    @Test
    fun duplicate_fingerprint_detected_as_duplicate_canonical_key() {
        val fp = "a".repeat(64)
        val result = handler.handle(input(protected = listOf(protected(fp), protected(fp))))
        assertEquals(
            DeterministicHandlingReason.DUPLICATE_CANONICAL_KEY,
            result.handlingReason
        )
        assertFalse(result.isDownstreamReady)
    }

    @Test
    fun invalid_fingerprint_rejected() {
        val fp = "z".repeat(64)
        val result = handler.handle(input(protected = listOf(protected(fp))))
        assertEquals(DeterministicHandlingReason.INVALID_FINGERPRINT, result.handlingReason)
        assertFalse(result.isDownstreamReady)
    }

    @Test
    fun no_recalculation_of_ranking() {
        val fp = "a".repeat(64)
        val e = protected(fp, relevance = 0.91)
        val result = handler.handle(input(protected = listOf(e)))
        assertEquals(e, result.canonicalEvidence.single().evidence)
        assertEquals(0.91, result.canonicalEvidence.single().evidence.evidence.candidate.relevanceScore, 0.0)
    }

    @Test
    fun no_recalculation_of_relevance() {
        val fp = "a".repeat(64)
        val e = protected(fp, relevance = 0.37)
        val result = handler.handle(input(protected = listOf(e)))
        assertEquals(0.37, result.canonicalEvidence.single().evidence.evidence.candidate.relevanceScore, 0.0)
    }

    @Test
    fun no_modification_of_evidence() {
        val e = protected("a".repeat(64), relevance = 0.73)
        val result = handler.handle(input(protected = listOf(e)))
        assertEquals(e, result.canonicalEvidence.single().evidence)
        assertNotSame(e, result.canonicalEvidence.single().evidence)
    }

    @Test
    fun result_partitions_correctly() {
        val protectedA = protected("a".repeat(64))
        val protectedB = protected("b".repeat(64))
        val invalidEvidence = InvalidEvidence(evidence("c".repeat(64)), InvalidReason.TAMPERED, "source")
        val futureEvidence = FutureEvidence(evidence("d".repeat(64)), 2_000L, "FUTURE_TIMESTAMP")

        val result = handler.handle(
            input(
                protected = listOf(protectedB, protectedA),
                invalid = listOf(invalidEvidence),
                future = listOf(futureEvidence)
            )
        )

        assertEquals(2, result.canonicalEvidence.size)
        assertEquals(listOf(invalidEvidence), result.invalidEvidence)
        assertEquals(listOf(futureEvidence), result.futureEvidence)
    }

    @Test
    fun is_downstream_ready_false_when_invalid_fingerprint() {
        val result = handler.handle(input(protected = listOf(protected("x".repeat(64)))))
        assertFalse(result.isDownstreamReady)
    }

    @Test
    fun stateless_and_deterministic() {
        val value = input(
            protected = listOf(
                protected("c".repeat(64), relevance = 0.9),
                protected("a".repeat(64), relevance = 0.1),
                protected("b".repeat(64), relevance = 0.5)
            )
        )
        val first = handler.handle(value)
        val second = handler.handle(value)
        assertEquals(first, second)
        assertEquals(listOf("a".repeat(64), "b".repeat(64), "c".repeat(64)), first.canonicalEvidence.map { it.canonicalKey })
    }
}
