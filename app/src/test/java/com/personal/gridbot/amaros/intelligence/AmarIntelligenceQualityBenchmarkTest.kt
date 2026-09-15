package com.personal.gridbot.amaros.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarIntelligenceQualityBenchmarkTest {
    @Test
    fun benchmark_isDeterministicAndPassesQualityGate() {
        val report = AmarIntelligenceQualityBenchmark().run()

        assertTrue(report.passed)
        assertTrue(report.accuracy >= 0.90)
        assertEquals(0.0, report.calibrationDrift, 0.0)
        assertTrue(report.elapsedNanos >= 0L)
        assertTrue(report.failedCaseIds.isEmpty())
    }

    @Test
    fun benchmark_containsMeaningfulCoverage() {
        val report = AmarIntelligenceQualityBenchmark().run()
        assertEquals(8, report.totalCases)
        assertEquals(report.totalCases, report.passedCases)
    }
}
