package com.personal.gridbot.amaros.trading.quant

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AmarQuantTradingMathTest {
    @Test fun trueRangeUsesGapAwareDefinition() {
        assertEquals(3.0, AmarQuantTradingMath.trueRange(105.0, 102.0, 103.0))
        assertEquals(5.0, AmarQuantTradingMath.trueRange(105.0, 102.0, 100.0))
    }

    @Test fun atrIsDeterministicAndRejectsInvalidInput() {
        assertEquals(2.0, AmarQuantTradingMath.atr(listOf(1.0, 2.0, 3.0), 2))
        assertNull(AmarQuantTradingMath.atr(listOf(1.0, Double.NaN), 2))
    }

    @Test fun volatilityDrawdownAndVarAreBounded() {
        assertTrue((AmarQuantTradingMath.realizedVolatility(listOf(100.0, 101.0, 100.0)) ?: -1.0) >= 0.0)
        assertEquals(0.25, AmarQuantTradingMath.maxDrawdown(listOf(100.0, 80.0, 75.0)))
        assertEquals(3.0, AmarQuantTradingMath.historicalVar(listOf(1.0, 2.0, 3.0, 4.0), 0.95))
    }

    @Test fun sizingAndKellyRejectUnsafeInputs() {
        assertEquals(100.0, AmarQuantTradingMath.riskPositionSize(10000.0, 0.01, 1.0, 1.0))
        assertEquals(0.2, AmarQuantTradingMath.kellyFraction(0.6, 2.0))
        assertNull(AmarQuantTradingMath.kellyFraction(0.5, 0.0))
        assertNull(AmarQuantTradingMath.riskPositionSize(10000.0, 0.0, 1.0, 1.0))
    }

    @Test fun riskOfRuinNeverLeavesProbabilityRange() {
        val result = AmarQuantTradingMath.riskOfRuin(0.6, 2.0, 0.01)
        assertTrue(result != null && result in 0.0..1.0)
    }
}
