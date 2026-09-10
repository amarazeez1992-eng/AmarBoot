package com.personal.gridbot.amaros.bots

/** Stable ten-slot strategy catalog for the approved BOT 1. */
data class AmarBot1StrategySlot(
    val strategyId: String,
    val slot: Int,
)

object AmarBot1StrategyCatalog {
    const val MAX_SLOTS = 10

    fun defaults(): List<AmarBot1StrategySlot> =
        (1..MAX_SLOTS).map { slot ->
            AmarBot1StrategySlot(
                strategyId = "STRATEGY_${slot.toString().padStart(2, '0')}",
                slot = slot,
            )
        }
}
