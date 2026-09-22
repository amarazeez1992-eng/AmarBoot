package com.personal.gridbot.amaros.agent.claim

import com.personal.gridbot.amaros.agent.AmarEvidence
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate
import com.personal.gridbot.amaros.agent.conflict.ConflictAwarenessReason
import com.personal.gridbot.amaros.agent.conflict.EvidenceConflictAwarenessResult
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate
import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence
import com.personal.gridbot.amaros.agent.status.ConflictState
import com.personal.gridbot.amaros.agent.status.EvidenceStatus
import com.personal.gridbot.amaros.agent.status.RejectionReason
import com.personal.gridbot.amaros.agent.AmarSourceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AmarClaimVerificationTest {
    private fun finding(text: String, stance: EvidenceStance = EvidenceStance.SUPPORTS) =
        ResearchFinding("Test Source", "https://example.test/" + text.hashCode(), text,
            Authority.OFFICIAL, stance, sourceType = AmarSourceType.KNOWLEDGE)

    private fun awareness(conflicts: Set<String> = emptySet()) =
        EvidenceConflictAwarenessResult(
            ConflictState.NONE, conflicts, emptyList(), true,
            ConflictAwarenessReason.NO_CONFLICTS_DETECTED
        )

    private fun input(
        answer: String = "The market report confirms gold prices are rising today.",
        claims: List<StructuredClaim>? = null,
        findings: List<ResearchFinding> = listOf(finding(answer)),
        upstream: Map<String, ClassifiedEvidence> = emptyMap(),
        conflicts: Set<String> = emptySet()
    ) = AmarClaimVerificationInput(answer, claims, findings, upstream, awareness(conflicts))

    private fun claim(text: String, id: String = "c1") =
        StructuredClaim(id, text, "market", "state", "rising")

    @Test fun empty_answer_returns_empty_result() {
        val r = AmarClaimVerifier().verify(input(answer = "", findings = emptyList()))
        assertTrue(r.verifiedClaims.isEmpty()); assertTrue(r.rejectedClaims.isEmpty()); assertFalse(r.isDownstreamReady)
    }

    @Test fun empty_findings_returns_insufficient() {
        val r = AmarClaimVerifier().verify(input(findings = emptyList()))
        assertEquals(ClaimVerificationState.INSUFFICIENT, r.rejectedClaims.single().state)
    }

    @Test fun single_claim_with_support_is_verified() {
        val r = AmarClaimVerifier().verify(input())
        assertEquals(ClaimVerificationState.SUPPORTED, r.verifiedClaims.single().state)
    }

    @Test fun single_claim_with_opposition_is_rejected() {
        val text = "The market report confirms gold prices are falling today."
        val r = AmarClaimVerifier().verify(input(answer = text, findings = listOf(finding(text, EvidenceStance.OPPOSES))))
        assertEquals(ClaimVerificationState.OPPOSED, r.rejectedClaims.single().state)
    }

    @Test fun single_claim_neutral_evidence_is_insufficient() {
        val text = "The market report confirms gold prices are rising today."
        val r = AmarClaimVerifier().verify(input(findings = listOf(finding(text, EvidenceStance.UNKNOWN))))
        assertEquals(ClaimVerificationState.NEUTRAL, r.rejectedClaims.single().state)
    }

    @Test fun claim_with_conflict_is_conflicted() {
        val f = finding("The market report confirms gold prices are rising today.")
        val r = AmarClaimVerifier().verify(input(findings = listOf(f), conflicts = setOf(f.fingerprint)))
        assertEquals(ClaimVerificationState.CONFLICTED, r.rejectedClaims.single().state)
    }

    @Test fun claim_verification_is_deterministic() {
        val v = AmarClaimVerifier()
        assertEquals(v.verify(input()), v.verify(input()))
    }

    @Test fun claim_verification_preserves_findings() {
        val f = finding("The market report confirms gold prices are rising today.")
        val r = AmarClaimVerifier().verify(input(findings = listOf(f)))
        assertEquals(f.fingerprint, r.verifiedClaims.single().supportingEvidenceIds.single())
    }

    @Test fun claim_verification_uses_upstream_states() {
        val f = finding("The market report confirms gold prices are rising today.")
        val candidate = NormalizedCandidate("test", "Test", f.sourceUri, f.evidence, f.retrievedAtEpochMs, f.fingerprint, emptySet())
        val classified = ClassifiedEvidence(
            RelevantCandidate(candidate, 1.0, "test"),
            EvidenceStatus.CONFLICTED, RejectionReason.CONFLICT_DETECTED, "conflict"
        )
        val r = AmarClaimVerifier().verify(input(findings = listOf(f), upstream = mapOf(f.fingerprint to classified)))
        assertEquals(ClaimVerificationState.CONFLICTED, r.rejectedClaims.single().state)
    }

    @Test fun claim_verification_uses_conflict_awareness() {
        val f = finding("The market report confirms gold prices are rising today.")
        val r = AmarClaimVerifier().verify(input(findings = listOf(f), conflicts = setOf(f.fingerprint)))
        assertEquals(ClaimVerificationState.CONFLICTED, r.rejectedClaims.single().state)
    }

    @Test fun multiple_claims_partition_correctly() {
        val a = claim("Gold prices are rising today according to the market report.", "a")
        val b = claim("Silver prices are falling today according to the official report.", "b")
        val r = AmarClaimVerifier().verify(input(
            claims = listOf(a, b),
            findings = listOf(finding(a.text), finding(b.text, EvidenceStance.OPPOSES))
        ))
        assertEquals(ClaimVerificationState.SUPPORTED, r.verifiedClaims.first { it.claim.id == "a" }.state)
        assertEquals(ClaimVerificationState.OPPOSED, r.rejectedClaims.first { it.claim.id == "b" }.state)
    }

    @Test fun fingerprint_used_as_canonical_id() {
        val f = finding("The market report confirms gold prices are rising today.")
        val r = AmarClaimVerifier().verify(input(findings = listOf(f)))
        assertEquals(f.fingerprint, r.verifiedClaims.single().supportingEvidenceIds.single())
    }

    @Test fun no_recalculation_of_authority() {
        val f = finding("The market report confirms gold prices are rising today.", EvidenceStance.SUPPORTS)
        assertEquals(ClaimVerificationState.SUPPORTED, AmarClaimVerifier().verify(input(findings = listOf(f))).verifiedClaims.single().state)
    }

    @Test fun no_recalculation_of_freshness() {
        val f = finding("The market report confirms gold prices are rising today.")
        assertEquals(f.fingerprint, AmarClaimVerifier().verify(input(findings = listOf(f))).verifiedClaims.single().supportingEvidenceIds.single())
    }

    @Test fun no_recalculation_of_relevance() {
        val f = finding("The market report confirms gold prices are rising today.")
        assertTrue(AmarClaimVerifier().verify(input(findings = listOf(f))).isDownstreamReady)
    }

    @Test fun no_recalculation_of_ranking() {
        val f = finding("The market report confirms gold prices are rising today.")
        assertEquals(1, AmarClaimVerifier().verify(input(findings = listOf(f))).verifiedClaims.size)
    }

    @Test fun no_llm_used() {
        assertTrue(AmarClaimVerifier().verify(input()).verifiedClaims.single().explanation.isNotBlank())
    }

    @Test fun structured_claims_override_answer_extraction() {
        val c = claim("The official report confirms silver prices are stable today.")
        val r = AmarClaimVerifier().verify(input(
            answer = "This answer should not be used as the claim source.",
            claims = listOf(c), findings = listOf(finding(c.text))
        ))
        assertEquals("c1", r.verifiedClaims.single().claim.id)
    }

    @Test fun is_downstream_ready_false_when_insufficient() {
        assertFalse(AmarClaimVerifier().verify(input(findings = emptyList())).isDownstreamReady)
    }

    @Test fun is_downstream_ready_false_when_conflicted() {
        val f = finding("The market report confirms gold prices are rising today.")
        assertFalse(AmarClaimVerifier().verify(input(findings = listOf(f), conflicts = setOf(f.fingerprint))).isDownstreamReady)
    }
}
