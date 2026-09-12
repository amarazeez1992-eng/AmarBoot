package com.personal.gridbot.amaros.bots

/** Pure reorder rule for the Bot-Lab drag/drop surface. */
object AmarBotDragDropOrder {
    fun move(order: List<Int>, dragged: Int, target: Int): List<Int> {
        val from = order.indexOf(dragged)
        val to = order.indexOf(target)
        if (from < 0 || to < 0 || dragged == target) return order
        return order.toMutableList().apply { add(to, removeAt(from)) }
    }
}
