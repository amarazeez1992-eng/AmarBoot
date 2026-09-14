package com.personal.gridbot.amaros.agent

/** Registry that rejects missing/unsupported licensing before a source becomes usable. */
class AmarSourceRegistry(
    private val allowedSpdxLicenses: Set<String> = setOf("MIT", "Apache-2.0", "BSD-2-Clause", "BSD-3-Clause")
) {
    private val sources = linkedMapOf<String, AmarSourceProvenance>()

    fun register(source: AmarSourceProvenance) {
        require(source.license.spdxId in allowedSpdxLicenses) { "Unsupported source license: ${source.license.spdxId}" }
        require(source.sourceId.startsWith("amar-") || source.repository != null) { "External source requires repository provenance" }
        val existing = sources[source.sourceId]
        require(existing == null || existing == source) { "Conflicting provenance for ${source.sourceId}" }
        sources[source.sourceId] = source
    }

    fun get(sourceId: String): AmarSourceProvenance? = sources[sourceId]
    fun all(): List<AmarSourceProvenance> = sources.values.toList()
}

class AmarStatisticalEngine {
    fun summarize(bars: List<AmarMarketBar>): AmarStatisticalSnapshot {
        if (bars.size < 2) return AmarStatisticalSnapshot(0, 0.0, 0.0, 0.0, 0.0)
        val returns = bars.zipWithNext().map { (a, b) -> b.close / a.close - 1.0 }
        require(returns.all { it.isFinite() })
        val mean = returns.average()
        val variance = if (returns.size > 1) returns.sumOf { (it - mean) * (it - mean) } / (returns.size - 1) else 0.0
        val volatility = kotlin.math.sqrt(variance)
        val last = returns.last()
        val z = if (volatility == 0.0) 0.0 else (last - mean) / volatility
        val positive = returns.count { it > 0.0 }.toDouble() / returns.size
        return AmarStatisticalSnapshot(returns.size, mean, volatility, z, positive)
    }
}

class AmarMarketStructureEngine {
    fun inspect(bars: List<AmarMarketBar>, lookback: Int = 20): AmarMarketStructureSnapshot {
        require(lookback >= 2)
        require(bars.size >= lookback + 1)
        val current = bars.last()
        val previousWindow = bars.takeLast(lookback + 1).dropLast(1)
        val swingHigh = previousWindow.maxOf { it.high }
        val swingLow = previousWindow.minOf { it.low }
        val previous = bars[bars.lastIndex - 1]
        val signal = when {
            current.high > swingHigh && current.close > swingHigh -> AmarStructureSignal.BREAKOUT_UP
            current.low < swingLow && current.close < swingLow -> AmarStructureSignal.BREAKOUT_DOWN
            current.high > previous.high && current.low > previous.low -> AmarStructureSignal.HIGHER_HIGH
            current.high < previous.high && current.low < previous.low -> AmarStructureSignal.LOWER_LOW
            current.high > previous.high && current.low <= previous.low -> AmarStructureSignal.HIGHER_HIGH
            current.high <= previous.high && current.low < previous.low -> AmarStructureSignal.LOWER_LOW
            current.close > previous.close -> AmarStructureSignal.HIGHER_LOW
            current.close < previous.close -> AmarStructureSignal.LOWER_HIGH
            else -> AmarStructureSignal.RANGE
        }
        return AmarMarketStructureSnapshot(signal, swingHigh, swingLow, lookback)
    }
}

class AmarStageSixAggregationEngine(
    private val indicatorAdapter: AmarIndicatorAdapter = AmarBuiltInIndicatorAdapter(),
    private val statisticsEngine: AmarStatisticalEngine = AmarStatisticalEngine(),
    private val structureEngine: AmarMarketStructureEngine = AmarMarketStructureEngine()
) {
    fun analyze(
        bars: List<AmarMarketBar>,
        indicatorRequests: List<AmarIndicatorRequest> = listOf(
            AmarIndicatorRequest(AmarIndicatorKind.SMA, 20),
            AmarIndicatorRequest(AmarIndicatorKind.EMA, 20),
            AmarIndicatorRequest(AmarIndicatorKind.RSI, 14),
            AmarIndicatorRequest(AmarIndicatorKind.ATR, 14)
        ),
        structureLookback: Int = 20
    ): AmarMarketIntelligenceReport {
        require(bars.isNotEmpty())
        require(bars.zipWithNext().all { it.first.timestampEpochMs < it.second.timestampEpochMs })
        val indicators = indicatorRequests.distinct().map { indicatorAdapter.calculate(bars, it) }
        val stats = statisticsEngine.summarize(bars)
        val structure = if (bars.size > structureLookback) structureEngine.inspect(bars, structureLookback)
        else AmarMarketStructureSnapshot(AmarStructureSignal.RANGE, bars.minOf { it.low }, bars.maxOf { it.high }, structureLookback)
        val score = deterministicScore(indicators, stats, structure)
        val confidence = (0.25 + 0.15 * indicators.count { it.points.isNotEmpty() } + 0.30 * (1.0 - kotlin.math.min(1.0, kotlin.math.abs(stats.zScore) / 4.0)) + 0.30).coerceIn(0.0, 1.0)
        return AmarMarketIntelligenceReport(
            bars = bars.size,
            indicators = indicators,
            statistics = stats,
            structure = structure,
            score = score,
            confidence = confidence,
            reasons = listOf("deterministic-indicator-analysis", "statistical-return-analysis", "market-structure-analysis")
        )
    }

    private fun deterministicScore(
        indicators: List<AmarIndicatorResult>,
        stats: AmarStatisticalSnapshot,
        structure: AmarMarketStructureSnapshot
    ): Double {
        val sma = indicators.firstOrNull { it.kind == AmarIndicatorKind.SMA }?.points?.lastOrNull()?.value
        val ema = indicators.firstOrNull { it.kind == AmarIndicatorKind.EMA }?.points?.lastOrNull()?.value
        val rsi = indicators.firstOrNull { it.kind == AmarIndicatorKind.RSI }?.points?.lastOrNull()?.value
        var score = 0.0
        if (sma != null && ema != null) score += if (ema > sma) 0.20 else if (ema < sma) -0.20 else 0.0
        if (rsi != null) score += when { rsi >= 55.0 -> 0.20; rsi <= 45.0 -> -0.20; else -> 0.0 }
        score += when (structure.signal) {
            AmarStructureSignal.BREAKOUT_UP, AmarStructureSignal.HIGHER_HIGH, AmarStructureSignal.HIGHER_LOW -> 0.35
            AmarStructureSignal.BREAKOUT_DOWN, AmarStructureSignal.LOWER_HIGH, AmarStructureSignal.LOWER_LOW -> -0.35
            AmarStructureSignal.RANGE -> 0.0
        }
        score += (stats.meanReturn * 100.0).coerceIn(-0.25, 0.25)
        return score.coerceIn(-1.0, 1.0)
    }
}
