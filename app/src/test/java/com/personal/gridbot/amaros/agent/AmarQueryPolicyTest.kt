package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarQueryPolicyTest {
    private val policy = AmarQueryPolicy()

    @Test fun system_requests_stay_local_without_evidence_gate() {
        val plan = AmarAgentPlanner().plan(AmarAgentRequest("من أنت"), emptyList())
        val decision = policy.classify(plan, AmarAgentRequest("من أنت"))

        assertEquals(AmarQueryPolicyMode.LOCAL_CONVERSATIONAL, decision.mode)
        assertFalse(decision.requiresResearch)
        assertFalse(decision.requiresStrictEvidence)
    }

    @Test fun general_factual_requests_research_without_financial_strict_gate() {
        val plan = AmarAgentPlanner().plan(AmarAgentRequest("ما هي عاصمة فرنسا؟"), emptyList())
        val decision = policy.classify(plan, AmarAgentRequest("ما هي عاصمة فرنسا؟"))

        assertEquals(AmarQueryPolicyMode.GENERAL_FACTUAL, decision.mode)
        assertTrue(decision.requiresResearch)
        assertFalse(decision.requiresStrictEvidence)
    }

    @Test fun financial_analysis_keeps_strict_evidence_gate() {
        val request = AmarAgentRequest("حلل الذهب XAUUSD الآن")
        val plan = AmarAgentPlanner().plan(request, emptyList())
        val decision = policy.classify(plan, request)

        assertEquals(AmarQueryPolicyMode.FINANCIAL_TRADING, decision.mode)
        assertTrue(decision.requiresResearch)
        assertTrue(decision.requiresStrictEvidence)
    }
}
