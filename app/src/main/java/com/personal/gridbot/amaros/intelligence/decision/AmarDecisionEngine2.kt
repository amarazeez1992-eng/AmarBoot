package com.personal.gridbot.amaros.intelligence.decision

import com.personal.gridbot.amaros.intelligence.MarketContext
import com.personal.gridbot.amaros.intelligence.verification.AmarVerificationReport
import com.personal.gridbot.amaros.intelligence.verification.AmarVerificationStatus
import java.security.MessageDigest
import kotlin.math.abs

/** Stage 11 / 5 — deterministic decision intelligence. Proposal only; never executes. */
class AmarDecisionEngine2(
    private val policy: AmarDecisionPolicy = AmarDecisionPolicy()
) {
    fun decide(input: AmarDecisionInput): AmarDecisionResult {
        require(input.nowEpochMs >= 0L)
        val directional = input.context.directionalScore.coerceIn(-1.0, 1.0)
        val contextConfidence = input.context.confidence.coerceIn(0.0, 1.0)
        val verificationScore = input.verification?.score?.coerceIn(0.0, 1.0) ?: 0.0
        val verificationReady = input.verification?.status == AmarVerificationStatus.VERIFIED
        val hasConflict = input.verification?.conflicts?.isNotEmpty() == true
        val conflictPenalty = if (hasConflict) policy.conflictMultiplier else 1.0
        val evidenceFactor = if (input.verification == null) policy.unverifiedMultiplier else verificationScore
        val weightTotal = policy.contextWeight + policy.evidenceWeight
        val contextWeight = policy.contextWeight / weightTotal
        val evidenceWeight = policy.evidenceWeight / weightTotal
        val confidence = ((contextConfidence * contextWeight + evidenceFactor * evidenceWeight) * conflictPenalty)
            .coerceIn(0.0, 1.0)
        val risk = riskScore(input, directional)
        val adjustedScore = directional * confidence * (1.0 - risk)
        val direction = when {
            adjustedScore >= policy.longThreshold -> AmarDecisionDirection.LONG_BIAS
            adjustedScore <= -policy.shortThreshold -> AmarDecisionDirection.SHORT_BIAS
            else -> AmarDecisionDirection.NEUTRAL
        }
        val blocked = input.emergencyLock || risk >= policy.maxRiskScore || (!verificationReady && policy.requireVerifiedEvidence)
        val status = when {
            blocked -> AmarDecisionStatus.BLOCKED
            direction == AmarDecisionDirection.NEUTRAL -> AmarDecisionStatus.HOLD
            confidence < policy.minConfidence -> AmarDecisionStatus.LOW_CONFIDENCE
            else -> AmarDecisionStatus.PROPOSED
        }
        val finalDirection = if (status == AmarDecisionStatus.BLOCKED) AmarDecisionDirection.NEUTRAL else direction
        val rationale = buildRationale(input, finalDirection, confidence, risk, verificationReady)
        val alternatives = listOf(AmarDecisionDirection.LONG_BIAS, AmarDecisionDirection.SHORT_BIAS, AmarDecisionDirection.NEUTRAL)
            .filterNot { it == finalDirection }
        return AmarDecisionResult(
            decisionId = stableId(input, finalDirection, status, confidence, risk),
            direction = finalDirection,
            status = status,
            score = adjustedScore.coerceIn(-1.0, 1.0),
            confidence = confidence,
            riskScore = risk,
            rationale = rationale,
            alternatives = alternatives,
            verifiedEvidence = verificationReady,
            executable = false
        )
    }

    private fun riskScore(input: AmarDecisionInput, directional: Double): Double {
        val volatilityRisk = input.volatility.coerceIn(0.0, 1.0)
        val spreadRisk = input.spreadRatio.coerceIn(0.0, 1.0)
        val uncertainty = 1.0 - input.context.confidence.coerceIn(0.0, 1.0)
        val disagreementValues = input.context.evidence.map { abs(it.score - directional) * it.confidence }
        val directionalDisagreement = if (disagreementValues.isEmpty()) 0.0 else disagreementValues.average()
        val baseRisk = volatilityRisk * 0.30 + spreadRisk * 0.20 + uncertainty * 0.20 + directionalDisagreement.coerceIn(0.0, 1.0) * 0.10
        val quantitativeRisk = input.quantitative?.riskScore ?: 0.0
        return (baseRisk + quantitativeRisk * 0.20).coerceIn(0.0, 1.0)
    }

    private fun buildRationale(input: AmarDecisionInput, direction: AmarDecisionDirection, confidence: Double, risk: Double, verified: Boolean): String =
        buildString {
            append("direction=").append(direction.name)
            append("; confidence=").append("%.4f".format(java.util.Locale.US, confidence))
            append("; risk=").append("%.4f".format(java.util.Locale.US, risk))
            append("; evidenceVerified=").append(verified)
            append("; regime=").append(input.context.regime)
            input.quantitative?.let { append("; quantRisk=").append("%.4f".format(java.util.Locale.US, it.riskScore)) }
            if (input.emergencyLock) append("; emergencyLock=true")
        }

    private fun stableId(input: AmarDecisionInput, direction: AmarDecisionDirection, status: AmarDecisionStatus, confidence: Double, risk: Double): String {
        val raw = listOf(input.decisionKey, direction.name, status.name, "%.8f".format(java.util.Locale.US, confidence), "%.8f".format(java.util.Locale.US, risk)).joinToString("|")
        return MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }.take(24)
    }
}

