package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HallucinationDetectionGuardTest {
    private val blocking = UnsupportedClaimBlocking.evaluate(
        AmarClaimVerificationReport(
            claims = listOf(AmarClaimVerification("A supported claim.", 3, 0, true, 3, 0)),
            accepted = true
        )
    )

    private val attribution = SourceAttributionEnforcer.evaluate(
        AmarClaimVerificationReport(
            claims = listOf(AmarClaimVerification("A supported claim.", 3, 0, true, 3, 0)),
            accepted = true
        ),
        listOf(
            ResearchFinding("S1", "https://example.com/1", "support"),
            ResearchFinding("S2", "https://example.com/2", "support"),
            ResearchFinding("S3", "https://example.com/3", "support")
        )
    )

    private fun evaluate(vararg claims: AmarClaimVerification): HallucinationDetectionResult {
        val report = AmarClaimVerificationReport(claims.toList(), claims.isNotEmpty() && claims.all { it.accepted })
        return HallucinationDetectionGuard.evaluate(report, blocking, attribution, claims.joinToString(" ") { it.claim })
    }

    @Test
    fun no_hallucination_when_clean() {
        val result = evaluate(
            AmarClaimVerification("The market report has supporting evidence.", 3, 0, true, 3, 0)
        )
        assertFalse(result.hallucinationDetected)
        assertEquals(HallucinationSeverity.NONE, result.severity)
    }

    @Test
    fun certainty_language_mismatch_detected() {
        val result = evaluate(
            AmarClaimVerification("Gold will definitely rise tomorrow.", 1, 0, true, 1, 0)
        )
        assertTrue(result.reasons.contains(HallucinationIndicator.CERTAINTY_LANGUAGE_MISMATCH))
        assertEquals(HallucinationSeverity.HIGH, result.severity)
    }

    @Test
    fun numeric_claim_without_evidence_detected() {
        val result = evaluate(
            AmarClaimVerification("The price is 2500 dollars.", 0, 0, false, 0, 0)
        )
        assertTrue(result.reasons.contains(HallucinationIndicator.NUMERIC_CLAIM_WITHOUT_EVIDENCE))
        assertEquals(HallucinationSeverity.MEDIUM, result.severity)
    }

    @Test
    fun entity_mention_mismatch_detected() {
        val result = evaluate(
            AmarClaimVerification("The broker Apple changed its policy.", 0, 0, false, 0, 0)
        )
        assertTrue(result.reasons.contains(HallucinationIndicator.ENTITY_MENTION_MISMATCH))
        assertEquals(HallucinationSeverity.MEDIUM, result.severity)
    }

    @Test
    fun confidence_inflation_detected() {
        val result = evaluate(
            AmarClaimVerification("The verified claim is supported.", 1, 0, true, 1, 0)
        )
        assertTrue(result.reasons.contains(HallucinationIndicator.CONFIDENCE_INFLATION))
        assertEquals(HallucinationSeverity.MEDIUM, result.severity)
    }

    @Test
    fun repetition_suspicion_detected() {
        val claim = AmarClaimVerification("The same supported claim is repeated.", 3, 0, true, 3, 0)
        val result = evaluate(claim, claim)
        assertTrue(result.reasons.contains(HallucinationIndicator.REPETITION_SUSPICION))
        assertEquals(HallucinationSeverity.LOW, result.severity)
    }

    @Test
    fun multiple_indicators_severity_high() {
        val result = evaluate(
            AmarClaimVerification("Gold will definitely rise tomorrow.", 1, 0, true, 1, 0),
            AmarClaimVerification("The verified claim is supported.", 1, 0, true, 1, 0)
        )
        assertTrue(result.reasons.contains(HallucinationIndicator.CERTAINTY_LANGUAGE_MISMATCH))
        assertTrue(result.reasons.contains(HallucinationIndicator.CONFIDENCE_INFLATION))
        assertEquals(HallucinationSeverity.HIGH, result.severity)
    }

    @Test
    fun determinism() {
        val claims = arrayOf(
            AmarClaimVerification("The same supported claim is repeated.", 3, 0, true, 3, 0),
            AmarClaimVerification("The same supported claim is repeated.", 3, 0, true, 3, 0)
        )
        assertEquals(evaluate(*claims), evaluate(*claims))
    }

    @Test
    fun regression() {
        val claim = AmarClaimVerification("A supported claim.", 3, 0, true, 3, 0)
        val report = AmarClaimVerificationReport(listOf(claim), true)
        val result = HallucinationDetectionGuard.evaluate(report, blocking, attribution, claim.claim)
        assertEquals(claim, report.claims.single())
        assertTrue(report.accepted)
        assertFalse(result.hallucinationDetected)
    }

    @Test
    fun no_reverification() {
        val claim = AmarClaimVerification("A supported claim.", 3, 0, true, 3, 0)
        val report = AmarClaimVerificationReport(listOf(claim), true)
        val before = report.copy(claims = report.claims.toList())
        val first = HallucinationDetectionGuard.evaluate(report, blocking, attribution, claim.claim)
        val second = HallucinationDetectionGuard.evaluate(report, blocking, attribution, claim.claim)
        assertEquals(first, second)
        assertEquals(before, report)
    }
}
