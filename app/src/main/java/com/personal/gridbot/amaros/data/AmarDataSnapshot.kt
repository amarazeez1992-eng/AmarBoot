package com.personal.gridbot.amaros.data

/** Aggregate read-only state consumed by intelligence and presentation layers. */
data class AmarDataSnapshot(
    val market: MarketSnapshot = MarketSnapshot(),
    val account: AccountSnapshot = AccountSnapshot(),
    val positions: List<PositionSnapshot> = emptyList(),
    val orders: List<OrderSnapshot> = emptyList(),
    val bot: BotRuntimeSnapshot = BotRuntimeSnapshot(),
    val risk: RiskSnapshot = RiskSnapshot(),
    val generatedAtEpochMs: Long = 0L,
    val demoOnly: Boolean = true
)
