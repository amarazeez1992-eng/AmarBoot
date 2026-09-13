package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarLlmModelManagerTest {
    private fun model(id: String) = AmarLlmModelDescriptor(
        id = id,
        displayName = id,
        estimatedMemoryMb = 2048
    )

    @Test
    fun firstRegisteredModelIsSelected() {
        val manager = AmarLlmModelManager()
        assertTrue(manager.register(model("a")))
        assertEquals("a", manager.selected()?.id)
    }

    @Test
    fun selectionRejectsUnknownModel() {
        val manager = AmarLlmModelManager(listOf(model("a")))
        assertFalse(manager.select("missing"))
        assertEquals("a", manager.selected()?.id)
    }

    @Test
    fun removingSelectedModelFallsBackToAnotherModel() {
        val manager = AmarLlmModelManager(listOf(model("a"), model("b")))
        assertTrue(manager.select("b"))
        assertTrue(manager.remove("b"))
        assertEquals("a", manager.selected()?.id)
    }
}
