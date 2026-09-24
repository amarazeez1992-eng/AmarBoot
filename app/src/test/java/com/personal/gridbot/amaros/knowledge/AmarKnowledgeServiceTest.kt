package com.personal.gridbot.amaros.knowledge

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarKnowledgeServiceTest {
    @Test
    fun publish_and_retrieve_work() = runBlocking {
        val service = AmarKnowledgeService()
        service.publish("market", "Gold", "Gold market note", "test", 1000L)
        val results = service.byTopic("market")
        assertEquals(1, results.size)
        assertEquals("Gold", results.first().title)
    }

    @Test
    fun search_missing_key_returns_empty_result() = runBlocking {
        assertTrue(AmarKnowledgeService().search("missing-key").isEmpty())
    }
}
