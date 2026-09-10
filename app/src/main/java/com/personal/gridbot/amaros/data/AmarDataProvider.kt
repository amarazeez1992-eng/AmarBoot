package com.personal.gridbot.amaros.data

interface AmarDataProvider {
    fun snapshot(): AmarDataSnapshot
}

/** Deterministic evolving data source for Demo mode. It never places or modifies trades. */
class DemoDataProvider(private val clock: () -> Long = { System.currentTimeMillis() }) : AmarDataProvider {
    private var tick: Long = 0L

    override fun snapshot(): AmarDataSnapshot {
        tick += 1L
        val phase = tick.toDouble() / 8.0
        val mid = 2500.0 + kotlin.math.sin(phase) * 3.5 + kotlin.math.sin(phase * 0.31) * 1.2
        val spread = 0.18 + (kotlin.math.sin(phase * 0.7) + 1.0) * 0.03
        val bid = mid - spread / 2.0
        val ask = mid + spread / 2.0
        val equity = 10000.0 + kotlin.math.sin(phase * 0.23) * 120.0
        val peak = 10180.0
        val positions = listOf(
            PositionSnapshot("DEMO-1", "XAUUSD", "BUY", 0.01, mid - 1.4, bid, (bid - (mid - 1.4)) * 0.01)
        )
        return AmarDataSnapshot(
            market = MarketSnapshot("XAUUSD", "M5", bid, ask, clock()),
            account = AccountSnapshot(10000.0, equity, 120.0, equity - 120.0, peak),
            positions = positions,
            orders = emptyList(),
            bot = BotRuntimeSnapshot(activePositions = positions.size),
            risk = RiskSnapshot(maxDrawdownPercent = 5.0, exposure = 0.01),
            generatedAtEpochMs = clock(),
            demoOnly = true
        )
    }
}
