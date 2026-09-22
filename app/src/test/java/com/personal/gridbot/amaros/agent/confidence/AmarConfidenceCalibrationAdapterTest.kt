package com.personal.gridbot.amaros.agent.confidence

import com.personal.gridbot.amaros.agent.AmarConfidenceCalibrationEngine
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationResult
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationState
import com.personal.gridbot.amaros.agent.claim.RejectedClaim
import com.personal.gridbot.amaros.agent.claim.StructuredClaim
import com.personal.gridbot.amaros.agent.claim.VerifiedClaim
import org.junit.Assert.*
import org.junit.Test

class AmarConfidenceCalibrationAdapterTest {
    private fun claim(id: String, text: String = "Gold prices are rising today."): StructuredClaim =
        StructuredClaim(id, text, "gold", "price", "rising")

    private fun result(state: ClaimVerificationState = ClaimVerificationState.SUPPORTED): ClaimVerificationResult =
        if (state == ClaimVerificationState.SUPPORTED) ClaimVerificationResult(
            listOf(VerifiedClaim(claim("a"), state, listOf("e1"), emptyList(), "supported")),
            emptyList(), true
        ) else ClaimVerificationResult(
            emptyList(),
            listOf(RejectedClaim(claim("a"), state, "rejected")),
            false
        )

    private fun input(
        raw: Double? = .8,
        quality: Double? = .9,
        claims: ClaimVerificationResult? = result(),
        conflicts: Int = 0
    ) = ConfidenceCalibrationInput(raw, quality, claims, conflicts)

    @Test fun valid_input_returns_calibrated_confidence() {
        val r = AmarConfidenceCalibrationAdapter().calibrate(input())
        assertNotNull(r.calibratedConfidence)
        assertTrue(r.isDownstreamReady)
    }

    @Test fun missing_raw_confidence_returns_error() {
        assertEquals(ConfidenceCalibrationReason.MISSING_RAW_CONFIDENCE, AmarConfidenceCalibrationAdapter().calibrate(input(raw = null)).reason)
    }

    @Test fun missing_evidence_quality_returns_error() {
        assertEquals(ConfidenceCalibrationReason.MISSING_EVIDENCE_QUALITY, AmarConfidenceCalibrationAdapter().calibrate(input(quality = null)).reason)
    }

    @Test fun missing_claim_verification_returns_error() {
        assertEquals(ConfidenceCalibrationReason.MISSING_CLAIM_VERIFICATION, AmarConfidenceCalibrationAdapter().calibrate(input(claims = null)).reason)
    }

    @Test fun valid_input_calls_existing_engine() {
        val engine = AmarConfidenceCalibrationEngine()
        val r = AmarConfidenceCalibrationAdapter(engine).calibrate(input())
        assertEquals(ConfidenceCalibrationReason.VALID_INPUT, r.reason)
    }

    @Test fun claim_verification_result_is_converted() {
        val r = AmarConfidenceCalibrationAdapter().calibrate(input(claims = result(ClaimVerificationState.OPPOSED)))
        assertNotNull(r)
    }

    @Test fun conflict_count_passed_to_engine() {
        val zero = AmarConfidenceCalibrationAdapter().calibrate(input(conflicts = 0)).calibratedConfidence
        val two = AmarConfidenceCalibrationAdapter().calibrate(input(conflicts = 2)).calibratedConfidence
        assertNotEquals(zero, two)
    }

    @Test fun result_preserves_input_summary() {
        val r = AmarConfidenceCalibrationAdapter().calibrate(input(conflicts = 2))
        assertEquals(.8, r.inputSummary["rawConfidence"]!!, 0.0)
        assertEquals(.9, r.inputSummary["evidenceQuality"]!!, 0.0)
        assertEquals(2.0, r.inputSummary["conflictCount"]!!, 0.0)
    }

    @Test fun is_deterministic() {
        val a = AmarConfidenceCalibrationAdapter().calibrate(input())
        val b = AmarConfidenceCalibrationAdapter().calibrate(input())
        assertEquals(a, b)
    }

    @Test fun is_stateless() {
        val adapter = AmarConfidenceCalibrationAdapter()
        assertEquals(adapter.calibrate(input()), adapter.calibrate(input()))
    }

    @Test fun no_recalculation_of_evidence_quality() {
        val r = AmarConfidenceCalibrationAdapter().calibrate(input(quality = .42))
        assertEquals(.42, r.inputSummary["evidenceQuality"]!!, 0.0)
    }

    @Test fun no_recalculation_of_claim_verification() {
        val supported = AmarConfidenceCalibrationAdapter().calibrate(input(claims = result(ClaimVerificationState.SUPPORTED)))
        val opposed = AmarConfidenceCalibrationAdapter().calibrate(input(claims = result(ClaimVerificationState.OPPOSED)))
        assertTrue((supported.calibratedConfidence ?: 0.0) > (opposed.calibratedConfidence ?: 0.0))
    }

    @Test fun adapter_uses_existing_calibration_authority() {
        val r = AmarConfidenceCalibrationAdapter(AmarConfidenceCalibrationEngine()).calibrate(input())
        assertEquals(ConfidenceCalibrationReason.VALID_INPUT, r.reason)
    }

    @Test fun fail_closed_on_invalid_input() {
        assertEquals(ConfidenceCalibrationReason.INVALID_INPUT, AmarConfidenceCalibrationAdapter().calibrate(input(raw = -0.1)).reason)
        assertEquals(ConfidenceCalibrationReason.INVALID_INPUT, AmarConfidenceCalibrationAdapter().calibrate(input(quality = 1.1)).reason)
        assertEquals(ConfidenceCalibrationReason.INVALID_INPUT, AmarConfidenceCalibrationAdapter().calibrate(input(conflicts = -1)).reason)
        val invalidUpstream = com.personal.gridbot.amaros.agent.AmarEvidenceQualityUpstreamState(
            true, true, true, true, true, true, true, true, false
        )
        assertEquals(
            ConfidenceCalibrationReason.INVALID_UPSTREAM_STATE,
            AmarConfidenceCalibrationAdapter().calibrate(ConfidenceCalibrationInput(.8, .9, result(), 0, invalidUpstream)).reason
        )
    }
    @Test fun output_is_downstream_ready_when_valid() {
        assertTrue(AmarConfidenceCalibrationAdapter().calibrate(input()).isDownstreamReady)
    }
}
