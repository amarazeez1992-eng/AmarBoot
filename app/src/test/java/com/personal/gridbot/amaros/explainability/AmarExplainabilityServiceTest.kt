package com.personal.gridbot.amaros.explainability

import com.personal.gridbot.amaros.core.AmarExecutionBoundary
import com.personal.gridbot.amaros.core.AmarRiskGate
import com.personal.gridbot.amaros.core.AmarRuntimeController
import com.personal.gridbot.amaros.core.AmarUiContract
import com.personal.gridbot.amaros.intelligence.DecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarExplainabilityServiceTest {
    @Test
    fun runtime_state_maps_to_explanation_correctly() {
        val state = AmarRuntimeController.RuntimeState(
            cycleNumber = 7L,
            telemetry = AmarUiContract.Telemetry(symbol = "XAUUSD", timeframe = "M5"),
            decision = DecisionEngine.DecisionProposal(
                direction = DecisionEngine.Direction.LONG_BIAS,
                score = 0.75,
                confidence = 0.82,
                rationale = "test rationale"
            ),
            risk = AmarRiskGate.Result(true, "risk approved"),
            execution = AmarExecutionBoundary.ExecutionResult(true, false, "req-7", "demo guarded"),
            lastUpdateEpochMs = 7000L
        )
        val explanation = AmarExplainabilityService().explain(state, "trace-7")
        assertEquals("trace-7", explanation.traceId)
        assertEquals(7L, explanation.cycleNumber)
        assertEquals("XAUUSD", explanation.symbol)
        assertEquals("M5", explanation.timeframe)
        assertEquals("LONG_BIAS", explanation.decision)
        assertEquals(0.75, explanation.score, 0.0)
        assertEquals(0.82, explanation.confidence, 0.0)
        assertEquals("test rationale", explanation.rationale)
        assertTrue(explanation.riskAllowed)
        assertEquals("risk approved", explanation.riskReason)
        assertFalse(explanation.executionAttempted)
        assertEquals("DEMO_GUARDED", explanation.executionMode)
        assertTrue(explanation.demoOnly)
        assertEquals(7000L, explanation.generatedAtEpochMs)
    }
}
