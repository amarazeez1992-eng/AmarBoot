package com.personal.gridbot.amaros.intelligence

import com.personal.gridbot.amaros.data.AmarDataSnapshot
import java.security.MessageDigest
import kotlin.math.abs

/**
 * Decision Intelligence foundation. Proposal only; never executes trades.
 * Stage 11 Item 5 is deliberately integrated into the existing B7 decision
 * engine so there is one decision authority instead of parallel engines.
 */
class DecisionEngine(
    private val policy: DecisionPolicy = DecisionPolicy()
) {
    enum class Direction { LONG_BIAS, SHORT_BIAS, NEUTRAL }
    enum class Status { BLOCKED, HOLD, LOW_CONFIDENCE, PROPOSED }

    data class QuantitativeAssessment(
        val realizedVolatility: Double,
        val maxDrawdown: Double,
        val historicalVar: Double,
        val riskOfRuin: Double
    ) {
        init {
            require(realizedVolatility.isFinite() && maxDrawdown.isFinite())
            require(historicalVar.isFinite() && riskOfRuin.isFinite())
            require(realizedVolatility >= 0.0 && maxDrawdown >= 0.0)
            require(historicalVar >= 0.0 && riskOfRuin >= 0.0)
        }

        fun riskScore(): Double =
            (realizedVolatility.coerceIn(0.0, 1.0) * 0.30 +
                maxDrawdown.coerceIn(0.0, 1.0) * 0.25 +
                historicalVar.coerceIn(0.0, 1.0) * 0.20 +
                riskOfRuin.coerceIn(0.0, 1.0) * 0.25).coerceIn(0.0, 1.0)
    }

    data class DecisionInput(
        val data: AmarDataSnapshot,
        val context: MarketContext,
        val verificationScore: Double = 1.0,
        val evidenceVerified: Boolean = true,
        val quantitative: QuantitativeAssessment? = null,
        val emergencyLock: Boolean = false
    ) {
        init {
            require(verificationScore.isFinite() && verificationScore in 0.0..1.0)
        }
    }

    data class DecisionProposal(
        val direction: Direction,
        val score: Double,
        val confidence: Double,
        val rationale: String,
        val executable: Boolean = false,
        val status: Status = Status.PROPOSED,
        val riskScore: Double = 0.0,
        val decisionId: String = ""
    )

    fun evaluate(data: AmarDataSnapshot, context: MarketContext): DecisionProposal =
        evaluate(DecisionInput(data = data, context = context))

    fun evaluate(input: DecisionInput): DecisionProposal {
        val score = input.context.directionalScore.coerceIn(-1.0, 1.0)
        val contextConfidence = input.context.confidence.coerceIn(0.0, 1.0)
        val evidenceQuality = evidenceQuality(input.context.evidence)
        val verification = input.verificationScore
        val verifiedFactor = if (input.evidenceVerified) 1.0 else policy.unverifiedEvidenceMultiplier
        val confidence = (
            contextConfidence * policy.contextWeight +
                evidenceQuality * verification * verifiedFactor * policy.evidenceWeight
            ).coerceIn(0.0, 1.0)

        val conflict = hasDirectionalConflict(input.context.evidence)
        val conflictMultiplier = if (conflict) policy.conflictMultiplier else 1.0
        val adjustedConfidence = (confidence * conflictMultiplier).coerceIn(0.0, 1.0)

        val quantitativeRisk = input.quantitative?.riskScore() ?: 0.0
        val uncertaintyRisk = 1.0 - adjustedConfidence
        val risk = (
            uncertaintyRisk * 0.40 +
                quantitativeRisk * 0.60
            ).coerceIn(0.0, 1.0)

        val adjustedScore = (score * adjustedConfidence * (1.0 - risk)).coerceIn(-1.0, 1.0)
        val direction = when {
            adjustedScore >= policy.longThreshold -> Direction.LONG_BIAS
            adjustedScore <= -policy.shortThreshold -> Direction.SHORT_BIAS
            else -> Direction.NEUTRAL
        }

        val blocked = input.emergencyLock ||
            risk >= policy.maxRiskScore ||
            (policy.requireVerifiedEvidence && !input.evidenceVerified)
        val lowConfidence = adjustedConfidence < policy.minimumConfidence

        val status = when {
            blocked -> Status.BLOCKED
            lowConfidence -> Status.LOW_CONFIDENCE
            direction == Direction.NEUTRAL -> Status.HOLD
            else -> Status.PROPOSED
        }
        val finalDirection = if (status == Status.BLOCKED) Direction.NEUTRAL else direction
        val rationale = buildRationale(input, finalDirection, adjustedConfidence, risk, conflict)
        val id = decisionId(input, finalDirection, adjustedConfidence, risk, rationale)

        return DecisionProposal(
            direction = finalDirection,
            score = adjustedScore,
            confidence = adjustedConfidence,
            rationale = rationale,
            executable = false,
            status = status,
            riskScore = risk,
            decisionId = id
        )
    }

    private fun evidenceQuality(evidence: List<Evidence>): Double {
        if (evidence.isEmpty()) return 0.0
        return evidence.map { abs(it.score) * it.confidence }.average().coerceIn(0.0, 1.0)
    }

    private fun hasDirectionalConflict(evidence: List<Evidence>): Boolean {
        val hasPositive = evidence.any { it.score >= 0.25 && it.confidence >= 0.50 }
        val hasNegative = evidence.any { it.score <= -0.25 && it.confidence >= 0.50 }
        return hasPositive && hasNegative
    }

    private fun buildRationale(
        input: DecisionInput,
        direction: Direction,
        confidence: Double,
        risk: Double,
        conflict: Boolean
    ): String = buildString {
        append("${input.context.regime} • ${input.context.explanation}")
        append(" • ${input.data.market.symbol}/${input.data.market.timeframe}")
        append(" • direction=$direction")
        append(" • confidence=${"%.4f".format(java.util.Locale.US, confidence)}")
        append(" • risk=${"%.4f".format(java.util.Locale.US, risk)}")
        append(" • conflict=$conflict")
        if (input.quantitative != null) append(" • quantitative=true")
    }

    private fun decisionId(
        input: DecisionInput,
        direction: Direction,
        confidence: Double,
        risk: Double,
        rationale: String
    ): String {
        val canonical = listOf(
            input.data.market.symbol,
            input.data.market.timeframe,
            input.data.market.timestampEpochMs,
            input.context.directionalScore,
            input.context.confidence,
            input.verificationScore,
            input.evidenceVerified,
            input.emergencyLock,
            direction,
            confidence,
            risk,
            rationale
        ).joinToString("|")
        val digest = MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    data class DecisionPolicy(
        val contextWeight: Double = 0.45,
        val evidenceWeight: Double = 0.55,
        val unverifiedEvidenceMultiplier: Double = 0.50,
        val conflictMultiplier: Double = 0.70,
        val minimumConfidence: Double = 0.55,
        val maxRiskScore: Double = 0.75,
        val longThreshold: Double = 0.20,
        val shortThreshold: Double = 0.20,
        val requireVerifiedEvidence: Boolean = true
    ) {
        init {
            require(contextWeight.isFinite() && evidenceWeight.isFinite())
            require(contextWeight >= 0.0 && evidenceWeight >= 0.0)
            require((contextWeight + evidenceWeight).isFinite() && contextWeight + evidenceWeight > 0.0)
            require(unverifiedEvidenceMultiplier.isFinite() && unverifiedEvidenceMultiplier in 0.0..1.0)
            require(conflictMultiplier.isFinite() && conflictMultiplier in 0.0..1.0)
            require(minimumConfidence.isFinite() && minimumConfidence in 0.0..1.0)
            require(maxRiskScore.isFinite() && maxRiskScore in 0.0..1.0)
            require(longThreshold.isFinite() && longThreshold in 0.0..1.0)
            require(shortThreshold.isFinite() && shortThreshold in 0.0..1.0)
        }
    }
}
