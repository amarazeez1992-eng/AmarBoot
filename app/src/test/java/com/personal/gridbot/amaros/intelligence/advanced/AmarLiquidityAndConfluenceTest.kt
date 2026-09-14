package com.personal.gridbot.amaros.intelligence.advanced

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarLiquidityAndConfluenceTest {
    @Test fun swingHighSweepProducesBearishReversal() {
        val level = AmarLiquidityLevel(100.0, AmarLiquidityLevel.Kind.SWING_HIGH)
        val candle = AmarOhlc(1L, 99.0, 101.0, 98.5, 99.5)
        val sweeps = AmarLiquidityEngine().detectSweeps(listOf(candle), listOf(level))
        assertEquals(1, sweeps.size)
        assertEquals(AmarLiquiditySweep.Direction.BEARISH_REVERSAL, sweeps.single().direction)
    }

    @Test fun repeatedBarsBelowLevelDoNotDuplicateSameSweep() {
        val level = AmarLiquidityLevel(100.0, AmarLiquidityLevel.Kind.SWING_HIGH)
        val candles = listOf(
            AmarOhlc(1L, 99.0, 101.0, 98.5, 99.5),
            AmarOhlc(2L, 99.5, 100.5, 98.0, 99.0),
            AmarOhlc(3L, 99.0, 100.4, 98.2, 99.2)
        )
        assertEquals(1, AmarLiquidityEngine().detectSweeps(candles, listOf(level)).size)
    }

    @Test fun recrossArmsLevelForANewSweep() {
        val level = AmarLiquidityLevel(100.0, AmarLiquidityLevel.Kind.SWING_HIGH)
        val candles = listOf(
            AmarOhlc(1L, 99.0, 101.0, 98.5, 99.5),
            AmarOhlc(2L, 100.0, 101.0, 99.5, 100.5),
            AmarOhlc(3L, 100.0, 101.2, 99.0, 99.4)
        )
        assertEquals(2, AmarLiquidityEngine().detectSweeps(candles, listOf(level)).size)
    }

    @Test fun swingLowSweepProducesBullishReversal() {
        val level = AmarLiquidityLevel(100.0, AmarLiquidityLevel.Kind.SWING_LOW)
        val candle = AmarOhlc(1L, 101.0, 101.5, 99.0, 100.5)
        val sweeps = AmarLiquidityEngine().detectSweeps(listOf(candle), listOf(level))
        assertEquals(1, sweeps.size)
        assertEquals(AmarLiquiditySweep.Direction.BULLISH_REVERSAL, sweeps.single().direction)
    }

    @Test fun confluenceCountsIndependentSourceGroupsAndUsesStrongestPerGroup() {
        val signals = listOf(
            AmarConfluenceSignal("trend-1", AmarConfluenceSignal.Direction.BULLISH, 0.9, "trend", "H1"),
            AmarConfluenceSignal("trend-2", AmarConfluenceSignal.Direction.BULLISH, 0.8, "trend", "M15"),
            AmarConfluenceSignal("structure-1", AmarConfluenceSignal.Direction.BULLISH, 0.7, "structure", "H1"),
            AmarConfluenceSignal("momentum-1", AmarConfluenceSignal.Direction.BEARISH, 0.4, "momentum", "M15")
        )
        val result = AmarConfluenceEngine().combine(signals)
        assertEquals(AmarConfluenceSignal.Direction.BULLISH, result.direction)
        assertEquals(4, result.supportingSignals)
        assertEquals(3, result.independentSourceGroups)
        assertTrue(result.confidence in 0.0..1.0)
    }

    @Test fun qualityGateRejectsMalformedAndNonMonotonicBars() {
        val errors = AmarMarketDataQualityGate.validate(
            listOf(
                AmarOhlc(2L, 10.0, 9.0, 8.0, 8.5),
                AmarOhlc(1L, 10.0, 11.0, 9.0, 10.0)
            )
        )
        assertTrue(errors.any { it.contains("invalid OHLC bounds") })
        assertTrue(errors.any { it.contains("non-monotonic timestamp") })
    }

    @Test fun qualityGateAcceptsValidMonotonicBars() {
        val errors = AmarMarketDataQualityGate.validate(
            listOf(
                AmarOhlc(1L, 10.0, 11.0, 9.0, 10.5, 0.1),
                AmarOhlc(2L, 10.5, 12.0, 10.0, 11.5, 0.1)
            )
        )
        assertTrue(errors.isEmpty())
    }
}
