package com.personal.gridbot.amaros.intelligence.advanced

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class AmarMarketRegimeEngine {
    fun classify(candles: List<AmarOhlc>): AmarRegimeObservation {
        if (candles.size < 20) return AmarRegimeObservation(AmarMarketRegime.UNKNOWN, 0.0, 0.0, 0.0, 0.0, 0.0, candles.lastOrNull()?.timestamp ?: 0L)
        val recent = candles.takeLast(20)
        val ranges = recent.map { max(1e-12, it.high - it.low) }
        val avgRange = ranges.average()
        val returns = recent.zipWithNext { a, b -> (b.close - a.close) / max(1e-12, a.close) }
        val volatility = returns.map(::abs).average()
        val path = recent.drop(1).zip(recent.dropLast(1)).sumOf { abs(it.first.close - it.second.close) }
        val trendScore = (abs(recent.last().close - recent.first().close) / max(1e-12, path)).coerceIn(0.0, 1.0)
        val volScore = (volatility / max(1e-12, avgRange / max(1e-12, recent.last().close))).coerceIn(0.0, 1.0)
        val prior = recent.dropLast(5).map { it.close }
        val last = recent.takeLast(5).map { it.close }
        val priorHigh = prior.maxOrNull() ?: recent.last().high
        val priorLow = prior.minOrNull() ?: recent.last().low
        val lastHigh = last.maxOrNull() ?: recent.last().close
        val lastLow = last.minOrNull() ?: recent.last().close
        val breakoutScore = max(if (lastHigh > priorHigh) (lastHigh - priorHigh) / max(1e-12, avgRange) else 0.0, if (lastLow < priorLow) (priorLow - lastLow) / max(1e-12, avgRange) else 0.0).coerceIn(0.0, 1.0)
        val regime = when {
            volScore > 0.80 -> AmarMarketRegime.HIGH_VOLATILITY
            breakoutScore > 0.65 -> AmarMarketRegime.BREAKOUT
            trendScore > 0.62 -> AmarMarketRegime.TREND
            volScore < 0.25 && trendScore < 0.35 -> AmarMarketRegime.LOW_VOLATILITY
            trendScore < 0.35 -> AmarMarketRegime.RANGE
            else -> AmarMarketRegime.TRANSITION
        }
        val confidence = (0.45 * trendScore + 0.35 * breakoutScore + 0.20 * (1.0 - min(1.0, abs(volScore - 0.5)))).coerceIn(0.0, 1.0)
        return AmarRegimeObservation(regime, confidence, volScore, trendScore, breakoutScore, 0.0, recent.last().timestamp)
    }
}
