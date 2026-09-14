package com.personal.gridbot.amaros.agent

import java.util.concurrent.Callable
import java.util.concurrent.Executors

/** Generic licensed adapter boundary for third-party indicator engines. */
class AmarLicensedIndicatorAdapter(
    override val provenance: AmarSourceProvenance,
    private val calculator: (List<AmarMarketBar>, AmarIndicatorRequest) -> AmarIndicatorResult
) : AmarIndicatorAdapter {
    init {
        require(provenance.sourceType == AmarSourceType.INDICATOR_ENGINE)
        require(provenance.license.spdxId in setOf("MIT", "Apache-2.0", "BSD-2-Clause", "BSD-3-Clause"))
    }

    override fun calculate(bars: List<AmarMarketBar>, request: AmarIndicatorRequest): AmarIndicatorResult =
        calculator(bars, request).also { require(it.source == provenance) }
}

/** Runs independent analyses concurrently and returns results in deterministic request order. */
class AmarParallelMarketAnalysisEngine(
    private val indicatorAdapter: AmarIndicatorAdapter = AmarBuiltInIndicatorAdapter(),
    private val statisticsEngine: AmarStatisticalEngine = AmarStatisticalEngine(),
    private val structureEngine: AmarMarketStructureEngine = AmarMarketStructureEngine()
) {
    fun analyze(
        bars: List<AmarMarketBar>,
        indicatorRequests: List<AmarIndicatorRequest>,
        structureLookback: Int = 20,
        maxWorkers: Int = minOf(4, Runtime.getRuntime().availableProcessors().coerceAtLeast(1))
    ): AmarMarketIntelligenceReport {
        require(bars.isNotEmpty())
        require(bars.zipWithNext().all { it.first.timestampEpochMs < it.second.timestampEpochMs })
        require(maxWorkers in 1..4)
        val pool = Executors.newFixedThreadPool(maxWorkers)
        return try {
            val indicatorFutures = indicatorRequests.distinct().map { request ->
                pool.submit(Callable { indicatorAdapter.calculate(bars, request) })
            }
            val statisticsFuture = pool.submit(Callable { statisticsEngine.summarize(bars) })
            val structureFuture = pool.submit(Callable {
                if (bars.size > structureLookback) structureEngine.inspect(bars, structureLookback)
                else AmarMarketStructureSnapshot(AmarStructureSignal.RANGE, bars.minOf { it.low }, bars.maxOf { it.high }, structureLookback)
            })
            val indicators = indicatorFutures.map { it.get() }
            val statistics = statisticsFuture.get()
            val structure = structureFuture.get()
            val score = deterministicScore(indicators, statistics, structure)
            val confidence = (0.25 + 0.15 * indicators.count { it.points.isNotEmpty() } +
                0.30 * (1.0 - kotlin.math.min(1.0, kotlin.math.abs(statistics.zScore) / 4.0)) + 0.30).coerceIn(0.0, 1.0)
            AmarMarketIntelligenceReport(
                bars.size,
                indicators,
                statistics,
                structure,
                score,
                confidence,
                listOf("parallel-independent-analysis", "deterministic-result-order", "fail-closed-source-boundary")
            )
        } finally {
            pool.shutdownNow()
        }
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
        if (sma != null && ema != null) score += when {
            ema > sma -> 0.20
            ema < sma -> -0.20
            else -> 0.0
        }
        if (rsi != null) score += when {
            rsi >= 55.0 -> 0.20
            rsi <= 45.0 -> -0.20
            else -> 0.0
        }
        score += when (structure.signal) {
            AmarStructureSignal.BREAKOUT_UP, AmarStructureSignal.HIGHER_HIGH, AmarStructureSignal.HIGHER_LOW -> 0.35
            AmarStructureSignal.BREAKOUT_DOWN, AmarStructureSignal.LOWER_HIGH, AmarStructureSignal.LOWER_LOW -> -0.35
            AmarStructureSignal.RANGE -> 0.0
        }
        return (score + (stats.meanReturn * 100.0).coerceIn(-0.25, 0.25)).coerceIn(-1.0, 1.0)
    }
}
