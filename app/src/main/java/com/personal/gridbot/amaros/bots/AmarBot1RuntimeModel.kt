package com.personal.gridbot.amaros.bots

import kotlin.math.abs

/** Stable runtime identity for the approved BOT 1. */
data class AmarBotIdentity(
    val botId: String = "BOT_1",
    val magic: Long = 20260908L,
    val version: String = "2.00",
    val strategyId: String = "STRATEGY_01"
)

enum class AmarBotRuntimeState {
    OFF, STARTING, RUNNING, STOPPING, REBUILDING, CLOSING, ERROR, EMERGENCY_LOCK
}

enum class AmarBotSyncState { MATCHED, DRIFT, UNKNOWN, ERROR }

data class AmarBot1RuntimeConfig(
    val lot: Double = 0.01,
    val gridStep: Double = 30.0,
    val maxOrders: Int = 10,
    val multiplier: Double = 2.0,
    val basketTp: Double = 50.0,
    val basketSl: Double = -30.0,
    val trailing: Double = 0.0,
    val buyEnabled: Boolean = true,
    val sellEnabled: Boolean = true
) {
    init {
        require(lot.isFinite() && lot > 0.0)
        require(gridStep.isFinite() && gridStep > 0.0)
        require(maxOrders > 0)
        require(multiplier.isFinite() && multiplier > 0.0)
        require(basketTp.isFinite())
        require(basketSl.isFinite())
        require(trailing.isFinite() && trailing >= 0.0)
    }
}

data class AmarBot1DesiredState(
    val identity: AmarBotIdentity = AmarBotIdentity(),
    val runtimeState: AmarBotRuntimeState = AmarBotRuntimeState.OFF,
    val config: AmarBot1RuntimeConfig = AmarBot1RuntimeConfig()
)

data class AmarBot1ActualState(
    val identity: AmarBotIdentity? = null,
    val runtimeState: AmarBotRuntimeState? = null,
    val config: AmarBot1RuntimeConfig? = null,
    val openPositions: Int = 0,
    val pendingOrders: Int = 0,
    val equity: Double? = null,
    val balance: Double? = null,
    val lastError: String? = null
)

fun reconcileBot1(desired: AmarBot1DesiredState, actual: AmarBot1ActualState): AmarBotSyncState {
    if (actual.identity == null || actual.config == null || actual.runtimeState == null) return AmarBotSyncState.UNKNOWN
    if (desired.identity.botId != actual.identity.botId || desired.identity.magic != actual.identity.magic) return AmarBotSyncState.ERROR
    if (desired.runtimeState != actual.runtimeState) return AmarBotSyncState.DRIFT
    val d = desired.config
    val a = actual.config
    val same = abs(d.lot - a.lot) < 1e-9 &&
        abs(d.gridStep - a.gridStep) < 1e-9 &&
        d.maxOrders == a.maxOrders &&
        abs(d.multiplier - a.multiplier) < 1e-9 &&
        abs(d.basketTp - a.basketTp) < 1e-9 &&
        abs(d.basketSl - a.basketSl) < 1e-9 &&
        abs(d.trailing - a.trailing) < 1e-9 &&
        d.buyEnabled == a.buyEnabled && d.sellEnabled == a.sellEnabled
    return if (same) AmarBotSyncState.MATCHED else AmarBotSyncState.DRIFT
}
