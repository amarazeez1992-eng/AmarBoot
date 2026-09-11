package com.personal.gridbot.amaros.data

/** Canonical read-only portfolio snapshot. Live values are supplied by the broker bridge later. */
data class AmarPortfolioState(
    val balance: Double? = null,
    val equity: Double? = null,
    val floatingProfitLoss: Double? = null,
    val openPositions: Int = 0,
    val pendingOrders: Int = 0,
    val buyExposure: Double? = null,
    val sellExposure: Double? = null,
    val connection: String = "DISCONNECTED",
    val source: String = "NONE",
    val timestampEpochMs: Long = 0L
) {
    val hasLiveSource: Boolean get() = source != "NONE" && timestampEpochMs > 0L
}

object AmarPortfolioStateStore {
    @Volatile private var state = AmarPortfolioState()
    fun current(): AmarPortfolioState = state
    fun publish(next: AmarPortfolioState) { state = next }
    fun reset() { state = AmarPortfolioState() }
}
