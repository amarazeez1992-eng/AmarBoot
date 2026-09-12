package com.personal.gridbot.amaros.intelligence.advanced

import kotlin.math.abs

/**
 * Deterministic mutation/evolution laboratory. It generates candidates only; adoption remains human-gated.
 */
object AmarStrategyEvolutionEngine {
    enum class MutationType { ENTRY, EXIT, RISK, REGIME, EXECUTION }
    enum class Gate { REJECT, CHALLENGER_ONLY, HUMAN_REVIEW, APPROVAL_READY }

    data class Candidate(
        val id: String,
        val parentId: String,
        val mutation: MutationType,
        val description: String,
        val complexityDelta: Double
    )

    data class Score(
        val expectancyR: Double,
        val maxDrawdownR: Double,
        val profitFactor: Double,
        val sampleSize: Int,
        val oosExpectancyR: Double,
        val stressExpectancyR: Double,
        val leakageDetected: Boolean,
        val complexity: Double
    )

    data class Decision(val candidate: Candidate, val gate: Gate, val score: Double, val reasons: List<String>)

    fun mutate(parentId: String, description: String, type: MutationType, complexityDelta: Double = 0.05): Candidate {
        require(parentId.isNotBlank() && description.isNotBlank())
        val id = "$parentId-M${type.name}-${description.hashCode().toUInt().toString(16)}"
        return Candidate(id, parentId, type, description.trim(), complexityDelta.coerceAtLeast(0.0))
    }

    fun gate(candidate: Candidate, score: Score): Decision {
        val reasons = mutableListOf<String>()
        if (score.leakageDetected) return Decision(candidate, Gate.REJECT, 0.0, listOf("leakage detected"))
        if (score.sampleSize < 30) return Decision(candidate, Gate.REJECT, 0.0, listOf("insufficient sample size"))
        if (score.oosExpectancyR <= 0.0) return Decision(candidate, Gate.REJECT, 0.0, listOf("OOS expectancy is not positive"))
        if (score.maxDrawdownR <= 0.0) reasons += "drawdown data is missing or non-positive"
        if (score.profitFactor < 1.0) return Decision(candidate, Gate.REJECT, 0.0, listOf("profit factor below 1"))
        if (score.stressExpectancyR <= 0.0) return Decision(candidate, Gate.CHALLENGER_ONLY, 0.25, listOf("stress expectancy is not positive"))
        val robustness = (score.oosExpectancyR / (1.0 + abs(score.maxDrawdownR)))
        val complexityPenalty = candidate.complexityDelta.coerceIn(0.0, 1.0) * 0.25
        val composite = (robustness + (score.profitFactor - 1.0) * 0.20 + score.stressExpectancyR * 0.30 - complexityPenalty).coerceIn(-1.0, 1.0)
        return when {
            composite < 0.15 -> Decision(candidate, Gate.CHALLENGER_ONLY, composite, reasons + "weak robustness")
            composite < 0.35 -> Decision(candidate, Gate.HUMAN_REVIEW, composite, reasons + "candidate requires human comparison")
            else -> Decision(candidate, Gate.APPROVAL_READY, composite, reasons + "passes quantitative challenger gates; user approval required")
        }
    }

    fun championChallenger(champion: Score, challenger: Score): String = when {
        challenger.leakageDetected -> "CHAMPION: leakage-free requirement failed for challenger"
        challenger.oosExpectancyR <= champion.oosExpectancyR && challenger.maxDrawdownR >= champion.maxDrawdownR -> "CHAMPION: incumbent retained"
        challenger.stressExpectancyR < champion.stressExpectancyR -> "CHAMPION: incumbent retained under stress"
        challenger.profitFactor < champion.profitFactor && challenger.oosExpectancyR <= champion.oosExpectancyR -> "CHAMPION: incumbent retained"
        else -> "CHALLENGER: superior measured profile; human approval required"
    }

    fun counterfactual(base: Score, alternative: Score): Map<String, Double> = mapOf(
        "oosExpectancyDeltaR" to alternative.oosExpectancyR - base.oosExpectancyR,
        "stressExpectancyDeltaR" to alternative.stressExpectancyR - base.stressExpectancyR,
        "drawdownDeltaR" to alternative.maxDrawdownR - base.maxDrawdownR,
        "profitFactorDelta" to alternative.profitFactor - base.profitFactor,
        "sampleDelta" to (alternative.sampleSize - base.sampleSize).toDouble()
    )
}
