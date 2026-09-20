package com.personal.gridbot.amaros.agent

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
        assertTrue(result.entities.contains("GOLD"))
    }

    @Test fun english_strategy_request_is_understood() {
        val result = parser.understand("How should I design a backtestable strategy for a bot?")
        assertEquals(AgentIntent.STRATEGY_DESIGN, result.intent)
        assertEquals("HOW", result.questionForm)
        assertTrue(result.entities.contains("BOT"))
    }

    @Test fun open_general_question_defaults_to_research() {
        val result = parser.understand("ما هو مفهوم الانزلاق السعري؟")
        assertEquals(AgentIntent.RESEARCH, result.intent)
        assertEquals("OPEN", result.questionForm)
    }

    @Test fun greeting_normalization_handles_arabic_diacritics() {
        val result = parser.understand("مرحباً")
        assertEquals(AgentIntent.GENERAL, result.intent)
        assertEquals("GREETING", result.questionForm)
    }

    @Test fun mixed_request_exposes_multiple_signals() {
        val result = parser.understand("اشرح استراتيجية الذهب ولماذا يتحرك السوق؟")
        assertTrue(result.signals.size >= 2)
    }

    @Test fun planner_exposes_understanding_and_entities() {
        val plan = AmarAgentPlanner().plan(
            AmarAgentRequest("كيف أختبر استراتيجية ذهب؟"),
            emptyList()
        )
        assertTrue(plan.steps.any { it.startsWith("understand_intent:HOW") })
        assertTrue(plan.steps.any { it.startsWith("entities:GOLD") })
    }
}
