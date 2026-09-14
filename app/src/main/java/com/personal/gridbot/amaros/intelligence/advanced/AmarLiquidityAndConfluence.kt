package com.personal.gridbot.amaros.intelligence.advanced

import kotlin.math.abs
import kotlin.math.max

data class AmarLiquidityLevel(
    val price: Double,
    val kind: Kind,
    val strength: Double = 1.0,
    val source: String = "market-structure"
) {
    enum class Kind { SWING_HIGH, SWING_LOW }
    init {
        require(price.isFinite() && price > 0.0)
        require(strength.isFinite() && strength in 0.0..1.0)
    }
}

data class AmarLiquiditySweep(
    val level: AmarLiquidityLevel,
    val sweptAt: Long,
    val direction: Direction,
    val excursion: Double
) {
    enum class Direction { BULLISH_REVERSAL, BEARISH_REVERSAL }
    init {
        require(sweptAt >= 0L)
        require(excursion.isFinite() && excursion >= 0.0)
    }
}

data class AmarConfluenceSignal(
    val id: String,
    val direction: Direction,
    val score: Double,
    val sourceGroup: String,
    val timeframe: String
) {
    enum class Direction { BULLISH, BEARISH, NEUTRAL }
    init {
        require(id.isNotBlank())
        require(score.isFinite() && score in 0.0..1.0)
        require(sourceGroup.isNotBlank())
        require(timeframe.isNotBlank())
    }
}

data class AmarConfluenceResult(
    val direction: AmarConfluenceSignal.Direction,
    val score: Double,
    val confidence: Double,
    val supportingSignals: Int,
    val independentSourceGroups: Int
)

class AmarLiquidityEngine {
    fun detectSweeps(candles: List<AmarOhlc>, levels: List<AmarLiquidityLevel>): List<AmarLiquiditySweep> {
        if (candles.isEmpty() || levels.isEmpty()) return emptyList()
        return candles.flatMap { candle ->
            levels.mapNotNull { level ->
                when (level.kind) {
                    AmarLiquidityLevel.Kind.SWING_HIGH -> if (candle.high > level.price && candle.close < level.price) AmarLiquiditySweep(level, candle.timestamp, AmarLiquiditySweep.Direction.BEARISH_REVERSAL, candle.high - level.price) else null
                    AmarLiquidityLevel.Kind.SWING_LOW -> if (candle.low < level.price && candle.close > level.price) AmarLiquiditySweep(level, candle.timestamp, AmarLiquiditySweep.Direction.BULLISH_REVERSAL, level.price - candle.low) else null
                }
            }
        }
    }
}

/** Combines independent source families while discounting repeated signals from the same family. */
class AmarConfluenceEngine {
    fun combine(signals: List<AmarConfluenceSignal>): AmarConfluenceResult {
        if (signals.isEmpty()) return AmarConfluenceResult(AmarConfluenceSignal.Direction.NEUTRAL, 0.0, 0.0, 0, 0)
        val grouped = signals.groupBy { it.sourceGroup }
        val groupScores = grouped.values.map { group ->
            group.sortedByDescending { it.score }.mapIndexed { index, signal ->
                signal to signal.score / (1.0 + 0.5 * index)
            }.maxByOrNull { it.second }!!
        }
        val bullish = groupScores.filter { it.first.direction == AmarConfluenceSignal.Direction.BULLISH }.sumOf { it.second }
        val bearish = groupScores.filter { it.first.direction == AmarConfluenceSignal.Direction.BEARISH }.sumOf { it.second }
        val total = max(1e-12, bullish + bearish)
        val edge = (bullish - bearish) / total
        val direction = when {
            edge > 0.15 -> AmarConfluenceSignal.Direction.BULLISH
            edge < -0.15 -> AmarConfluenceSignal.Direction.BEARISH
            else -> AmarConfluenceSignal.Direction.NEUTRAL
        }
        val score = abs(edge).coerceIn(0.0, 1.0)
        val confidence = (score * (groupScores.size.toDouble() / max(1, signals.size))).coerceIn(0.0, 1.0)
        return AmarConfluenceResult(direction, score, confidence, signals.size, grouped.size)
    }
}

/** Rejects malformed OHLC data before intelligence engines consume it. */
object AmarMarketDataQualityGate {
    fun validate(candles: List<AmarOhlc>): List<String> {
        val errors = mutableListOf<String>()
        candles.forEachIndexed { index, candle ->
            if (candle.timestamp < 0L) errors += "negative timestamp at index $index"
            if (!candle.open.isFinite() || !candle.high.isFinite() || !candle.low.isFinite() || !candle.close.isFinite()) errors += "non-finite OHLC at index $index"
            if (candle.high < max(candle.open, candle.close) || candle.low > minOf(candle.open, candle.close)) errors += "invalid OHLC bounds at index $index"
            if (candle.low > candle.high) errors += "low above high at index $index"
            if (candle.spread < 0.0 || !candle.spread.isFinite()) errors += "invalid spread at index $index"
            if (index > 0 && candle.timestamp <= candles[index - 1].timestamp) errors += "non-monotonic timestamp at index $index"
        }
        return errors
    }
}
