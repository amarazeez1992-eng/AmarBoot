package com.personal.gridbot.amaros.ai

import kotlinx.coroutines.runBlocking
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
    fun greeting_survives_orchestrator_evidence_enrichment() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "Hello")

        assertTrue(result.answer.startsWith("أهلاً بك. أنا AMAR AI Agent."))
        assertFalse(result.answer.contains("Evidence summary:"))
    }

    @Test
    fun engine_response_is_blocked_when_research_evidence_is_unavailable() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "ابحث وحلل XAUUSD")

        assertTrue(result.answer.contains("لم يتم اعتماد الإجابة بعد"))
        assertTrue(result.answer.contains("no_evidence") || result.answer.contains("source_verification_failed"))
    }
}
