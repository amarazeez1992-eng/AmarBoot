package com.personal.gridbot.amaros.agent.protection

import com.personal.gridbot.amaros.agent.FreshnessStatus
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationResult
import com.personal.gridbot.amaros.agent.claim.RejectedClaim
import com.personal.gridbot.amaros.agent.claim.StructuredClaim
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationState
import com.personal.gridbot.amaros.agent.conflict.EvidenceConflictAwarenessResult
import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence
import com.personal.gridbot.amaros.agent.status.ConflictState
import com.personal.gridbot.amaros.agent.status.EvidenceStatus
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate
import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate
import com.personal.gridbot.amaros.intelligence.verification.AmarConflict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarInvalidFutureEvidenceProtectionTest {

    private val protector = AmarInvalidFutureEvidenceProtector()
    private val emptyClaims = ClaimVerificationResult(emptyList(), emptyList(), true)

    private fun evidence(fp: String = "a".repeat(64), retrievedAt: Long = 1_000L): ClassifiedEvidence =
        ClassifiedEvidence(
            candidate = RelevantCandidate(
                candidate = NormalizedCandidate(
                    provider = "test",
                    title = "evidence",
                    canonicalUrl = "https://example.com/evidence",
                    normalizedExcerpt = "excerpt",
                    retrievedAtEpochMs = retrievedAt,
                    fingerprint = fp,
                    normalizationFlags = emptySet()
                ),
                relevanceScore = 1.0,
                reason = "test"
            ),
            status = EvidenceStatus.COMPLETE,
            reason = null,
            explanation = "test"
        )

    private fun conflict(state: ConflictState) = EvidenceConflictAwarenessResult(
        globalConflictState = state,
        conflictingFingerprints = emptySet(),
        conflictDetails = emptyList<AmarConflict>(),
        isDownstreamReady = state == ConflictState.NO_CONFLICT,
        reason = com.personal.gridbot.amaros.agent.conflict.ConflictAwarenessReason.NO_CONFLICT
    )

    private fun input(
        evidence: List<ClassifiedEvidence>,
        freshness: Map<String, FreshnessStatus>,
        tampering: Map<String, Boolean> = emptyMap(),
        conflicts: ConflictState = ConflictState.NO_CONFLICT,
        claims: ClaimVerificationResult = emptyClaims
    ) = InvalidFutureEvidenceProtectionInput(
        evidence = evidence,
        freshnessStates = freshness,
        tamperingStates = tampering,
        conflictAwareness = conflict(conflicts),
        claimVerification = claims
    )

    @Test fun empty_input_returns_empty_result() {
        assertEquals(InvalidFutureEvidenceProtectionResult.empty(), protector.protect(input(emptyList(), emptyMap())))
    }

    @Test fun future_evidence_is_rejected_and_quarantined() {
        val e = evidence(retrievedAt = 2_000L)
        val r = protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FUTURE)))
        assertEquals(1, r.future.size)
        assertEquals(2_000L, r.future.single().futureTimestamp)
        assertFalse(r.isDownstreamReady)
    }

    @Test fun fresh_evidence_is_protected() {
        val e = evidence()
        val r = protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FRESH)))
        assertEquals(1, r.protected.size)
    }

    @Test fun stale_evidence_is_protected() {
        val e = evidence()
        val r = protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.STALE)))
        assertEquals(1, r.protected.size)
    }

    @Test fun tampered_evidence_is_invalid() {
        val e = evidence()
        val r = protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FRESH), mapOf(e.candidate.candidate.fingerprint to true)))
        assertEquals(InvalidReason.TAMPERED, r.invalid.single().reason)
    }

    @Test fun conflicted_evidence_is_invalid() {
        val e = evidence()
        val r = protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FRESH), conflicts = ConflictState.CONFLICTED))
        assertEquals(InvalidReason.CONFLICTED_EVIDENCE, r.invalid.single().reason)
    }

    @Test fun claim_rejected_evidence_is_invalid() {
        val e = evidence()
        val rejected = ClaimVerificationResult(
            emptyList(),
            listOf(RejectedClaim(StructuredClaim("c1", "claim", "s", "p", "o"), ClaimVerificationState.OPPOSED, "rejected")),
            false
        )
        val r = protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FRESH), claims = rejected))
        assertEquals(InvalidReason.CLAIM_REJECTED, r.invalid.single().reason)
    }

    @Test fun valid_evidence_passes() {
        val e = evidence()
        assertTrue(protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FRESH))).protected.isNotEmpty())
    }

    @Test fun protection_is_deterministic() {
        val e = evidence()
        val i = input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FRESH))
        assertEquals(protector.protect(i), protector.protect(i))
    }

    @Test fun protection_is_stateless() {
        val e = evidence()
        val i = input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FRESH))
        val first = protector.protect(i)
        val second = protector.protect(i)
        assertEquals(first, second)
    }

    @Test fun no_recalculation_of_freshness() {
        val e = evidence(retrievedAt = 9_999L)
        val r = protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.STALE)))
        assertEquals(1, r.protected.size)
    }

    @Test fun no_recalculation_of_tampering() {
        val e = evidence()
        val r = protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FRESH), mapOf(e.candidate.candidate.fingerprint to false)))
        assertEquals(1, r.protected.size)
    }

    @Test fun no_recalculation_of_conflict() {
        val e = evidence()
        val r = protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FRESH), conflicts = ConflictState.NO_CONFLICT))
        assertEquals(1, r.protected.size)
    }

    @Test fun preserves_evidence() {
        val e = evidence()
        val r = protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FRESH)))
        assertEquals(e, r.protected.single().evidence)
    }

    @Test fun result_partitions_correctly() {
        val a = evidence("a".repeat(64))
        val b = evidence("b".repeat(64))
        val c = evidence("c".repeat(64), 2_000L)
        val r = protector.protect(input(listOf(a,b,c), mapOf(
            a.candidate.candidate.fingerprint to FreshnessStatus.FRESH,
            b.candidate.candidate.fingerprint to FreshnessStatus.FRESH,
            c.candidate.candidate.fingerprint to FreshnessStatus.FUTURE
        ), mapOf(b.candidate.candidate.fingerprint to true)))
        assertEquals(1, r.protected.size)
        assertEquals(1, r.invalid.size)
        assertEquals(1, r.future.size)
    }

    @Test fun fail_closed_on_missing_freshness_state() {
        val e = evidence()
        val r = protector.protect(input(listOf(e), emptyMap()))
        assertEquals(InvalidReason.UNKNOWN_INVALIDITY, r.invalid.single().reason)
        assertFalse(r.isDownstreamReady)
    }

    @Test fun is_downstream_ready_false_when_invalid_present() {
        val e = evidence()
        val r = protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FRESH), mapOf(e.candidate.candidate.fingerprint to true)))
        assertFalse(r.isDownstreamReady)
    }

    @Test fun is_downstream_ready_false_when_future_present() {
        val e = evidence(retrievedAt = 2_000L)
        val r = protector.protect(input(listOf(e), mapOf(e.candidate.candidate.fingerprint to FreshnessStatus.FUTURE)))
        assertFalse(r.isDownstreamReady)
    }

    @Test fun fingerprint_used_as_canonical_id() {
        val fp = "d".repeat(64)
        val e = evidence(fp)
        val r = protector.protect(input(listOf(e), mapOf(fp to FreshnessStatus.FRESH)))
        assertEquals(fp, r.protected.single().evidence.candidate.candidate.fingerprint)
    }

    @Test fun audit_trail_preserved_for_rejected() {
        val e = evidence()
        val r = protector.protect(input(listOf(e), emptyMap()))
        assertEquals(e, r.invalid.single().evidence)
        assertEquals(e.candidate.candidate.fingerprint, r.invalid.single().originalSource)
    }
}
