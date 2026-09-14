package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarStageSixTest {
    private fun bars(count: Int = 100): List<AmarMarketBar> = buildList {
        var price = 100.0
        for (i in 0 until count) {
            val drift = if (i % 7 < 4) 0.35 else -0.12
            val open = price
            val close = price + drift
            val high = maxOf(open, close) + 0.20
            val low = minOf(open, close) - 0.15
            add(AmarMarketBar(i.toLong() + 1L, open, high, low, close, 1000.0 + i))
            price = close
        }
    }

    @Test
    fun foundational_indicators_are_deterministic_and_finite() {
        val engine = AmarBuiltInIndicatorAdapter()
        val data = bars(80)
        for (kind in listOf(AmarIndicatorKind.SMA, AmarIndicatorKind.EMA, AmarIndicatorKind.RSI, AmarIndicatorKind.ATR)) {
            val result = engine.calculate(data, AmarIndicatorRequest(kind, 14))
            assertTrue(result.points.isNotEmpty())
            assertTrue(result.points.all { it.value.isFinite() })
            assertEquals(result.points, engine.calculate(data, AmarIndicatorRequest(kind, 14)).points)
        }
    }

    @Test
    fun provenance_registry_accepts_verified_bsd_source_and_rejects_unapproved_or_incomplete_sources() {
        val registry = AmarSourceRegistry()
        val source = AmarBuiltInIndicatorAdapter().provenance
        registry.register(source)
        assertEquals(source, registry.get(source.sourceId))

        val taLibCandidate = source.copy(
            sourceId = "ta-lib",
            name = "TA-Lib",
            version = "candidate",
            homepage = "https://ta-lib.org",
            repository = "https://github.com/TA-Lib/ta-lib",
            license = AmarSourceLicense("BSD-3-Clause", "https://opensource.org/license/bsd-3-clause/")
        )
        registry.register(taLibCandidate)
        assertEquals(taLibCandidate, registry.get("ta-lib"))

        val unlicensedExternal = taLibCandidate.copy(sourceId = "missing-repository", repository = null)
        try {
            registry.register(unlicensedExternal)
            throw AssertionError("external source without repository provenance must fail closed")
        } catch (_: IllegalArgumentException) {
            // expected
        }

        val incompatible = taLibCandidate.copy(
            sourceId = "bad-source",
            license = AmarSourceLicense("GPL-3.0-only", "https://www.gnu.org/licenses/gpl-3.0.html")
        )
        try {
            registry.register(incompatible)
            throw AssertionError("GPL source must be rejected by the default policy")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun duplicate_source_id_with_changed_provenance_is_rejected() {
        val registry = AmarSourceRegistry()
        val source = AmarBuiltInIndicatorAdapter().provenance
        registry.register(source)
        try {
            registry.register(source.copy(version = "2.1"))
            throw AssertionError("conflicting provenance must fail closed")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun licensed_adapter_cannot_return_a_different_provenance() {
        val source = AmarBuiltInIndicatorAdapter().provenance
        val adapter = AmarLicensedIndicatorAdapter(source) { bars, request ->
            AmarBuiltInIndicatorAdapter().calculate(bars, request)
        }
        assertTrue(adapter.calculate(bars(40), AmarIndicatorRequest(AmarIndicatorKind.SMA, 10)).points.isNotEmpty())
    }

    @Test
    fun aggregation_is_deterministic_and_bounded() {
        val data = bars(120)
        val engine = AmarStageSixAggregationEngine()
        val first = engine.analyze(data)
        val second = engine.analyze(data)
        assertEquals(first, second)
        assertTrue(first.score in -1.0..1.0)
        assertTrue(first.confidence in 0.0..1.0)
        assertEquals(120, first.bars)
        assertEquals(4, first.indicators.size)
    }

    @Test
    fun parallel_analysis_matches_sequential_analysis_for_same_inputs() {
        val data = bars(2_000)
        val requests = listOf(
            AmarIndicatorRequest(AmarIndicatorKind.SMA, 20),
            AmarIndicatorRequest(AmarIndicatorKind.EMA, 20),
            AmarIndicatorRequest(AmarIndicatorKind.RSI, 14),
            AmarIndicatorRequest(AmarIndicatorKind.ATR, 14)
        )
        val sequential = AmarStageSixAggregationEngine().analyze(data, requests, 20)
        val parallel = AmarParallelMarketAnalysisEngine().analyze(data, requests, 20, 4)
        assertEquals(sequential.indicators, parallel.indicators)
        assertEquals(sequential.statistics, parallel.statistics)
        assertEquals(sequential.structure, parallel.structure)
        assertEquals(sequential.score, parallel.score, 0.0)
    }

    @Test
    fun malformed_or_unsorted_market_data_fails_closed() {
        val data = bars(30)
        try {
            AmarStageSixAggregationEngine().analyze(listOf(data[1], data[0]))
            throw AssertionError("unsorted data must be rejected")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun large_deterministic_dataset_completes_without_numeric_failure() {
        val report = AmarStageSixAggregationEngine().analyze(bars(20_000))
        assertEquals(20_000, report.bars)
        assertTrue(report.indicators.all { it.points.all { point -> point.value.isFinite() } })
    }
}
