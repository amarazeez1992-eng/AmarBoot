package com.personal.gridbot.amaros.trading.quant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarQuantTradingMathTest {
    @Test fun trueRangeUsesGapAwareDefinition() {
        assertEquals(3.0, AmarQuantTradingMath.trueRange(105.0, 102.0, 103.0)!!, 0.0)
        assertEquals(5.0, AmarQuantTradingMath.trueRange(105.0, 102.0, 100.0)!!, 0.0)
    }

    @Test fun atrUsesWilderSmoothingInsteadOfSimpleRollingAverage() {
        assertEquals(2.25, AmarQuantTradingMath.atr(listOf(1.0, 2.0, 3.0), 2)!!, 0.0)
        assertEquals(2.125, AmarQuantTradingMath.atr(listOf(1.0, 2.0, 3.0, 2.0), 2)!!, 0.0)
        assertNull(AmarQuantTradingMath.atr(listOf(1.0, Double.NaN), 2))
    }

    @Test fun volatilityDrawdownAndVarUseExplicitStatisticalDefinitions() {
        assertTrue((AmarQuantTradingMath.realizedVolatility(listOf(100.0, 101.0, 100.0)) ?: -1.0) >= 0.0)
        assertEquals(0.25, AmarQuantTradingMath.maxDrawdown(listOf(100.0, 80.0, 75.0))!!, 0.0)
        assertEquals(3.85, AmarQuantTradingMath.historicalVar(listOf(1.0, 2.0, 3.0, 4.0), 0.95)!!, 1e-12)
        assertNull(AmarQuantTradingMath.historicalVar(listOf(-1.0, 2.0), 0.95))
        assertNull(AmarQuantTradingMath.historicalVar(listOf(1.0, 2.0), 1.1))
    }

    @Test fun sizingAndKellyRejectUnsafeInputs() {
        assertEquals(100.0, AmarQuantTradingMath.riskPositionSize(10000.0, 0.01, 1.0, 1.0)!!, 0.0)
        assertEquals(0.4, AmarQuantTradingMath.kellyFraction(0.6, 2.0)!!, 0.0)
        assertNull(AmarQuantTradingMath.kellyFraction(0.5, 0.0))
        assertNull(AmarQuantTradingMath.riskPositionSize(10000.0, 0.0, 1.0, 1.0))
    }

    @Test fun riskOfRuinUsesExplicitDrawdownBarrierAndModelAssumptions() {
        val favorable = AmarQuantTradingMath.riskOfRuin(0.6, 2.0, 0.01, 0.5)
        assertTrue(favorable != null && favorable in 0.0..1.0)

        val nonPositiveLogGrowth = AmarQuantTradingMath.riskOfRuin(0.4, 1.0, 0.01, 0.2)
        assertEquals(1.0, nonPositiveLogGrowth!!, 0.0)

        assertNull(AmarQuantTradingMath.riskOfRuin(0.6, 2.0, 0.01, 0.0))
        assertNull(AmarQuantTradingMath.riskOfRuin(0.6, 2.0, 0.01, 1.0))
    }
}
