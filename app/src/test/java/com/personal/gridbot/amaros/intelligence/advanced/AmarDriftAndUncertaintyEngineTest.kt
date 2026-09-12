package com.personal.gridbot.amaros.intelligence.advanced

import org.junit.Assert.assertTrue
import org.junit.Test

class AmarDriftAndUncertaintyEngineTest {
    @Test fun severeDistributionShiftIsDetected() {
        val report = AmarDriftAndUncertaintyEngine.drift(
            listOf(1.0, 1.1, 0.9, 1.0, 1.05, 0.95),
            listOf(4.0, 4.2, 3.8, 4.1, 4.3, 3.9)
        )
        assertTrue(report.severe)
    }

    @Test fun smallSamplesIncreaseUncertainty() {
        val report = AmarDriftAndUncertaintyEngine.uncertainty(12, 1.0, 4, 1, 0.6)
        assertTrue(report.uncertaintyPct > 50.0)
    }
}
