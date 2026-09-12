package com.personal.gridbot.amaros.trading

import com.personal.gridbot.amaros.intelligence.trading.AmarStrategyGovernanceEngine
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingPrecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class AmarTradingIntelligenceTest {
    @org.junit.Test
    fun precisionGateBlocksIncompleteEvidence() {
        val report = AmarTradingPrecisionEngine.defaultResearchGate()
        assertTrue(report.uncertaintyPct > 0.0)
        assertEquals(AmarTradingPrecisionEngine.Gate.BLOCKED, report.gate)
    }

    @org.junit.Test
    fun challengerCannotBypassLeakageGate() {
        val champion = AmarStrategyGovernanceEngine.Candidate("A", 1, 0.10, 2.0, 1.4, 200)
        val challenger = AmarStrategyGovernanceEngine.Candidate("B", 2, 0.30, 1.5, 1.6, 200, leakageFree = false)
        val result = AmarStrategyGovernanceEngine.compare(champion, challenger)
        assertEquals(AmarStrategyGovernanceEngine.Gate.REJECT, result.gate)
    }

    @org.junit.Test
    fun cleanChallengerStillRequiresHumanApproval() {
        val champion = AmarStrategyGovernanceEngine.Candidate("A", 1, 0.10, 2.0, 1.4, 200)
        val challenger = AmarStrategyGovernanceEngine.Candidate("B", 2, 0.30, 1.5, 1.6, 200)
        val result = AmarStrategyGovernanceEngine.compare(champion, challenger)
        assertEquals(AmarStrategyGovernanceEngine.Gate.APPROVAL_READY, result.gate)
        assertTrue(result.reasons.any { it.contains("Human approval") })
    }
}
