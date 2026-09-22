package com.personal.gridbot.amaros.agent.confidence

import com.personal.gridbot.amaros.agent.AmarClaimVerification
import com.personal.gridbot.amaros.agent.AmarClaimVerificationReport
import com.personal.gridbot.amaros.agent.AmarConfidenceCalibrationEngine
import com.personal.gridbot.amaros.agent.AmarEvidenceQualityUpstreamState
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationResult
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationState

class AmarConfidenceCalibrationAdapter(
    private val engine: AmarConfidenceCalibrationEngine = AmarConfidenceCalibrationEngine()
) : AmarConfidenceCalibrationContract {

    override fun calibrate(input: ConfidenceCalibrationInput): ConfidenceCalibrationResult {
        val raw = input.rawConfidence
            ?: return ConfidenceCalibrationResult.failed(ConfidenceCalibrationReason.MISSING_RAW_CONFIDENCE)
        val quality = input.evidenceQuality
            ?: return ConfidenceCalibrationResult.failed(ConfidenceCalibrationReason.MISSING_EVIDENCE_QUALITY)
        val claimResult = input.claimVerification
            ?: return ConfidenceCalibrationResult.failed(ConfidenceCalibrationReason.MISSING_CLAIM_VERIFICATION)

        if (raw.isNaN() || raw !in 0.0..1.0 ||
            quality.isNaN() || quality !in 0.0..1.0 ||
            input.conflictCount < 0
        ) {
            return ConfidenceCalibrationResult.failed(ConfidenceCalibrationReason.INVALID_INPUT)
        }
        if (input.upstreamStates != null && !input.upstreamStates.fullyVerified) {
            return ConfidenceCalibrationResult.failed(ConfidenceCalibrationReason.INVALID_UPSTREAM_STATE)
        }

        val legacyReport = convertToLegacy(claimResult)
        return try {
            val calibrated = engine.calibrate(raw, quality, legacyReport, input.conflictCount)
            ConfidenceCalibrationResult(
                calibratedConfidence = calibrated,
                inputSummary = mapOf(
                    "rawConfidence" to raw,
                    "evidenceQuality" to quality,
                    "conflictCount" to input.conflictCount.toDouble(),
                    "calibratedConfidence" to calibrated
                ),
                isDownstreamReady = true,
                reason = ConfidenceCalibrationReason.VALID_INPUT
            )
        } catch (_: Exception) {
            ConfidenceCalibrationResult.failed(ConfidenceCalibrationReason.CALIBRATION_FAILED)
        }
    }

    private fun convertToLegacy(result: ClaimVerificationResult): AmarClaimVerificationReport {
        val claims = buildList {
            result.verifiedClaims.forEach { claim ->
                add(
                    AmarClaimVerification(
                        claim = claim.claim.text,
                        supportingEvidence = claim.supportingEvidenceIds.size,
                        opposingEvidence = claim.opposingEvidenceIds.size,
                        accepted = claim.state == ClaimVerificationState.SUPPORTED,
                        matchedEvidence = claim.supportingEvidenceIds.size + claim.opposingEvidenceIds.size
                    )
                )
            }
            result.rejectedClaims.forEach { claim ->
                add(
                    AmarClaimVerification(
                        claim = claim.claim.text,
                        supportingEvidence = 0,
                        opposingEvidence = if (claim.state == ClaimVerificationState.OPPOSED) 1 else 0,
                        accepted = false
                    )
                )
            }
        }
        return AmarClaimVerificationReport(claims, claims.all { it.accepted })
    }
}
