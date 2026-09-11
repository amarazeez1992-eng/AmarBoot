package com.personal.gridbot.amaros.broker

/**
 * Live UI boundary for BOT1. It discovers the authenticated MT5 account read-only,
 * then delegates every destructive/lifecycle operation to the verified facade.
 * If the runtime registry is not installed, no command is sent.
 */
object AmarBot1UiCommandGateway {
    suspend fun execute(
        symbol: String,
        command: AmarBot1RemoteCommandType,
        targetSymbol: String? = null,
        enabled: Boolean? = null,
        settings: AmarBot1RemoteSettings? = null,
    ): AmarBrokerResult {
        if (symbol.isBlank()) return AmarBrokerResult(false, false, "", "رمز التداول غير صالح")
        val runtime = AmarBot1CommandRuntimeRegistry.current()
            ?: return AmarBrokerResult(false, false, "", "Runtime غير مهيأ — لا يوجد تنفيذ حي")
        val account = runtime.client.accountSnapshot()
            ?: return AmarBrokerResult(false, false, "", "تعذر قراءة حساب MT5 — تم منع التنفيذ")
        if (!account.connected || account.login <= 0L) {
            return AmarBrokerResult(false, false, "", "حساب MT5 غير متصل — تم منع التنفيذ")
        }
        val facade = AmarBot1LifecycleFacade(
            verifier = runtime.verifier,
            accountLogin = account.login,
            botMagic = AmarBot1UiMagic.BOT1,
            symbol = symbol,
        )
        return when (command) {
            AmarBot1RemoteCommandType.START -> facade.start(targetSymbol)
            AmarBot1RemoteCommandType.STOP -> facade.stop()
            AmarBot1RemoteCommandType.REBUILD -> facade.rebuild(targetSymbol)
            AmarBot1RemoteCommandType.CLOSE_ALL -> facade.closeAll()
            AmarBot1RemoteCommandType.CLOSE_BUY -> facade.closeBuy()
            AmarBot1RemoteCommandType.CLOSE_SELL -> facade.closeSell()
            AmarBot1RemoteCommandType.SET_BUY_ENABLED -> enabled?.let { facade.setBuyEnabled(it) }
                ?: AmarBrokerResult(false, false, "", "قيمة BUY غير محددة")
            AmarBot1RemoteCommandType.SET_SELL_ENABLED -> enabled?.let { facade.setSellEnabled(it) }
                ?: AmarBrokerResult(false, false, "", "قيمة SELL غير محددة")
            AmarBot1RemoteCommandType.UPDATE_SETTINGS -> settings?.let { facade.updateSettings(it) }
                ?: AmarBrokerResult(false, false, "", "إعدادات BOT1 غير محددة")
        }
    }
}

object AmarBot1UiMagic {
    const val BOT1: Long = 20260908L
}
