package com.personal.gridbot.amaros.intelligence.advanced

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarStrategyEvolutionEngineTest {
    @Test fun leakageAlwaysRejects() {
        val candidate = AmarStrategyEvolutionEngine.mutate("A", "change entry", AmarStrategyEvolutionEngine.MutationType.ENTRY)
        val d = AmarStrategyEvolutionEngine.gate(candidate, AmarStrategyEvolutionEngine.Score(1.0, 2.0, 1.5, 100, 0.5, 0.4, true, 0.2))
        assertEquals(AmarStrategyEvolutionEngine.Gate.REJECT, d.gate)
    }

    @Test fun positiveRobustCandidateCanReachApprovalReady() {
        val candidate = AmarStrategyEvolutionEngine.mutate("A", "validated exit", AmarStrategyEvolutionEngine.MutationType.EXIT, 0.01)
        val d = AmarStrategyEvolutionEngine.gate(candidate, AmarStrategyEvolutionEngine.Score(0.8, 0.5, 1.8, 500, 0.65, 0.45, false, 0.2))
        assertTrue(d.gate == AmarStrategyEvolutionEngine.Gate.APPROVAL_READY || d.gate == AmarStrategyEvolutionEngine.Gate.HUMAN_REVIEW)
    }
}
