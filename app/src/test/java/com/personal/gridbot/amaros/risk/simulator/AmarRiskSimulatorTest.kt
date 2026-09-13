package com.personal.gridbot.amaros.risk.simulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarRiskSimulatorTest {
    @Test
    fun scenarioCalculatesEquityMarginAndExposureImpact() {
        val result = AmarRiskSimulator.simulate(
            AmarRiskSimulator.AccountState(1000.0, 200.0, 500.0, 5.0),
            AmarRiskSimulator.Scenario(100.0, 50.0, 200.0)
        )
        assertEquals(900.0, result.equityAfterLoss, 0.0001)
        assertEquals(250.0, result.marginAfter, 0.0001)
        assertEquals(650.0, result.freeMarginAfter, 0.0001)
        assertEquals(700.0, result.exposureAfter, 0.0001)
        assertEquals(10.0, result.estimatedEquityImpactPct, 0.0001)
        assertTrue(result.isScenarioOnly)
    }

    @Test
    fun invalidScenarioIsRejected() {
        val errors = AmarRiskSimulator.validate(
            AmarRiskSimulator.AccountState(1000.0, 200.0, 500.0, 5.0),
            AmarRiskSimulator.Scenario(estimatedLoss = -1.0)
        )
        assertTrue("LOSS_INVALID" in errors)
    }
}
