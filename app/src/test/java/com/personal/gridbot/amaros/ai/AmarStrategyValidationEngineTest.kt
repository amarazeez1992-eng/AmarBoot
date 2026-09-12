package com.personal.gridbot.amaros.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarStrategyValidationEngineTest {
    @Test
    fun validationReportsMeasuredStatistics() {
        val report = AmarStrategyValidationEngine.analyze(listOf(1.0, -0.5, 1.5, -0.25, 0.75), simulations = 100)
        assertEquals(5, report.sampleSize)
        assertTrue(report.winRatePct > 0.0)
        assertTrue(report.profitFactor > 1.0)
        assertTrue(report.verified)
    }
}
