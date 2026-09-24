package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
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

    @Test
    fun source_count_clamped_to_policy_maximum() = runBlocking {
        var capturedContext: AmarAgentContext? = null
        val provider = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext): AmarAgentResponse {
                capturedContext = context
                return AmarAgentResponse("ok")
            }
        }

        val core = AmarAgentCore(provider, EmptyTools())

        core.ask(AmarAgentRequest("Research", maximumSourceCount = 200))

        assertEquals(100, capturedContext?.maximumSourceCount)
    }

    @Test
    fun reasoning_provider_failure_propagates_exception(): Unit = runBlocking {
        val provider = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext): AmarAgentResponse {
                throw IllegalStateException("provider failure")
            }
        }

        val core = AmarAgentCore(provider, EmptyTools())

        assertThrows(IllegalStateException::class.java) {
            runBlocking { core.ask(AmarAgentRequest("Hello")) }
        }
    }

    @Test
    fun tools_are_passed_from_registry() = runBlocking {
        val expectedTool = AmarAgentTool(
            id = "test_tool",
            description = "test"
        )
        val registry = object : AmarAgentToolRegistry {
            override fun availableTools(policy: AmarAgentPolicy): List<AmarAgentTool> =
                listOf(expectedTool)
        }
        var capturedContext: AmarAgentContext? = null
        val provider = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext): AmarAgentResponse {
                capturedContext = context
                return AmarAgentResponse("ok")
            }
        }

        val core = AmarAgentCore(provider, registry)

        core.ask(AmarAgentRequest("Hello"))

        assertEquals(listOf(expectedTool), capturedContext?.tools)
    }
}
