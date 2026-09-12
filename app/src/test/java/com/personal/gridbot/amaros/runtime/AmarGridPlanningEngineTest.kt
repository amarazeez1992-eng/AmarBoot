package com.personal.gridbot.amaros.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarGridPlanningEngineTest {
    @Test
    fun buildsBothSidesWithDeterministicLotProgression() {
        val levels = AmarGridPlanningEngine.build(100.0, 2.0, 3, 0.01, 2.0, true, true)
        assertEquals(6, levels.size)
        assertEquals(98.0, levels.first { it.side == AmarGridPlanningEngine.Side.BUY && it.index == 1 }.price, 0.000001)
        assertEquals(106.0, levels.first { it.side == AmarGridPlanningEngine.Side.SELL && it.index == 3 }.price, 0.000001)
        assertEquals(0.04, levels.first { it.side == AmarGridPlanningEngine.Side.BUY && it.index == 3 }.volume, 0.000001)
    }

    @Test
    fun disabledSideIsNotPlanned() {
        val levels = AmarGridPlanningEngine.build(100.0, 1.0, 2, 0.01, 1.5, true, false)
        assertTrue(levels.all { it.side == AmarGridPlanningEngine.Side.BUY })
        assertEquals(2, levels.size)
    }
}
