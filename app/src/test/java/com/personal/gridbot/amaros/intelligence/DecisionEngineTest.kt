package com.personal.gridbot.amaros.intelligence

import com.personal.gridbot.amaros.data.AmarDataSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionEngineTest {
    private val engine = DecisionEngine()
    private val data = AmarDataSnapshot(generatedAtEpochMs = 1_000L)
    private val context = MarketAnalyzer().analyze(data)

    @Test
    fun quantitativeRisk_isNullWithoutEvidence() {
        assertNull(engine.assessQuantitativeRisk(DecisionEngine.QuantitativeInput()))
    }

    @Test
    fun quantitativeRisk_usesOnlyAvailableValidMetrics() {
        val risk = engine.assessQuantitativeRisk(
            DecisionEngine.QuantitativeInput(
                closes = listOf(100.0, 101.0, 99.0, 100.5),
                equityCurve = listOf(1000.0, 1100.0, 900.0, 950.0),
                losses = listOf(10.0, 20.0, 30.0, 40.0),
                varConfidence = 0.75,
                varScale = 100.0
            )
        )

        assertNotNull(risk)
        assertTrue(risk!!.realizedVolatility!! >= 0.0)
        assertEquals(200.0 / 1100.0, risk.maxDrawdown!!, 1e-9)
        assertEquals(32.5 / 100.0, risk.normalizedHistoricalVar!!, 1e-9)
        assertNull(risk.riskOfRuin)
        assertTrue(risk.score in 0.0..1.0)
    }

    @Test
    fun higherQuantitativeRisk_reducesScoreAndConfidence_butNeverEnablesExecution() {
        val lowRisk = DecisionEngine.QuantitativeInput(
            closes = listOf(100.0, 100.1, 100.0, 100.1),
            equityCurve = listOf(1000.0, 1005.0, 1003.0, 1007.0),
            losses = listOf(1.0, 1.0, 2.0, 1.0),
            varConfidence = 0.95,
            varScale = 100.0
        )
        val highRisk = DecisionEngine.QuantitativeInput(
            closes = listOf(100.0, 120.0, 80.0, 130.0),
            equityCurve = listOf(1000.0, 1200.0, 400.0, 450.0),
            losses = listOf(50.0, 70.0, 90.0, 100.0),
            varConfidence = 0.95,
            varScale = 100.0
        )

        val low = engine.evaluate(data, context, lowRisk)
        val high = engine.evaluate(data, context, highRisk)

        assertTrue(high.quantitativeRisk!!.score > low.quantitativeRisk!!.score)
        assertTrue(kotlin.math.abs(high.score) <= kotlin.math.abs(low.score) + 1e-12)
        assertTrue(high.confidence <= low.confidence + 1e-12)
        assertFalse(low.executable)
        assertFalse(high.executable)
    }

    @Test
    fun invalidQuantitativeInputs_doNotBecomeSyntheticRisk() {
        val risk = engine.assessQuantitativeRisk(
            DecisionEngine.QuantitativeInput(
                closes = listOf(100.0, Double.NaN, 101.0),
                equityCurve = listOf(1000.0, Double.POSITIVE_INFINITY),
                losses = listOf(10.0, -1.0),
                varScale = 100.0,
                winProbability = 2.0,
                payoffRatio = 1.0,
                riskFraction = 0.01,
                ruinFraction = 0.5
            )
        )

        assertNull(risk)
    }
}
