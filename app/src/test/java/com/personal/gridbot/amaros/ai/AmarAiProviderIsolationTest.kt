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

    @Test
    fun codeGenerationIsDraftOnlyAndArabicNativePolicyExists() = runBlocking {
        val result = AmarAiAgentEngine().ask("", "", "اكتب كود Kotlin لتطبيق يحسب المتوسط")
        assertTrue(result.answer.isNotBlank())
        assertTrue(result.proposedActions.any { it.startsWith("code_generation") })
        assertTrue(AmarAiToolRegistry.resolve("code_generation")?.authority == AmarAiToolRegistry.Authority.DRAFT_ONLY)
        assertFalse(AmarAiToolRegistry.isExecutionCapable("code_generation"))
        assertTrue(AmarAiLanguagePolicy.NATIVE_LANGUAGE == "ar")
    }
}
