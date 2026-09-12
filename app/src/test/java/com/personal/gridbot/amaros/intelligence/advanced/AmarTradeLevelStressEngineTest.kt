package com.personal.gridbot.amaros.intelligence.advanced

import org.junit.Assert.assertTrue
import org.junit.Test

class AmarTradeLevelStressEngineTest {
    @Test fun spreadStressDegradesMeasuredResults() {
        val result = AmarTradeLevelStressEngine.evaluate(listOf(1.0, -0.5, 0.8, -0.2), AmarTradeLevelStressEngine.Scenario.SPREAD, 1.0)
        assertTrue(result.degradationR > 0.0)
        assertTrue(result.stressedNetR < result.baseNetR)
    }
}
