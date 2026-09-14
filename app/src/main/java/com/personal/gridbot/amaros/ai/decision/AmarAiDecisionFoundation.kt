package com.personal.gridbot.amaros.ai.decision

/**
 * Read-only foundation for the future unified market decision room.
 * No broker execution and no trading recommendation is generated here.
 */
data class AmarDecisionEvidence(
    val source: String,
    val signal: String,
    val weight: Double = 1.0,
    val supported: Boolean = true
)

data class AmarUnifiedDecision(
    val direction: AmarDecisionDirection,
    val score: Double,
    val evidence: List<AmarDecisionEvidence>
)

enum class AmarDecisionDirection { BUY, SELL, NEUTRAL }

class AmarDecisionEngine {
    fun synthesize(evidence: List<AmarDecisionEvidence>): AmarUnifiedDecision {
        require(evidence.all { it.weight.isFinite() && it.weight >= 0.0 })
        val valid = evidence.filter { it.supported && it.weight > 0.0 }
        val buy = valid.filter { it.signal.equals("BUY", true) }.sumOf { it.weight }
        val sell = valid.filter { it.signal.equals("SELL", true) }.sumOf { it.weight }
        val total = buy + sell
        val score = if (total == 0.0) 0.0 else ((buy - sell) / total).coerceIn(-1.0, 1.0)
        val direction = when {
            score > 0.0 -> AmarDecisionDirection.BUY
            score < 0.0 -> AmarDecisionDirection.SELL
            else -> AmarDecisionDirection.NEUTRAL
        }
        return AmarUnifiedDecision(direction, score, valid)
    }
}

/** Deterministic routing/cache boundary for expensive repeated analysis. */
class AmarAiAnalysisCache<K, V> {
    private val values = LinkedHashMap<K, V>()

    fun get(key: K): V? = values[key]
    fun put(key: K, value: V) { values[key] = value }
    fun clear() { values.clear() }
}
