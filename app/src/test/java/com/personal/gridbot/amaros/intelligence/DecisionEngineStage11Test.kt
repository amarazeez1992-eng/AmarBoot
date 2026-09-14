package com.personal.gridbot.amaros.intelligence

import com.personal.gridbot.amaros.data.AmarDataSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionEngineStage11Test {
    private val data = AmarDataSnapshot()
    private val strongContext = MarketContext(
        directionalScore = 0.90,
        confidence = 0.90,
        regime = "TREND",
        evidence = listOf(
            Evidence(EvidenceType.TREND, 0.90, 0.90, "trend"),
            Evidence(EvidenceType.MOMENTUM, 0.70, 0.80, "momentum")
        ),
        explanation = "strong"
    )

    @Test
    fun legacyEvaluateRemainsProposalOnly() {
        val result = DecisionEngine().evaluate(data, strongContext)
        assertFalse(result.executable)
        assertEquals(DecisionEngine.Direction.LONG_BIAS, result.direction)
        assertEquals(DecisionEngine.Status.PROPOSED, result.status)
        assertTrue(result.decisionId.isNotBlank())
    }

    @Test
    fun missingVerificationBlocksByPolicy() {
        val result = DecisionEngine().evaluate(
            DecisionEngine.DecisionInput(
                data = data,
                context = strongContext,
                verificationScore = 0.0,
                evidenceVerified = false
            )
        )
        assertEquals(DecisionEngine.Status.BLOCKED, result.status)
        assertEquals(DecisionEngine.Direction.NEUTRAL, result.direction)
        assertFalse(result.executable)
    }

    @Test
    fun emergencyLockAlwaysBlocks() {
        val result = DecisionEngine().evaluate(
            DecisionEngine.DecisionInput(data, strongContext, emergencyLock = true)
        )
        assertEquals(DecisionEngine.Status.BLOCKED, result.status)
        assertEquals(DecisionEngine.Direction.NEUTRAL, result.direction)
    }

    @Test
    fun quantitativeRiskCanOnlyIncreaseRisk() {
        val baseline = DecisionEngine().evaluate(
            DecisionEngine.DecisionInput(data, strongContext)
        )
        val quantitative = DecisionEngine.QuantitativeAssessment(
            realizedVolatility = 0.80,
            maxDrawdown = 0.70,
            historicalVar = 0.60,
            riskOfRuin = 0.50
        )
        val stressed = DecisionEngine().evaluate(
            DecisionEngine.DecisionInput(data, strongContext, quantitative = quantitative)
        )
        assertTrue(stressed.riskScore >= baseline.riskScore)
        assertFalse(stressed.executable)
    }

    @Test
    fun conflictingEvidenceReducesConfidence() {
        val conflicting = strongContext.copy(
            evidence = listOf(
                Evidence(EvidenceType.TREND, 0.90, 0.90, "up"),
                Evidence(EvidenceType.STRUCTURE, -0.90, 0.90, "down")
            )
        )
        val clean = DecisionEngine().evaluate(data, strongContext)
        val result = DecisionEngine().evaluate(data, conflicting)
        assertTrue(result.confidence < clean.confidence)
    }

    @Test
    fun policyRejectsNonFiniteWeights() {
        assertIllegalArgument {
            DecisionEngine.DecisionPolicy(contextWeight = Double.POSITIVE_INFINITY)
        }
        assertIllegalArgument {
            DecisionEngine.DecisionPolicy(evidenceWeight = Double.NaN)
        }
    }

    @Test
    fun outputsRemainBounded() {
        val result = DecisionEngine().evaluate(
            DecisionEngine.DecisionInput(
                data = data,
                context = strongContext,
                verificationScore = 1.0,
                quantitative = DecisionEngine.QuantitativeAssessment(9.0, 9.0, 9.0, 9.0)
            )
        )
        assertTrue(result.score in -1.0..1.0)
        assertTrue(result.confidence in 0.0..1.0)
        assertTrue(result.riskScore in 0.0..1.0)
    }

    @Test
    fun sameInputProducesSameDecisionId() {
        val input = DecisionEngine.DecisionInput(data, strongContext)
        val first = DecisionEngine().evaluate(input)
        val second = DecisionEngine().evaluate(input)
        assertEquals(first.decisionId, second.decisionId)
    }

    private fun assertIllegalArgument(block: () -> Unit) {
        try {
            block()
            throw AssertionError("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected regression guard.
        }
    }
}
