package com.personal.gridbot.amaros.broker

import com.personal.gridbot.amaros.bots.AmarBot1ActualState
import com.personal.gridbot.amaros.bots.AmarBot1DesiredState
import com.personal.gridbot.amaros.bots.AmarBot1ReconciliationEngine
import com.personal.gridbot.amaros.bots.AmarBot1RuntimeConfig
import com.personal.gridbot.amaros.bots.AmarBotIdentity
import com.personal.gridbot.amaros.bots.AmarBotRuntimeState
import com.personal.gridbot.amaros.bots.AmarBotSyncState

/** B43-B46: converts terminal read-back into the existing canonical runtime model. */
class AmarBot1VerifiedRuntime(
    private val reconciliation: AmarBot1ReconciliationEngine = AmarBot1ReconciliationEngine()
) {
    fun actualFromRemote(state: AmarBot1RemoteState): AmarBot1ActualState {
        val identity = if (state.botId != null && state.magic != null && state.strategyVersion != null) {
            AmarBotIdentity(botId = state.botId, magic = state.magic, version = state.strategyVersion)
        } else null
        val runtime = state.runtimeState?.let { runCatching { AmarBotRuntimeState.valueOf(it) }.getOrNull() }
        val config = if (state.lotStart != null && state.gridStep != null && state.maxOrders != null &&
            state.martingale != null && state.basketTp != null && state.basketSl != null && state.trailing != null) {
            runCatching {
                AmarBot1RuntimeConfig(
                    lot = state.lotStart,
                    gridStep = state.gridStep,
                    maxOrders = state.maxOrders,
                    multiplier = state.martingale,
                    basketTp = state.basketTp,
                    basketSl = state.basketSl,
                    trailing = state.trailing.toDouble(),
                    buyEnabled = state.buyEnabled,
                    sellEnabled = state.sellEnabled,
                )
            }.getOrNull()
        } else null
        return AmarBot1ActualState(
            identity = identity,
            runtimeState = runtime,
            config = config,
            openPositions = state.openPositions,
            pendingOrders = state.pendingOrders,
            lastError = state.lastError,
        )
    }

    fun reconcile(desired: AmarBot1DesiredState, state: AmarBot1RemoteState): AmarBotSyncState {
        if (!state.available || !state.fresh || !state.marketReady) return AmarBotSyncState.UNKNOWN
        return reconciliation.evaluate(desired, actualFromRemote(state))
    }

    fun reconcile(desired: AmarBot1DesiredState, state: AmarBot1RemoteState, expectedTargetSymbol: String): AmarBotSyncState {
        if (expectedTargetSymbol.isBlank() || state.targetSymbol != expectedTargetSymbol) return AmarBotSyncState.ERROR
        return reconcile(desired, state)
    }

    fun isVerified(desired: AmarBot1DesiredState, state: AmarBot1RemoteState, expectedTargetSymbol: String? = null): Boolean =
        (expectedTargetSymbol == null || state.targetSymbol == expectedTargetSymbol) &&
            reconcile(desired, state) == AmarBotSyncState.MATCHED
}
