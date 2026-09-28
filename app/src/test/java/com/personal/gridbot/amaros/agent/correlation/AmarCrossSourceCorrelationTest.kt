package com.personal.gridbot.amaros.agent.correlation

import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationResult
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationState
import com.personal.gridbot.amaros.agent.claim.StructuredClaim
import com.personal.gridbot.amaros.agent.claim.VerifiedClaim
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
        val result = correlate(emptyList(), emptyMap(), verification = null)
        assertTrue(result.correlatedGroups.isEmpty())
        assertFalse(result.isDownstreamReady)
    }

    @Test fun single_evidence_no_correlation() {
        val result = correlate(
            listOf(evidence("a", "source-a")),
            mapOf(fp("a") to true),
            verification = verificationOf(
                claim("gold rising", supporting = listOf(fp("a")))
            )
        )
        assertTrue(result.correlatedGroups.isEmpty())
        assertEquals(CorrelationReason.INSUFFICIENT_DATA, result.reason)
    }

    @Test fun two_independent_evidence_supporting_same_claim_agree() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b")),
            mapOf(fp("a") to true, fp("b") to true),
            verification = verificationOf(
                claim("gold rising", supporting = listOf(fp("a"), fp("b")))
            )
        )
        assertEquals(1, result.correlatedGroups.size)
        assertEquals(CorrelationType.AGREEMENT, result.correlatedGroups.single().correlationType)
        assertEquals(setOf(fp("a"), fp("b")), result.correlatedGroups.single().evidenceFingerprints)
        assertEquals(listOf("gold rising"), result.correlatedGroups.single().sharedClaims)
    }

    @Test fun two_dependent_evidence_supporting_same_claim_dependency() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b")),
            mapOf(fp("a") to false, fp("b") to false),
            verification = verificationOf(
                claim("gold rising", supporting = listOf(fp("a"), fp("b")))
            )
        )
        assertEquals(1, result.correlatedGroups.size)
        assertEquals(CorrelationType.DEPENDENCY, result.correlatedGroups.single().correlationType)
    }

    @Test fun two_evidence_opposing_same_claim_disagree() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b")),
            mapOf(fp("a") to true, fp("b") to true),
            verification = verificationOf(
                claim("gold rising", opposing = listOf(fp("a"), fp("b")))
            )
        )
        assertEquals(1, result.correlatedGroups.size)
        assertEquals(CorrelationType.DISAGREEMENT, result.correlatedGroups.single().correlationType)
        assertEquals(listOf("gold rising"), result.correlatedGroups.single().sharedClaims)
    }

    @Test fun mixed_agreement_and_disagreement_groups() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b"), evidence("c", "source-c")),
            mapOf(fp("a") to true, fp("b") to true, fp("c") to true),
            verification = verificationOf(
                claim("gold rising", supporting = listOf(fp("a"), fp("b"))),
                claim("gold falling", opposing = listOf(fp("c")))
            )
        )
        assertEquals(1, result.correlatedGroups.size)
        assertEquals(CorrelationType.AGREEMENT, result.correlatedGroups.single().correlationType)
    }

    @Test fun single_evidence_per_claim_produces_no_group() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b")),
            mapOf(fp("a") to true, fp("b") to true),
            verification = verificationOf(
                claim("gold rising", supporting = listOf(fp("a"))),
                claim("silver falling", opposing = listOf(fp("b")))
            )
        )
        assertTrue(result.correlatedGroups.isEmpty())
        assertEquals(CorrelationReason.INSUFFICIENT_DATA, result.reason)
    }

    @Test fun upstream_conflict_propagated_as_disagreement() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b")),
            mapOf(fp("a") to true, fp("b") to true),
            conflict = conflict(setOf(fp("a"), fp("b"))),
            verification = verificationOf(
                claim("gold rising", supporting = listOf(fp("a"), fp("b")))
            )
        )
        assertEquals(CorrelationReason.CONFLICT_UPSTREAM, result.reason)
        assertEquals(1, result.correlatedGroups.size)
        assertEquals(CorrelationType.DISAGREEMENT, result.correlatedGroups.single().correlationType)
    }

    @Test fun no_claim_verification_returns_insufficient_without_crash() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b")),
            mapOf(fp("a") to true, fp("b") to true),
            verification = null
        )
        assertTrue(result.correlatedGroups.isEmpty())
        assertTrue(result.isDownstreamReady)
        assertEquals(CorrelationReason.INSUFFICIENT_DATA, result.reason)
    }

    @Test fun fail_closed_on_missing_independence() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b")),
            emptyMap(),
            verification = verificationOf(claim("gold rising", supporting = listOf(fp("a"), fp("b"))))
        )
        assertEquals(CorrelationReason.MISSING_INDEPENDENCE_STATE, result.reason)
        assertFalse(result.isDownstreamReady)
    }

    @Test fun empty_deterministic_evidence_fails_closed() {
        val result = correlator.correlate(
            CrossSourceCorrelationInput(
                classifiedEvidence = listOf(evidence("a", "source-a")),
                independenceStates = mapOf(fp("a") to true),
                conflictAwareness = noConflict(),
                deterministicEvidence = DeterministicEvidenceResult(
                    canonicalEvidence = emptyList(),
                    invalidEvidence = emptyList(),
                    futureEvidence = emptyList(),
                    isDownstreamReady = true,
                    handlingReason = DeterministicHandlingReason.CANONICAL_ORDER_APPLIED
                ),
                claimVerification = null
            )
        )
        assertEquals(CorrelationReason.INSUFFICIENT_DATA, result.reason)
        assertFalse(result.isDownstreamReady)
    }

    @Test fun blank_fingerprint_fails_closed() {
        val blankEvidence = evidence("a", "source-a").let { e ->
            val blankCandidate = e.candidate.candidate.copy(fingerprint = "")
            e.copy(candidate = e.candidate.copy(candidate = blankCandidate))
        }
        val result = correlator.correlate(
            CrossSourceCorrelationInput(
                classifiedEvidence = listOf(blankEvidence),
                independenceStates = emptyMap(),
                conflictAwareness = noConflict(),
                deterministicEvidence = DeterministicEvidenceResult(
                    canonicalEvidence = emptyList(),
                    invalidEvidence = emptyList(),
                    futureEvidence = emptyList(),
                    isDownstreamReady = true,
                    handlingReason = DeterministicHandlingReason.CANONICAL_ORDER_APPLIED
                ),
                claimVerification = null
            )
        )
        assertEquals(CorrelationReason.INVALID_INPUT, result.reason)
    }

    @Test fun is_deterministic() {
        val evidence = listOf(evidence("a", "source-a"), evidence("b", "source-b"))
        val states = mapOf(fp("a") to true, fp("b") to true)
        val verification = verificationOf(claim("gold rising", supporting = listOf(fp("a"), fp("b"))))
        assertEquals(
            correlate(evidence, states, verification = verification),
            correlate(evidence, states, verification = verification)
        )
    }

    @Test fun is_stateless() {
        val evidence = listOf(evidence("a", "source-a"), evidence("b", "source-b"))
        val states = mapOf(fp("a") to true, fp("b") to true)
        val verification = verificationOf(claim("gold rising", supporting = listOf(fp("a"), fp("b"))))
        val first = correlator.correlate(inputContract(evidence, states, noConflict(), verification))
        val second = correlator.correlate(inputContract(evidence, states, noConflict(), verification))
        assertEquals(first, second)
    }

    @Test fun preserves_evidence() {
        val evidence = listOf(evidence("a", "source-a"), evidence("b", "source-b"))
        val result = correlate(
            evidence,
            mapOf(fp("a") to true, fp("b") to true),
            verification = verificationOf(claim("gold rising", supporting = listOf(fp("a"), fp("b"))))
        )
        assertEquals(setOf(fp("a"), fp("b")), result.correlatedGroups.single().evidenceFingerprints)
    }

    @Test fun is_downstream_ready_when_valid() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b")),
            mapOf(fp("a") to true, fp("b") to true),
            verification = verificationOf(claim("gold rising", supporting = listOf(fp("a"), fp("b"))))
        )
        assertTrue(result.isDownstreamReady)
    }

    @Test fun groups_are_sorted_deterministically() {
        val result = correlate(
            listOf(evidence("a", "source-a"), evidence("b", "source-b"), evidence("c", "source-c"), evidence("d", "source-d")),
            mapOf(fp("a") to true, fp("b") to true, fp("c") to true, fp("d") to true),
            verification = verificationOf(
                claim("claim-z", supporting = listOf(fp("c"), fp("d"))),
                claim("claim-a", supporting = listOf(fp("a"), fp("b")))
            )
        )
        assertEquals(2, result.correlatedGroups.size)
        assertEquals(listOf("claim-a"), result.correlatedGroups[0].sharedClaims)
        assertEquals(listOf("claim-z"), result.correlatedGroups[1].sharedClaims)
    }

    // ---- helpers ----

    private fun correlate(
        evidence: List<ClassifiedEvidence>,
        states: Map<String, Boolean>,
        conflict: EvidenceConflictAwarenessResult = noConflict(),
        verification: ClaimVerificationResult?
    ) = correlator.correlate(inputContract(evidence, states, conflict, verification))

    private fun inputContract(
        evidence: List<ClassifiedEvidence>,
        states: Map<String, Boolean>,
        conflict: EvidenceConflictAwarenessResult = noConflict(),
        verification: ClaimVerificationResult?
    ) = CrossSourceCorrelationInput(
        classifiedEvidence = evidence,
        independenceStates = states,
        conflictAwareness = conflict,
        deterministicEvidence = deterministic(evidence),
        claimVerification = verification
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
            status = EvidenceStatus.COMPLETE,
            reason = null,
            explanation = "test"
        )
    }

    private fun claim(
        text: String,
        supporting: List<String> = emptyList(),
        opposing: List<String> = emptyList()
    ) = VerifiedClaim(
        claim = StructuredClaim("claim-$text", text, "", "", null),
        state = when {
            supporting.isNotEmpty() && opposing.isNotEmpty() -> ClaimVerificationState.CONFLICTED
            opposing.isNotEmpty() -> ClaimVerificationState.OPPOSED
            supporting.isNotEmpty() -> ClaimVerificationState.SUPPORTED
            else -> ClaimVerificationState.NEUTRAL
        },
        supportingEvidenceIds = supporting,
        opposingEvidenceIds = opposing,
        explanation = "test"
    )

    private fun verificationOf(vararg claims: VerifiedClaim) = ClaimVerificationResult(
        verifiedClaims = claims.toList(),
        rejectedClaims = emptyList(),
        isDownstreamReady = true
    )

    private fun fp(seed: String): String = seed.repeat(64).take(64)
}
