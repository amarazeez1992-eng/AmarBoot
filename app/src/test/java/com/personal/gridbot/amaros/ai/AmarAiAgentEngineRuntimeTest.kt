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
        assertTrue(result.answer.contains("Hello"))
    }
}
