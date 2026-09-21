package com.personal.gridbot.amaros.agent.status

import com.personal.gridbot.amaros.agent.AmarEvidenceQualityUpstreamState
import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceStatusContractTest {
    private val classifier = AmarEvidenceStatusClassifier()

    @Test
    fun complete_status_when_all_verified() {
        val candidate = candidate("a".repeat(64))
        val result = classify(listOf(candidate))

        assertEquals(EvidenceStatus.COMPLETE, result.classified.single().status)
        assertNull(result.classified.single().reason)
        assertTrue(result.classified.single().explanation.isNotBlank())
    }

    @Test
    fun partial_status_when_some_verified() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            upstream = verified().copy(point5SourceIndependenceVerified = false)
        )

        assertEquals(EvidenceStatus.PARTIAL, result.classified.single().status)
        assertEquals(RejectionReason.PARTIAL_UPSTREAM_VERIFICATION, result.classified.single().reason)
        assertEquals(EvidenceStatus.PARTIAL, result.overallStatus)
        assertTrue(result.isDownstreamReady)
    }

    @Test
    fun insufficient_status_when_below_context_minimum() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            queryContext = QueryContext.FINANCIAL_LIVE
        )

        assertEquals(EvidenceStatus.INSUFFICIENT, result.classified.single().status)
        assertEquals(RejectionReason.INSUFFICIENT_EVIDENCE, result.classified.single().reason)
        assertEquals(EvidenceStatus.INSUFFICIENT, result.overallStatus)
        assertFalse(result.isDownstreamReady)
    }

    @Test
    fun conflicted_status_when_conflict_is_supplied() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            conflict = ConflictState.CONFLICTED
        )

        assertEquals(EvidenceStatus.CONFLICTED, result.classified.single().status)
        assertEquals(RejectionReason.CONFLICT_DETECTED, result.classified.single().reason)
        assertEquals(EvidenceStatus.CONFLICTED, result.overallStatus)
        assertFalse(result.isDownstreamReady)
    }

    @Test
    fun stale_status_when_freshness_is_not_verified() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            freshnessStates = mapOf(candidate.fp() to false)
        )

        assertEquals(EvidenceStatus.STALE, result.classified.single().status)
        assertEquals(RejectionReason.STALE_EVIDENCE, result.classified.single().reason)
        assertEquals(EvidenceStatus.PARTIAL, result.overallStatus)
        assertTrue(result.isDownstreamReady)
    }

    @Test
    fun unverified_status_when_not_admitted() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            admissionStates = mapOf(candidate.fp() to false),
            conflict = ConflictState.CONFLICTED
        )

        assertEquals(EvidenceStatus.UNVERIFIED, result.classified.single().status)
        assertEquals(RejectionReason.UNVERIFIED_ADMISSION, result.classified.single().reason)
        assertEquals(EvidenceStatus.UNVERIFIED, result.overallStatus)
        assertFalse(result.isDownstreamReady)
    }

    @Test
    fun precedence_unverified_first() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            admissionStates = mapOf(candidate.fp() to false),
            freshnessStates = mapOf(candidate.fp() to false),
            conflict = ConflictState.CONFLICTED,
            queryContext = QueryContext.FINANCIAL_LIVE
        )

        assertEquals(EvidenceStatus.UNVERIFIED, result.classified.single().status)
    }

    @Test
    fun precedence_insufficient_before_partial() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            upstream = verified().copy(point9EvidenceUniquenessVerified = false),
            queryContext = QueryContext.FINANCIAL_LIVE
        )

        assertEquals(EvidenceStatus.INSUFFICIENT, result.classified.single().status)
    }

    @Test
    fun status_is_deterministic() {
        val candidate = candidate("a".repeat(64))
        val input = AmarEvidenceStatusInput(
            candidates = listOf(candidate),
            upstreamState = verified(),
            admissionStates = mapOf(candidate.fp() to true),
            freshnessStates = mapOf(candidate.fp() to true)
        )

        assertEquals(classifier.classify(input), classifier.classify(input))
    }

    @Test
    fun status_preserves_evidence() {
        val candidate = candidate("a".repeat(64))
        val result = classify(listOf(candidate))

        assertEquals(candidate, result.classified.single().candidate)
    }

    @Test
    fun status_result_partitions_correctly() {
        val complete = candidate("a".repeat(64))
        val missingState = candidate("b".repeat(64))
        val result = classify(
            listOf(complete, missingState),
            admissionStates = mapOf(complete.fp() to true),
            freshnessStates = mapOf(complete.fp() to true)
        )

        assertEquals(1, result.classified.size)
        assertEquals(1, result.unclassified.size)
        assertEquals(complete, result.classified.single().candidate)
        assertEquals(missingState, result.unclassified.single())
        assertEquals(EvidenceStatus.UNVERIFIED, result.overallStatus)
        assertFalse(result.isDownstreamReady)
    }

    @Test
    fun fingerprint_canonical_order_is_deterministic() {
        val high = candidate("f".repeat(64))
        val low = candidate("0".repeat(64))
        val resultA = classify(listOf(high, low))
        val resultB = classify(listOf(low, high))

        assertEquals(
            resultA.classified.map { it.candidate.candidate.fingerprint },
            resultB.classified.map { it.candidate.candidate.fingerprint }
        )
        assertTrue(
            resultA.classified.first().candidate.candidate.fingerprint <
                resultA.classified.last().candidate.candidate.fingerprint
        )
    }

    @Test
    fun overall_status_complete_when_all_complete() {
        val first = candidate("a".repeat(64))
        val second = candidate("b".repeat(64))
        val result = classify(listOf(first, second))

        assertEquals(EvidenceStatus.COMPLETE, result.overallStatus)
        assertEquals(2, result.distribution[EvidenceStatus.COMPLETE])
        assertTrue(result.isDownstreamReady)
    }

    @Test
    fun overall_status_conflicted_when_any_conflicted() {
        val first = candidate("a".repeat(64))
        val second = candidate("b".repeat(64))
        val result = classify(
            listOf(first, second),
            conflict = ConflictState.CONFLICTED
        )

        assertEquals(EvidenceStatus.CONFLICTED, result.overallStatus)
        assertEquals(2, result.distribution[EvidenceStatus.CONFLICTED])
        assertFalse(result.isDownstreamReady)
    }

    @Test
    fun is_downstream_ready_false_when_unclassified_present() {
        val classified = candidate("a".repeat(64))
        val unclassified = candidate("b".repeat(64))
        val result = classify(
            listOf(classified, unclassified),
            admissionStates = mapOf(classified.fp() to true),
            freshnessStates = mapOf(classified.fp() to true)
        )

        assertEquals(EvidenceStatus.UNVERIFIED, result.overallStatus)
        assertFalse(result.isDownstreamReady)
        assertEquals(0, result.distribution[EvidenceStatus.UNVERIFIED])
    }

    private fun classify(
        candidates: List<RelevantCandidate>,
        admissionStates: Map<String, Boolean> = candidates.associate { it.fp() to true },
        freshnessStates: Map<String, Boolean> = candidates.associate { it.fp() to true },
        upstream: AmarEvidenceQualityUpstreamState = verified(),
        conflict: ConflictState = ConflictState.NOT_AVAILABLE,
        queryContext: QueryContext = QueryContext.GENERAL
    ) = classifier.classify(
        AmarEvidenceStatusInput(
            candidates = candidates,
            upstreamState = upstream,
            admissionStates = admissionStates,
            freshnessStates = freshnessStates,
            conflictState = conflict,
            queryContext = queryContext
        )
    )

    private fun verified() = AmarEvidenceQualityUpstreamState(
        point1EvidenceIntakeVerified = true,
        point2SourceQualityVerified = true,
        point3AuthorityVerified = true,
        point4FreshnessVerified = true,
        point5SourceIndependenceVerified = true,
        point6DuplicateFreeVerified = true,
        point7FingerprintIntegrityVerified = true,
        point8TamperingIntegrityVerified = true,
        point9EvidenceUniquenessVerified = true
    )

    private fun candidate(fingerprint: String) = RelevantCandidate(
        candidate = NormalizedCandidate(
            provider = "provider",
            title = "Evidence $fingerprint",
            canonicalUrl = "https://example.com/$fingerprint",
            normalizedExcerpt = "Evidence",
            retrievedAtEpochMs = 1L,
            fingerprint = fingerprint,
            normalizationFlags = emptySet()
        ),
        relevanceScore = 1.0,
        reason = "RELEVANCE_ACCEPTED"
    )

    private fun RelevantCandidate.fp() = candidate.fingerprint
}
