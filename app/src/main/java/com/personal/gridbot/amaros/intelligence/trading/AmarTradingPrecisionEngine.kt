package com.personal.gridbot.amaros.intelligence.trading

import kotlin.math.max
import kotlin.math.min

/** Deterministic decision-quality gate. It measures evidence quality, not profit probability. */
object AmarTradingPrecisionEngine {
    data class Input(
        val marketQuality: Double,
        val regimeConfidence: Double,
        val strategyQuality: Double,
        val validationQuality: Double,
        val evidenceQuality: Double,
        val executionReadiness: Double,
        val dataCompleteness: Double,
        val uncertaintyPenalty: Double = 0.0
    )

    data class Report(
        val scorePct: Double,
        val confidencePct: Double,
        val uncertaintyPct: Double,
        val gate: Gate,
        val reasons: List<String>
    )

    enum class Gate { BLOCKED, RESEARCH_ONLY, PAPER_TEST, HUMAN_REVIEW, READY_FOR_APPROVAL }

    fun evaluate(input: Input): Report {
        val values = listOf(
            "market" to input.marketQuality,
            "regime" to input.regimeConfidence,
            "strategy" to input.strategyQuality,
            "validation" to input.validationQuality,
            "evidence" to input.evidenceQuality,
            "execution" to input.executionReadiness,
            "data" to input.dataCompleteness
        )
        val score = values.map { it.second.coerceIn(0.0, 1.0) }.average()
        val uncertainty = (1.0 - score + input.uncertaintyPenalty.coerceIn(0.0, 1.0) * 0.5).coerceIn(0.0, 1.0)
        val confidence = (score * (1.0 - uncertainty * 0.55)).coerceIn(0.0, 1.0)
        val weak = values.filter { it.second < 0.55 }.map { it.first }
        val gate = when {
            input.dataCompleteness < 0.40 || input.marketQuality < 0.40 -> Gate.BLOCKED
            input.validationQuality < 0.45 || input.strategyQuality < 0.50 -> Gate.RESEARCH_ONLY
            input.validationQuality < 0.70 || input.evidenceQuality < 0.60 -> Gate.PAPER_TEST
            input.executionReadiness < 0.70 -> Gate.HUMAN_REVIEW
            else -> Gate.READY_FOR_APPROVAL
        }
        val reasons = buildList {
            if (weak.isNotEmpty()) add("Weak dimensions: ${weak.joinToString()}")
            if (input.uncertaintyPenalty > 0.0) add("Explicit uncertainty penalty is active")
            add("This score is decision-quality confidence, not a probability of profit")
            add("Gate=$gate")
        }
        return Report(score * 100.0, confidence * 100.0, uncertainty * 100.0, gate, reasons)
    }

    fun defaultResearchGate(): Report = evaluate(
        Input(
            marketQuality = 0.45,
            regimeConfidence = 0.35,
            strategyQuality = 0.50,
            validationQuality = 0.25,
            evidenceQuality = 0.40,
            executionReadiness = 0.20,
            dataCompleteness = 0.35,
            uncertaintyPenalty = 0.30
        )
    )
}
