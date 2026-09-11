package com.personal.gridbot.amaros.broker

/** B52: observable health state for remote BOT1; no automatic trading action is performed. */
enum class AmarBot1HealthStatus {
    HEALTHY,
    STALE,
    UNAVAILABLE,
    IDENTITY_MISMATCH,
    MARKET_UNREADY,
    EXECUTION_ERROR,
}

data class AmarBot1Health(
    val status: AmarBot1HealthStatus,
    val reason: String,
    val ageMs: Long? = null,
) {
    val safeForExecution: Boolean get() = status == AmarBot1HealthStatus.HEALTHY
}

class AmarBot1HealthMonitor(
    private val maxStateAgeMs: Long = 5_000L,
) {
    init { require(maxStateAgeMs > 0L) }

    fun evaluate(
        state: AmarBot1RemoteState,
        expectedBotId: String = "BOT_1",
        expectedMagic: Long = 20260908L,
        expectedStrategyId: String = "STRATEGY_01",
        expectedStrategyVersion: String = "2.00",
    ): AmarBot1Health {
        if (!state.available) return AmarBot1Health(AmarBot1HealthStatus.UNAVAILABLE, "BOT1_STATE_UNAVAILABLE", state.ageMs)
        if (!state.fresh || state.ageMs == null || state.ageMs < 0L || state.ageMs > maxStateAgeMs) {
            return AmarBot1Health(AmarBot1HealthStatus.STALE, "BOT1_STATE_STALE", state.ageMs)
        }
        if (state.botId != expectedBotId || state.magic != expectedMagic ||
            state.strategyId != expectedStrategyId || state.strategyVersion != expectedStrategyVersion) {
            return AmarBot1Health(AmarBot1HealthStatus.IDENTITY_MISMATCH, "BOT1_IDENTITY_MISMATCH", state.ageMs)
        }
        if (!state.marketReady) return AmarBot1Health(AmarBot1HealthStatus.MARKET_UNREADY, "MARKET_NOT_READY", state.ageMs)
        if (state.lastCommandStatus == "FAILED") return AmarBot1Health(AmarBot1HealthStatus.EXECUTION_ERROR, state.lastError ?: "BOT1_EXECUTION_ERROR", state.ageMs)
        return AmarBot1Health(AmarBot1HealthStatus.HEALTHY, "BOT1_HEALTHY", state.ageMs)
    }
}
