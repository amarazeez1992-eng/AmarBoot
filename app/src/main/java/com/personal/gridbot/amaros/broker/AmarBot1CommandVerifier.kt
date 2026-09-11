package com.personal.gridbot.amaros.broker

import kotlinx.coroutines.delay

/** B50: queue acceptance is never exposed as execution success. */
class AmarBot1CommandVerifier(
    private val client: AmarMt5CommandClient,
    private val pollIntervalMs: Long = 250L,
    private val maxWaitMs: Long = 7_500L,
) {
    init {
        require(pollIntervalMs in 50L..2_000L)
        require(maxWaitMs in pollIntervalMs..60_000L)
    }

    suspend fun submitAndVerify(
        accountLogin: Long,
        botMagic: Long,
        symbol: String,
        command: AmarBot1RemoteCommandType,
        targetSymbol: String? = null,
        enabled: Boolean? = null,
        settings: AmarBot1RemoteSettings? = null,
        ttlMs: Long = 15_000L,
        idempotencyKey: String = java.util.UUID.randomUUID().toString(),
    ): AmarBrokerResult {
        val submitted = client.submitBot1(
            accountLogin = accountLogin,
            botMagic = botMagic,
            symbol = symbol,
            command = command,
            targetSymbol = targetSymbol,
            enabled = enabled,
            settings = settings,
            ttlMs = ttlMs,
            idempotencyKey = idempotencyKey,
        )
        if (!submitted.accepted) return submitted

        val started = System.currentTimeMillis()
        while (System.currentTimeMillis() - started <= maxWaitMs) {
            val ack = client.bot1Status(submitted.requestId)
            if (ack.status == "FAILED" || (ack.status == "REJECTED" && !ack.accepted)) {
                return AmarBrokerResult(false, false, submitted.requestId, ack.message ?: "تم رفض تنفيذ الأمر")
            }
            if (ack.accepted && ack.status == "VERIFIED") {
                val state = client.bot1State()
                if (isPostConditionVerified(command, state, submitted.requestId, targetSymbol, enabled, settings)) {
                    return AmarBrokerResult(true, true, submitted.requestId, "تم التحقق من التنفيذ الفعلي")
                }
            }
            delay(pollIntervalMs)
        }
        return AmarBrokerResult(false, false, submitted.requestId, "انتهت مهلة التحقق: لم يثبت التنفيذ الفعلي")
    }

    private fun isPostConditionVerified(
        command: AmarBot1RemoteCommandType,
        state: AmarBot1RemoteState,
        requestId: String,
        targetSymbol: String?,
        enabled: Boolean?,
        settings: AmarBot1RemoteSettings?,
    ): Boolean {
        if (!state.available || !state.fresh) return false
        if (targetSymbol != null && state.targetSymbol != targetSymbol) return false
        if (state.lastRequestId != requestId || state.lastCommandStatus != "VERIFIED") return false
        return when (command) {
            AmarBot1RemoteCommandType.START,
            AmarBot1RemoteCommandType.REBUILD -> state.runtimeState == "RUNNING" && state.marketReady
            AmarBot1RemoteCommandType.STOP -> state.runtimeState == "OFF" && !state.isTrading
            AmarBot1RemoteCommandType.CLOSE_ALL -> state.openPositions == 0 && state.pendingOrders == 0
            AmarBot1RemoteCommandType.SET_BUY_ENABLED -> state.buyEnabled == enabled
            AmarBot1RemoteCommandType.SET_SELL_ENABLED -> state.sellEnabled == enabled
            AmarBot1RemoteCommandType.UPDATE_SETTINGS -> settings != null && settingsMatch(state, settings)
        }
    }

    private fun settingsMatch(state: AmarBot1RemoteState, settings: AmarBot1RemoteSettings): Boolean =
        state.lotStart == settings.lotStart &&
            state.gridStep == settings.gridStep &&
            state.maxOrders == settings.maxOrders &&
            state.martingale == settings.martingale &&
            state.basketTp == settings.basketTp &&
            state.basketSl == settings.basketSl &&
            state.trailing?.toDouble() == settings.trailing &&
            state.buyEnabled == settings.buyEnabled &&
            state.sellEnabled == settings.sellEnabled
}
