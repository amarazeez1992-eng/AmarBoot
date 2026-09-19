package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAgentCoreRuntimeTest {
    private class EmptyTools : AmarAgentToolRegistry {
        override fun availableTools(policy: AmarAgentPolicy): List<AmarAgentTool> = emptyList()
    }

    @Test
    fun hello_reaches_reasoning_and_returns_response() = runBlocking {
        val core = AmarAgentCore(
            reasoningProvider = AmarLocalReasoning(),
            toolRegistry = EmptyTools()
        )

        val response = core.ask(AmarAgentRequest("Hello"))

        assertEquals(AmarAgentResponse.Status.READY, response.status)
        assertTrue(response.answer.contains("AMAR AI Agent"))
        assertFalse(response.answer.isBlank())
    }

    @Test
    fun disabled_agent_returns_explicit_blocked_response() = runBlocking {
        val core = AmarAgentCore(
            reasoningProvider = AmarLocalReasoning(),
            toolRegistry = EmptyTools(),
            policy = AmarAgentPolicy(agentEnabled = false)
        )

        val response = core.ask(AmarAgentRequest("Hello"))

        assertEquals(AmarAgentResponse.Status.BLOCKED, response.status)
        assertTrue(response.answer.isNotBlank())
    }
}
