package com.personal.gridbot.amaros.ai

import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiEngineBindingTest {
    @Test fun marketUsesRealMarketStateEngine() {
        assertTrue(AmarAiEngineBinding.market().startsWith("ENGINE_MARKET|"))
    }

    @Test fun gridUsesDeterministicGridPlanner() {
        val result = AmarAiEngineBinding.grid(100.0, 2.0, 3, 0.01, 2.0, true, true)
        assertTrue(result.startsWith("ENGINE_GRID|levels=6|"))
        assertTrue(result.contains("BUY"))
        assertTrue(result.contains("SELL"))
    }

    @Test fun validationAndRiskUseRealEngines() {
        val validation = AmarAiEngineBinding.validate(listOf(1.0, -1.0, 2.0, -0.5))
        assertTrue(validation.startsWith("ENGINE_VALIDATION|"))
        val risk = AmarAiEngineBinding.riskGate()
        assertTrue(risk.startsWith("ENGINE_RISK|"))
    }
}
