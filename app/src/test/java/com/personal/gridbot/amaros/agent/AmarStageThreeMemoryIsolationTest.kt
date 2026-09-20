package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AmarStageThreeMemoryIsolationTest {

    @Test
    fun memory_from_different_question_is_not_unified() {
        val memory = AmarAgentMemoryStore()
        val now = 1_000_000L
        memory.remember(
            AmarMemoryRecord(
                key = "emma",
                query = "ما عاصمة امريكا؟",
                content = "Emma Goldman was an American activist and writer.",
                sourceUri = "https://example.com/emma",
                sourceTitle = "Emma Goldman",
                fingerprint = "emma",
                createdAtEpochMs = now,
                expiresAtEpochMs = now + 86_400_000L
            )
        )

        val stage = AmarStageThreeEngine(memory = memory)
        val result = stage.synchronize("كم عدد الاحرف العربية والانكليزية؟", emptyList(), now)

        assertEquals(0, result.retrievedMemoryCount)
        assertFalse(result.unifiedEvidence.any { it.sourceTitle == "Emma Goldman" })
    }

    @Test
    fun exact_same_question_memory_can_be_reused() {
        val memory = AmarAgentMemoryStore()
        val now = 1_000_000L
        memory.remember(
            AmarMemoryRecord(
                key = "capital",
                query = "ما عاصمة امريكا؟",
                content = "Washington, D.C. is the capital of the United States.",
                sourceUri = "https://example.com/washington",
                sourceTitle = "Washington, D.C.",
                fingerprint = "capital",
                createdAtEpochMs = now,
                expiresAtEpochMs = now + 86_400_000L
            )
        )

        val stage = AmarStageThreeEngine(memory = memory)
        val result = stage.synchronize("ما عاصمة امريكا؟", emptyList(), now)

        assertEquals(1, result.retrievedMemoryCount)
        assertEquals("Washington, D.C.", result.unifiedEvidence.single().sourceTitle)
    }
}
