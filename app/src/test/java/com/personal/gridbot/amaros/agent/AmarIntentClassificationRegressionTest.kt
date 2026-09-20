package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarIntentClassificationRegressionTest {
    private val parser = AmarIntentUnderstanding()

    @Test
    fun arabic_definite_article_does_not_hide_trade_intent() {
        val result = parser.understand("حلل الذهب XAUUSD الآن")
        assertEquals(AgentIntent.TRADE_ANALYSIS, result.intent)
        assertTrue(result.entities.contains("GOLD"))
        assertTrue(result.entities.contains("XAUUSD"))
    }

    @Test
    fun financial_instrument_request_with_research_word_still_uses_trade_intent() {
        val result = parser.understand("ابحث وحلل XAUUSD")
        assertEquals(AgentIntent.TRADE_ANALYSIS, result.intent)
        assertTrue(result.entities.contains("XAUUSD"))
    }

    @Test
    fun arabic_factual_question_form_is_research() {
        val result = parser.understand("ما هي عاصمة فرنسا؟")
        assertEquals(AgentIntent.RESEARCH, result.intent)
    }
}
