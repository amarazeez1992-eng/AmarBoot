package com.personal.gridbot.amaros.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiProviderIsolationTest {
    @Test
    fun defaultAgentIsProviderNeutral() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "حلل السوق")
        assertTrue(result.answer.isNotBlank())
        assertTrue(result.proposedActions.isEmpty())
        assertFalse(result.answer.contains("Gemini", ignoreCase = true))
        assertFalse(result.answer.contains("API key", ignoreCase = true))
    }

    @Test
    fun engineMeshIsProviderNeutralAndNonExecution() {
        val snapshot = AmarAiEngineMesh().snapshot()
        assertTrue(snapshot.providerNeutral)
        assertFalse(snapshot.executionAuthority)
        assertTrue(snapshot.intelligenceEngineCount >= 1)
    }
}
