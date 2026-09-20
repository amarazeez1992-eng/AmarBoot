package com.personal.gridbot.amaros.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiAgentEngineRuntimeTest {
    @Test
    fun engine_accepts_request_and_returns_non_blank_answer() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "Hello")

        assertFalse(result.answer.isBlank())
        assertTrue(result.answer.startsWith("أهلاً بك. أنا AMAR AI Agent."))
    }

    @Test
    fun arabic_small_talk_gets_a_dedicated_response() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "كيف حالك")

        assertFalse(result.answer.isBlank())
        assertTrue(result.answer.startsWith("أنا بخير، شكرًا لسؤالك."))
        assertFalse(result.answer.contains("أهلاً بك. أنا AMAR AI Agent."))
        assertFalse(result.answer.contains("لم يتم اعتماد الإجابة بعد"))
    }

    @Test
    fun final_progress_reports_real_elapsed_and_source_metadata() = runBlocking {
        val progress = mutableListOf<com.personal.gridbot.amaros.agent.AmarAgentProgress>()
        val result = AmarAiAgentEngine().ask("", "", "مرحبا") { progress += it }

        val final = progress.last()
        assertEquals(com.personal.gridbot.amaros.agent.AgentTaskState.RESPONDING, final.state)
        assertEquals(result.elapsedMs, final.elapsedMs)
        assertEquals(result.sourcesSearched, final.sourcesSearched)
        assertEquals(result.sourcesAccepted, final.sourcesAccepted)
        assertTrue(final.elapsedMs >= 0L)
        assertTrue(final.sourcesSearched >= final.sourcesAccepted)
    }

    @Test
    fun greeting_survives_orchestrator_evidence_enrichment() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "Hello")

        assertTrue(result.answer.startsWith("أهلاً بك. أنا AMAR AI Agent."))
        assertFalse(result.answer.contains("Evidence summary:"))
    }

    @Test
    fun identity_question_returns_local_answer_without_evidence_gate() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "من أنت")
        assertTrue(result.answer.startsWith("أنا AMAR AI Agent."))
        assertFalse(result.answer.contains("لم يتم اعتماد الإجابة بعد"))
    }

    @Test
    fun time_question_returns_device_time_without_evidence_gate() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "كم الوقت الآن")
        assertTrue(result.answer.contains("الوقت الآن حسب ساعة الجهاز:"))
        assertFalse(result.answer.contains("لم يتم اعتماد الإجابة بعد"))
    }

    @Test
    fun ordinary_research_can_return_an_uncertain_answer_without_trading_strict_gate() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "ما هو مفهوم الانزلاق السعري؟")
        assertFalse(result.answer.isBlank())
        assertFalse(result.answer.contains("لم يتم اعتماد الإجابة بعد"))
    }

    @Test
    fun engine_response_is_blocked_when_research_evidence_is_unavailable() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "ابحث وحلل XAUUSD")

        assertTrue(result.answer.contains("لم يتم اعتماد الإجابة بعد"))
        assertTrue(result.answer.contains("no_evidence") || result.answer.contains("source_verification_failed"))
    }
}
