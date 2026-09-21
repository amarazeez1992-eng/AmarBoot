package com.personal.gridbot.amaros.agent.status

import com.personal.gridbot.amaros.agent.AmarEvidenceQualityUpstreamState
import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceStatusContractTest {
    private val classifier = AmarEvidenceStatusClassifier()

    @Test
    fun complete_status_when_all_verified() {
        val candidate = candidate("a".repeat(64))
        val result = classify(listOf(candidate), mapOf(candidate.fp() to true), mapOf(candidate.fp() to true))

        assertEquals(EvidenceStatus.COMPLETE, result.classified.single().status)
        assertNull(result.classified.single().reason)
    }

    @Test
    fun partial_status_when_some_verified() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            mapOf(candidate.fp() to true),
            mapOf(candidate.fp() to true),
            upstream = verified().copy(point5SourceIndependenceVerified = false)
        )

        assertEquals(EvidenceStatus.PARTIAL, result.classified.single().status)
        assertEquals(RejectionReason.PARTIAL_UPSTREAM_VERIFICATION, result.classified.single().reason)
    }

    @Test
    fun insufficient_status_when_below_minimum() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            mapOf(candidate.fp() to true),
            mapOf(candidate.fp() to true),
            minimumCount = 2
        )

        assertEquals(EvidenceStatus.INSUFFICIENT, result.classified.single().status)
        assertEquals(RejectionReason.INSUFFICIENT_EVIDENCE, result.classified.single().reason)
    }

    @Test
    fun conflicted_status_when_conflict_is_supplied() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            mapOf(candidate.fp() to true),
            mapOf(candidate.fp() to true),
            conflict = ConflictState.CONFLICTED
        )

        assertEquals(EvidenceStatus.CONFLICTED, result.classified.single().status)
        assertEquals(RejectionReason.CONFLICT_DETECTED, result.classified.single().reason)
    }

    @Test
    fun stale_status_when_freshness_is_not_verified() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            mapOf(candidate.fp() to true),
            mapOf(candidate.fp() to false)
        )

        assertEquals(EvidenceStatus.STALE, result.classified.single().status)
        assertEquals(RejectionReason.STALE_EVIDENCE, result.classified.single().reason)
    }

    @Test
    fun unverified_status_when_not_admitted() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            mapOf(candidate.fp() to false),
            mapOf(candidate.fp() to true),
            conflict = ConflictState.CONFLICTED
        )

        assertEquals(EvidenceStatus.UNVERIFIED, result.classified.single().status)
        assertEquals(RejectionReason.UNVERIFIED_ADMISSION, result.classified.single().reason)
    }

    @Test
    fun precedence_unverified_first() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            mapOf(candidate.fp() to false),
            mapOf(candidate.fp() to false),
            conflict = ConflictState.CONFLICTED,
            minimumCount = 2
        )

        assertEquals(EvidenceStatus.UNVERIFIED, result.classified.single().status)
    }

    @Test
    fun precedence_insufficient_before_partial() {
        val candidate = candidate("a".repeat(64))
        val result = classify(
            listOf(candidate),
            mapOf(candidate.fp() to true),
            mapOf(candidate.fp() to true),
            upstream = verified().copy(point9EvidenceUniquenessVerified = false),
            minimumCount = 2
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
        val result = classify(listOf(candidate), mapOf(candidate.fp() to true), mapOf(candidate.fp() to true))

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
    }

    @Test
    fun fingerprint_canonical_order_is_deterministic() {
        val high = candidate("f".repeat(64))
        val low = candidate("0".repeat(64))
        val resultA = classify(
            listOf(high, low),
            admissionStates = mapOf(high.fp() to true, low.fp() to true),
            freshnessStates = mapOf(high.fp() to true, low.fp() to true)
        )
        val resultB = classify(
            listOf(low, high),
            admissionStates = mapOf(high.fp() to true, low.fp() to true),
            freshnessStates = mapOf(high.fp() to true, low.fp() to true)
        )

        assertEquals(
            resultA.classified.map { it.candidate.candidate.fingerprint },
            resultB.classified.map { it.candidate.candidate.fingerprint }
        )
        assertTrue(resultA.classified.first().candidate.candidate.fingerprint < resultA.classified.last().candidate.candidate.fingerprint)
    }

    private fun classify(
        candidates: List<RelevantCandidate>,
        admissionStates: Map<String, Boolean>,
        freshnessStates: Map<String, Boolean>,
        upstream: AmarEvidenceQualityUpstreamState = verified(),
        conflict: ConflictState = ConflictState.NOT_AVAILABLE,
        minimumCount: Int = 1
    ) = classifier.classify(
        AmarEvidenceStatusInput(
            candidates = candidates,
            upstreamState = upstream,
            admissionStates = admissionStates,
            freshnessStates = freshnessStates,
            conflictState = conflict,
            minimumCount = minimumCount
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
