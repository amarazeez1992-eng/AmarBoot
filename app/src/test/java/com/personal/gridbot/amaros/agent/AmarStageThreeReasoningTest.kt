package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarStageThreeReasoningTest {
    private class SequencedProvider(private val answers: ArrayDeque<String>) : AmarReasoningProvider {
        var calls = 0
        override suspend fun respond(context: AmarAgentContext): AmarAgentResponse {
            calls++
            return AmarAgentResponse(answers.removeFirst())
        }
    }

    @Test
    fun accepted_draft_does_not_retry() = runBlocking {
        val provider = SequencedProvider(ArrayDeque(listOf("A supported answer.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"))
        assertEquals(1, provider.calls)
        assertEquals(0, result.revisionCount)
        assertTrue(result.critique.accepted)
        assertEquals(AmarAgentResponse.Status.READY, result.response.status)
    }

    @Test
    fun critic_failure_gets_one_bounded_revision() = runBlocking {
        val provider = SequencedProvider(ArrayDeque(listOf("The result is guaranteed.", "The result is uncertain.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"))
        assertEquals(2, provider.calls)
        assertEquals(1, result.revisionCount)
        assertTrue(result.critique.accepted)
        assertEquals(AmarAgentResponse.Status.READY, result.response.status)
    }

    @Test
    fun repeated_critic_failure_is_blocked_without_another_retry() = runBlocking {
        val provider = SequencedProvider(ArrayDeque(listOf("The result is guaranteed.", "The result is guaranteed.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"))
        assertEquals(2, provider.calls)
        assertEquals(1, result.revisionCount)
        assertTrue(result.critique.issues.isNotEmpty())
        assertEquals(AmarAgentResponse.Status.ERROR, result.response.status)
    }
}