data class AmarDecisionPolicy(
    val contextWeight: Double = 0.45,
    val evidenceWeight: Double = 0.55,
    val unverifiedMultiplier: Double = 0.50,
    val conflictMultiplier: Double = 0.70,
    val minConfidence: Double = 0.55,
    val maxRiskScore: Double = 0.75,
    val longThreshold: Double = 0.20,
    val shortThreshold: Double = 0.20,
    val requireVerifiedEvidence: Boolean = true
) {
    init {
        require(contextWeight.isFinite() && evidenceWeight.isFinite())
        require(contextWeight >= 0.0 && evidenceWeight >= 0.0)
        require((contextWeight + evidenceWeight).isFinite())
        require(contextWeight + evidenceWeight > 0.0)
        require(unverifiedMultiplier.isFinite() && unverifiedMultiplier in 0.0..1.0)
        require(conflictMultiplier.isFinite() && conflictMultiplier in 0.0..1.0)
        require(minConfidence.isFinite() && minConfidence in 0.0..1.0)
        require(maxRiskScore.isFinite() && maxRiskScore in 0.0..1.0)
        require(longThreshold.isFinite() && longThreshold in 0.0..1.0)
        require(shortThreshold.isFinite() && shortThreshold in 0.0..1.0)
    }
}

data class AmarQuantitativeAssessment(
    val realizedVolatility: Double,
    val maxDrawdown: Double,
    val historicalVar: Double,
    val riskOfRuin: Double
) {
    val riskScore: Double
        get() = (realizedVolatility * 0.30 + maxDrawdown * 0.25 + historicalVar * 0.20 + riskOfRuin * 0.25).coerceIn(0.0, 1.0)

    init {
        require(realizedVolatility in 0.0..1.0)
        require(maxDrawdown in 0.0..1.0)
        require(historicalVar in 0.0..1.0)
        require(riskOfRuin in 0.0..1.0)
    }
}

data class AmarDecisionInput(
    val decisionKey: String,
    val context: MarketContext,
    val volatility: Double = 0.0,
    val spreadRatio: Double = 0.0,
    val verification: AmarVerificationReport? = null,
    val quantitative: AmarQuantitativeAssessment? = null,
    val emergencyLock: Boolean = false,
    val nowEpochMs: Long = 0L
) {
    init { require(decisionKey.isNotBlank()) }
}

enum class AmarDecisionDirection { LONG_BIAS, SHORT_BIAS, NEUTRAL }

enum class AmarDecisionStatus { PROPOSED, HOLD, LOW_CONFIDENCE, BLOCKED }

data class AmarDecisionResult(
    val decisionId: String,
    val direction: AmarDecisionDirection,
    val status: AmarDecisionStatus,
    val score: Double,
    val confidence: Double,
    val riskScore: Double,
    val rationale: String,
    val alternatives: List<AmarDecisionDirection>,
    val verifiedEvidence: Boolean,
    val executable: Boolean
) {
    init {
        require(score in -1.0..1.0)
        require(confidence in 0.0..1.0)
        require(riskScore in 0.0..1.0)
        require(!executable)
    }
}
