package com.personal.gridbot.amaros.trading.risk

import kotlin.test.Test
import kotlin.test.assertEquals

class AmarRiskPolicyEvaluatorTest {
    @Test
    fun unavailableEvidenceFailsClosed() {
        val result = AmarRiskPolicyEvaluator.evaluate(
            AmarRiskPolicy(dailyLossLimit = 100.0),
            AmarRiskSnapshot(dataTrusted = false)
        )
        assertEquals(listOf("RISK_DATA_UNAVAILABLE"), result)
    }

    @Test
    fun dailyLossLimitIsDetected() {
        val result = AmarRiskPolicyEvaluator.evaluate(
            AmarRiskPolicy(dailyLossLimit = 100.0),
            AmarRiskSnapshot(dailyPnl = -100.0, dataTrusted = true)
        )
        assertEquals(listOf("DAILY_LOSS_LIMIT"), result)
    }
}
