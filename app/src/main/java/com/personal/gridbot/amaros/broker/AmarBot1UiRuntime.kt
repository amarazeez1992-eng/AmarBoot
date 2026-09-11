package com.personal.gridbot.amaros.broker

import java.util.concurrent.atomic.AtomicReference

/**
 * UI-facing BOT1 runtime boundary.
 *
 * The UI may request lifecycle/settings operations only through this object.
 * A runtime is considered usable only after the caller explicitly installs a
 * fully configured AmarBot1LifecycleFacade. No bridge credentials are stored here.
 */
object AmarBot1UiRuntime {
    private val facade = AtomicReference<AmarBot1LifecycleFacade?>(null)

    fun install(lifecycle: AmarBot1LifecycleFacade) {
        facade.set(lifecycle)
    }

    fun clear() {
        facade.set(null)
    }

    fun isInstalled(): Boolean = facade.get() != null

    suspend fun start(targetSymbol: String?): AmarBrokerResult =
        requireFacade()?.start(targetSymbol)
            ?: unavailable("BOT1 Runtime غير متصل")

    suspend fun stop(): AmarBrokerResult =
        requireFacade()?.stop() ?: unavailable("BOT1 Runtime غير متصل")

    suspend fun rebuild(targetSymbol: String?): AmarBrokerResult =
        requireFacade()?.rebuild(targetSymbol) ?: unavailable("BOT1 Runtime غير متصل")

    suspend fun closeAll(): AmarBrokerResult =
        requireFacade()?.closeAll() ?: unavailable("BOT1 Runtime غير متصل")

    suspend fun closeBuy(): AmarBrokerResult =
        requireFacade()?.closeBuy() ?: unavailable("BOT1 Runtime غير متصل")

    suspend fun closeSell(): AmarBrokerResult =
        requireFacade()?.closeSell() ?: unavailable("BOT1 Runtime غير متصل")

    suspend fun setBuyEnabled(enabled: Boolean): AmarBrokerResult =
        requireFacade()?.setBuyEnabled(enabled) ?: unavailable("BOT1 Runtime غير متصل")

    suspend fun setSellEnabled(enabled: Boolean): AmarBrokerResult =
        requireFacade()?.setSellEnabled(enabled) ?: unavailable("BOT1 Runtime غير متصل")

    suspend fun updateSettings(settings: AmarBot1RemoteSettings): AmarBrokerResult =
        requireFacade()?.updateSettings(settings) ?: unavailable("BOT1 Runtime غير متصل")

    private fun requireFacade(): AmarBot1LifecycleFacade? = facade.get()

    private fun unavailable(message: String): AmarBrokerResult =
        AmarBrokerResult(accepted = false, executed = false, requestId = "", message = message)
}
