package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarIntentUnderstandingTest {
    private val parser = AmarIntentUnderstanding()

    @Test fun arabic_trade_question_is_understood_without_planner_keywords() {
        val result = parser.understand("هل الذهب مناسب للتحليل الآن؟")
        assertEquals(AgentIntent.TRADE_ANALYSIS, result.intent)
        assertTrue(result.confidence > 0.20)
        assertEquals("YES_NO", result.questionForm)
    }

    @Test fun english_strategy_request_is_understood() {
        val result = parser.understand("How should I design a backtestable strategy?")
        assertEquals(AgentIntent.STRATEGY_DESIGN, result.intent)
        assertEquals("HOW", result.questionForm)
    }

    @Test fun open_general_question_defaults_to_research() {
        val result = parser.understand("ما هو مفهوم الانزلاق السعري؟")
        assertEquals(AgentIntent.RESEARCH, result.intent)
        assertEquals("OPEN", result.questionForm)
    }

    @Test fun planner_exposes_understanding_step() {
        val plan = AmarAgentPlanner().plan(
            AmarAgentRequest("لماذا يتحرك الذهب بسرعة؟"),
            emptyList()
        )
        assertTrue(plan.steps.any { it.startsWith("understand_intent:") })
        assertEquals(AgentIntent.TRADE_ANALYSIS, plan.intent)
    }
}
