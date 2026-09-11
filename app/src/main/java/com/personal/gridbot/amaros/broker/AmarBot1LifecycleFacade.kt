package com.personal.gridbot.amaros.broker

/**
 * B58-B60: single fail-closed facade for BOT1 lifecycle operations.
 * UI callers use this boundary instead of talking to the bridge directly.
 * No operation is reported as executed until ACK + runtime state verification succeeds.
 */
class AmarBot1LifecycleFacade(
    private val verifier: AmarBot1CommandVerifier,
    private val healthMonitor: AmarBot1HealthMonitor = AmarBot1HealthMonitor(),
    private val accountLogin: Long,
    private val botMagic: Long,
    private val symbol: String,
) {
    init {
        require(accountLogin > 0L)
        require(botMagic >= 0L)
        require(symbol.isNotBlank())
    }

    suspend fun start(targetSymbol: String? = null): AmarBrokerResult =
        execute(AmarBot1RemoteCommandType.START, targetSymbol = targetSymbol)

    suspend fun stop(): AmarBrokerResult =
        execute(AmarBot1RemoteCommandType.STOP)

    suspend fun rebuild(targetSymbol: String? = null): AmarBrokerResult =
        execute(AmarBot1RemoteCommandType.REBUILD, targetSymbol = targetSymbol)

    suspend fun closeAll(): AmarBrokerResult =
        execute(AmarBot1RemoteCommandType.CLOSE_ALL)

    suspend fun closeBuy(): AmarBrokerResult =
        execute(AmarBot1RemoteCommandType.CLOSE_BUY)

    suspend fun closeSell(): AmarBrokerResult =
        execute(AmarBot1RemoteCommandType.CLOSE_SELL)

    suspend fun setBuyEnabled(enabled: Boolean): AmarBrokerResult =
        execute(AmarBot1RemoteCommandType.SET_BUY_ENABLED, enabled = enabled)

    suspend fun setSellEnabled(enabled: Boolean): AmarBrokerResult =
        execute(AmarBot1RemoteCommandType.SET_SELL_ENABLED, enabled = enabled)

    suspend fun updateSettings(settings: AmarBot1RemoteSettings): AmarBrokerResult =
        execute(AmarBot1RemoteCommandType.UPDATE_SETTINGS, settings = settings)

    fun health(state: AmarBot1RemoteState): AmarBot1Health =
        healthMonitor.evaluate(state, expectedMagic = botMagic)

    private suspend fun execute(
        command: AmarBot1RemoteCommandType,
        targetSymbol: String? = null,
        enabled: Boolean? = null,
        settings: AmarBot1RemoteSettings? = null,
    ): AmarBrokerResult {
        if (targetSymbol != null && targetSymbol.isBlank()) {
            return AmarBrokerResult(false, false, "", "الرمز المستهدف غير صالح")
        }
        return verifier.submitAndVerify(
            accountLogin = accountLogin,
            botMagic = botMagic,
            symbol = symbol,
            command = command,
            targetSymbol = targetSymbol,
            enabled = enabled,
            settings = settings,
        )
    }
}
