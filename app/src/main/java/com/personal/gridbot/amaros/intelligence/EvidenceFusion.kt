package com.personal.gridbot.amaros.intelligence

object EvidenceFusion {
    fun fuse(evidence: List<Evidence>): MarketContext {
        if (evidence.isEmpty()) return MarketContext(0.0, 0.0, "UNKNOWN", emptyList(), "لا توجد أدلة كافية")
        val weight = evidence.sumOf { it.confidence }.coerceAtLeast(1e-9)
        val score = (evidence.sumOf { it.score * it.confidence } / weight).coerceIn(-1.0, 1.0)
        val confidence = (evidence.map { it.confidence }.average()).coerceIn(0.0, 1.0)
        val regime = when {
            evidence.any { it.type == EvidenceType.VOLATILITY && it.score > 0.65 } -> "HIGH_VOLATILITY"
            kotlin.math.abs(score) < 0.18 -> "NEUTRAL"
            score > 0.0 -> "BULLISH_BIAS"
            else -> "BEARISH_BIAS"
        }
        val explanation = evidence.joinToString(" • ") { it.explanation }
        return MarketContext(score, confidence, regime, evidence, explanation)
    }
}
