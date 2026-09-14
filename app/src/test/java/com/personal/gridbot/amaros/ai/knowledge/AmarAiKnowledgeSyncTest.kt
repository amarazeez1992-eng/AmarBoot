package com.personal.gridbot.amaros.ai.knowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiKnowledgeSyncTest {
    @Test
    fun refresh_policy_has_multiple_independent_knowledge_domains() {
        assertTrue(AmarAiKnowledgeSyncPolicy.topics.size >= 5)
        assertTrue(AmarAiKnowledgeSyncPolicy.topics.distinct().size == AmarAiKnowledgeSyncPolicy.topics.size)
    }

    @Test
    fun refresh_interval_is_six_hours() {
        assertEquals(6L, AmarAiKnowledgeSyncPolicy.INTERVAL_HOURS)
    }

    @Test
    fun work_name_is_stable() {
        assertEquals("amar_ai_knowledge_sync", AmarAiKnowledgeSyncPolicy.WORK_NAME)
    }
}
