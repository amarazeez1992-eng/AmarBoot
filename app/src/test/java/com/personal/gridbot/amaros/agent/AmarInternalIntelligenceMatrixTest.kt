package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarInternalIntelligenceMatrixTest {
    @Test
    fun matrix_runs_parallel_signals_without_generating_an_answer() = runBlocking {
        val request = AmarAgentRequest("كم سعر الذهب الآن؟", requireCrossValidation = true)
        val tools = emptyList<AmarAgentTool>()
        val plan = AmarAgentPlanner().plan(request, tools)
        val report = AmarInternalIntelligenceMatrix().evaluate(request, tools, plan)
        assertEquals(AgentIntent.GENERAL, report.intent)
        assertTrue(report.understanding.questionCount > 0)
        assertTrue(report.analysis.signals.contains("time_sensitive"))
        assertTrue(report.reasoning.evidenceNeeded)
        assertTrue(report.inspection.valid)
    }

    @Test
    fun matrix_detects_invalid_input_without_crashing() = runBlocking {
        val request = AmarAgentRequest("x", requestedSourceCount = 2, maximumSourceCount = 1)
        val plan = AmarAgentPlanner().plan(request, emptyList())
        val report = AmarInternalIntelligenceMatrix().evaluate(request, emptyList(), plan)
        assertTrue(report.inspection.issues.contains("source_budget_inconsistent"))
    }
}