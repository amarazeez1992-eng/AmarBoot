package com.personal.gridbot.amaros.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarIntelligenceQualityBenchmarkTest {
    @Test
    fun benchmark_isDeterministicAndPassesQualityGate() {
        val benchmark = AmarIntelligenceQualityBenchmark()
        val first = benchmark.run()
        val second = benchmark.run()

        assertTrue(first.passed)
        assertEquals(first, second)
        assertTrue(first.accuracy >= 0.90)
        assertTrue(first.calibrationError <= 0.10)
        assertTrue(first.failedCaseIds.isEmpty())
    }

    @Test
    fun benchmark_containsMeaningfulCoverage() {
        val report = AmarIntelligenceQualityBenchmark().run()
        assertEquals(8, report.totalCases)
        assertEquals(report.totalCases, report.passedCases)
    }
}
