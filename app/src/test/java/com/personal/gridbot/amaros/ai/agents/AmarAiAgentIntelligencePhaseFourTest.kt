package com.personal.gridbot.amaros.ai.agents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiAgentIntelligencePhaseFourTest {
    @Test
    fun planner_createsSpecialistTasks() {
        val tasks = AmarAgentPlanner().plan("تحليل XAUUSD")
        assertEquals(5, tasks.size)
        assertTrue(tasks.any { it.allowedRoles.contains(AmarAgentRole.QUANT_ANALYST) })
    }

    @Test
    fun consensus_measuresAgreementAndDisagreement() {
        val findings = listOf(
            AmarAgentFinding(AmarAgentRole.RESEARCHER, "up", listOf("e1"), 0.8, 1.0),
            AmarAgentFinding(AmarAgentRole.QUANT_ANALYST, "up", listOf("e2"), 0.6, 0.8),
            AmarAgentFinding(AmarAgentRole.RISK_ANALYST, "down", listOf("e3"), -0.4, 0.5)
        )
        val result = AmarAgentConsensusEngine().evaluate(findings)
        assertTrue(result.directionScore > 0.0)
        assertEquals(2, result.supportingAgents)
        assertEquals(1, result.opposingAgents)
        assertTrue(result.disagreement > 0.0)
    }

    @Test
    fun synthesizer_returnsDecisionWithoutClaimingProfitProbability() {
        val consensus = AmarConsensus(0.6, 0.8, 0.2, 4, 1)
        val result = AmarAgentSynthesizer().synthesize(consensus, emptyList())
        assertEquals("شراء", result.label)
        assertEquals(0.6, result.score, 0.0001)
        assertEquals(0.64, result.confidence, 0.0001)
    }
}
