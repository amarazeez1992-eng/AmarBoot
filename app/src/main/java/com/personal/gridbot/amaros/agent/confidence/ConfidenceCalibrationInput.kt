package com.personal.gridbot.amaros.agent.confidence

import com.personal.gridbot.amaros.agent.AmarEvidenceQualityUpstreamState
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationResult

data class ConfidenceCalibrationInput(
    val rawConfidence: Double?,
    val evidenceQuality: Double?,
    val claimVerification: ClaimVerificationResult?,
    val conflictCount: Int = 0,
    val upstreamStates: AmarEvidenceQualityUpstreamState? = null
)
