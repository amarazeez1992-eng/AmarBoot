package com.personal.gridbot.amaros.memory

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AmarMemoryServiceTest {
    @Test
    fun remember_and_recall_work() = runBlocking {
        val service = AmarMemoryService()
        service.remember("test", "key", "value", 1000L)
        val recalled = service.recall("test", "key")
        assertNotNull(recalled)
        assertEquals("value", recalled?.value)
    }

    @Test
    fun recall_missing_key_returns_empty_result() = runBlocking {
        assertNull(AmarMemoryService().recall("test", "missing"))
    }
}
