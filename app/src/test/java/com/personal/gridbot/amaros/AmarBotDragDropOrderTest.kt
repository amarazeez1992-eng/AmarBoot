package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.bots.AmarBotDragDropOrder
import org.junit.Assert.assertEquals
import org.junit.Test

class AmarBotDragDropOrderTest {
    @Test fun movesBotToTargetAndPreservesTenIdentitySlots() {
        val source = (1..10).toList()
        val moved = AmarBotDragDropOrder.move(source, dragged = 1, target = 7)
        assertEquals(listOf(2, 3, 4, 5, 6, 7, 1, 8, 9, 10), moved)
        assertEquals(10, moved.distinct().size)
        assertEquals((1..10).toSet(), moved.toSet())
    }

    @Test fun invalidTargetDoesNotMutateOrder() {
        val source = (1..10).toList()
        assertEquals(source, AmarBotDragDropOrder.move(source, dragged = 1, target = 99))
    }
}
