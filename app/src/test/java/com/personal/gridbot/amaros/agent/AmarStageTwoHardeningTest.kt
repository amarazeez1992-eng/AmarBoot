package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarStageTwoHardeningTest {
    private val quality = AmarEvidenceQualityEngine(freshnessWindowMs = 1_000L)

    private fun finding(
        uri: String,
        evidence: String,
        authority: Authority = Authority.OFFICIAL,
        stance: EvidenceStance = EvidenceStance.SUPPORTS,
        fingerprint: String = ""
    ) = ResearchFinding(
        sourceTitle = "source",
        sourceUri = uri,
        evidence = evidence,
        authority = authority,
        stance = stance,
        retrievedAtEpochMs = System.currentTimeMillis(),
        fingerprint = fingerprint
    )

    @Test
    fun duplicate_evidence_is_counted_and_not_treated_as_independent() {
        val a = finding("https://a.example/x", "gold trend is rising", fingerprint = "same")
        val b = finding("https://b.example/x", "gold trend is rising", fingerprint = "same")
        val result = quality.assess(listOf(a, b))
        assertEquals(2, result.independentSourceCount)
        assertEquals(1, result.duplicateEvidenceCount)
        assertEquals(AmarEvidenceQualityStatus.UNVERIFIABLE, result.status)
    }

    @Test
    fun duplicate_content_is_detected_even_when_valid_fingerprints_differ() {
        val a = finding("https://a.example/x", "Gold Trend Is Rising")
        val b = finding("https://b.example/x", "  gold   trend is rising  ")
        val result = quality.assess(listOf(a, b))
        assertEquals(1, result.duplicateEvidenceCount)
        assertFalse(result.items[0].uniqueEvidence)
        assertFalse(result.items[1].uniqueEvidence)
    }

    @Test
    fun same_host_sources_are_not_independent() {
        val a = finding("https://example.com/a", "gold trend is rising")
        val b = finding("https://example.com/b", "gold trend is rising")
        assertEquals(1, quality.assess(listOf(a, b)).independentSourceCount)
    }

    @Test
    fun claim_without_matching_support_is_rejected() {
        val engine = AmarClaimVerificationEngine()
        val result = engine.verify(
            "Gold is guaranteed to rise strongly tomorrow.",
            listOf(finding("https://a.example/x", "gold was stable today", stance = EvidenceStance.OPPOSES))
        )
        assertFalse(result.accepted)
        assertTrue(result.claims.isNotEmpty())
    }

    @Test
    fun calibrated_confidence_drops_with_conflicts() {
        val engine = AmarConfidenceCalibrationEngine()
        val claims = AmarClaimVerificationReport(
            listOf(AmarClaimVerification("supported claim", 1, 0, true)),
            true
        )
        val clean = engine.calibrate(.9, .9, claims, 0)
        val conflicted = engine.calibrate(.9, .9, claims, 5)
        assertTrue(conflicted < clean)
    }

    @Test
    fun evidence_quality_penalizes_old_unknown_sources() {
        val old = finding("https://old.example/x", "old claim", Authority.UNKNOWN).copy(retrievedAtEpochMs = System.currentTimeMillis() - 10_000L)
        val fresh = finding("https://fresh.example/x", "fresh claim", Authority.PRIMARY)
        assertTrue(quality.assess(listOf(fresh)).score > quality.assess(listOf(old)).score)
    }

    @Test
    fun evidence_explanation_is_machine_readable_and_matches_quality_output() {
        val result = quality.assess(listOf(finding("https://source.example/x", "gold trend is rising")))
        val item = result.items.single()
        val explanation = item.explanation

        assertEquals(item.authorityScore, explanation.authority, 0.0)
        assertEquals(item.freshnessScore, explanation.freshness, 0.0)
        assertEquals(item.independentSource, explanation.independentSource)
        assertEquals(item.uniqueEvidence, explanation.uniqueEvidence)
        assertEquals(item.integrityValid, explanation.integrityValid)
        assertEquals(item.contentValid, explanation.contentValid)
        assertEquals(item.score, explanation.finalScore, 0.0)
        assertTrue(explanation.rawScore >= explanation.finalScore)
        assertEquals(AmarEvidenceDecision.VERIFIED_THRESHOLD_MET, explanation.decision)
    }

    @Test
    fun duplicate_evidence_explanation_identifies_non_unique_content() {
        val a = finding("https://a.example/x", "gold trend is rising")
        val b = finding("https://b.example/x", "  GOLD   TREND IS RISING  ")
        val result = quality.assess(listOf(a, b))

        assertEquals(AmarEvidenceDecision.BELOW_WEAK_THRESHOLD, result.items[0].explanation.decision)
        assertFalse(result.items[0].explanation.uniqueEvidence)
        assertEquals(result.items[0].score, result.items[0].explanation.finalScore, 0.0)
    }

    @Test
    fun tampered_evidence_explanation_is_fail_closed_but_keeps_diagnostics() {
        val result = quality.assess(listOf(finding("https://source.example/x", "gold trend is rising", fingerprint = "tampered")))
        val item = result.items.single()
        val explanation = item.explanation

        assertEquals(0.0, item.score, 0.0)
        assertFalse(explanation.integrityValid)
        assertEquals(AmarEvidenceDecision.INTEGRITY_FAILURE, explanation.decision)
        assertEquals(0.0, explanation.finalScore, 0.0)
        assertTrue(explanation.rawScore > 0.0)
    }

    @Test
    fun invalid_content_explanation_is_fail_closed() {
        val result = quality.assess(listOf(finding("", "")))
        val explanation = result.items.single().explanation

        assertFalse(explanation.contentValid)
        assertEquals(0.0, explanation.finalScore, 0.0)
        assertEquals(AmarEvidenceDecision.CONTENT_FAILURE, explanation.decision)
    }
}