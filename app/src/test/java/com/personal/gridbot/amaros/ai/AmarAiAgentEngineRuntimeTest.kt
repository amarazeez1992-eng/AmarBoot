package com.personal.gridbot.amaros.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiAgentEngineRuntimeTest {
    @Test
    fun engine_accepts_request_and_returns_non_blank_answer() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "Hello")

        assertFalse(result.answer.isBlank())
        assertTrue(result.answer.contains("Hello"))
    }

    @Test
    fun engine_routes_arabic_greeting_through_reasoning() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "مرحبا")

        assertTrue(result.answer.contains("AMAR AI Agent"))
        assertFalse(result.answer.startsWith("فهمت طلبك:"))
    }

    @Test
    fun engine_routes_identity_question_through_canonical_identity() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "من أنت؟")

        assertTrue(result.answer.startsWith("أنا عمار."))
        assertFalse(result.answer.startsWith("فهمت طلبك:"))
    }

    @Test
    fun runtime_gateway_routes_greeting_to_agent_engine() = runBlocking {
        val result = AmarAgentRuntimeGateway(AmarAiAgentEngine()).ask("مرحبا")

        assertTrue(result.answer.contains("AMAR AI Agent"))
        assertEquals("Agent: جاهز", result.status)
    }

    @Test
    fun runtime_gateway_keeps_application_commands_on_command_path() = runBlocking {
        val result = AmarAgentRuntimeGateway(AmarAiAgentEngine()).ask("اذهب إلى الإعدادات")

        assertEquals("تم تنفيذ أمر الواجهة", result.status)
        assertTrue(result.answer.contains("فتحت"))
    }

    @Test
    fun engine_response_is_blocked_when_research_evidence_is_unavailable() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "ابحث وحلل XAUUSD")

        assertTrue(result.answer.contains("لم يتم اعتماد الإجابة بعد"))
        assertTrue(result.answer.contains("no_evidence") || result.answer.contains("source_verification_failed"))
    }
}
