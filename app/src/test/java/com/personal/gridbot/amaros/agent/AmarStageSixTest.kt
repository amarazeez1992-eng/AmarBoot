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
    fun sma_ema_rsi_atr_are_deterministic_and_finite() {
        val engine = AmarBuiltInIndicatorAdapter()
        val data = bars(80)
        for (kind in AmarIndicatorKind.entries) {
            val result = engine.calculate(data, AmarIndicatorRequest(kind, 14))
            assertTrue(result.points.isNotEmpty())
            assertTrue(result.points.all { it.value.isFinite() })
            assertEquals(result.points, engine.calculate(data, AmarIndicatorRequest(kind, 14)).points)
        }
    }

    @Test
    fun provenance_registry_accepts_approved_license_and_rejects_unapproved() {
        val registry = AmarSourceRegistry()
        val source = AmarBuiltInIndicatorAdapter().provenance
        registry.register(source)
        assertEquals(source, registry.get(source.sourceId))
        val external = source.copy(
            sourceId = "bad-source",
            name = "Bad Source",
            sourceType = AmarSourceType.INDICATOR_ENGINE,
            repository = "https://example.com/repo",
            license = AmarSourceLicense("GPL-3.0-only", "https://www.gnu.org/licenses/gpl-3.0.html")
        )
        try {
            registry.register(external)
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
            registry.register(source.copy(version = "2.0"))
            throw AssertionError("conflicting provenance must fail closed")
        } catch (_: IllegalArgumentException) {
            // expected
        }
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
        val data = bars(20_000)
        val started = System.nanoTime()
        val report = AmarStageSixAggregationEngine().analyze(data)
        val elapsedMs = (System.nanoTime() - started) / 1_000_000L
        assertEquals(20_000, report.bars)
        assertTrue(report.indicators.all { it.points.all { point -> point.value.isFinite() } })
        assertTrue(elapsedMs < 5_000L)
    }
}
