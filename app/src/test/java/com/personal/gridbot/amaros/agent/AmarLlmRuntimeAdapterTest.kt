package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarLlmRuntimeAdapterTest {
    @Test
    fun deterministicFallbackIsReadyAndHasNoModel() {
        val adapter = AmarDeterministicLlmAdapter()
        assertTrue(adapter.isReady)
        assertEquals("deterministic-fallback", adapter.runtimeId)
        assertEquals(null, adapter.modelId)
    }

    @Test
    fun generationReturnsSafeFallbackResponse() = runTest {
        val result = AmarDeterministicLlmAdapter().generate(
            AmarLlmGenerationRequest("analyze gold")
        )
        assertTrue(result is AmarLlmGenerationResult.Success)
        assertTrue((result as AmarLlmGenerationResult.Success).text.contains("No broker action"))
    }

    @Test
    fun invalidGenerationRequestIsRejected() {
        var rejected = false
        try {
            AmarLlmGenerationRequest("")
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
    }
}
