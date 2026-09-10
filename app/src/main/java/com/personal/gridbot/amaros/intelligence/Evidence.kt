package com.personal.gridbot.amaros.intelligence

enum class EvidenceType { TREND, MOMENTUM, VOLATILITY, STRUCTURE, VOLUME, SESSION, REGIME }

data class Evidence(
    val type: EvidenceType,
    val score: Double,
    val confidence: Double,
    val explanation: String
) {
    init {
        require(score in -1.0..1.0)
        require(confidence in 0.0..1.0)
    }
}

data class MarketContext(
    val directionalScore: Double,
    val confidence: Double,
    val regime: String,
    val evidence: List<Evidence>,
    val explanation: String
)
