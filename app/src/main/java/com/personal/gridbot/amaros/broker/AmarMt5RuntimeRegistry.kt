package com.personal.gridbot.amaros.broker

import com.personal.gridbot.amaros.chart.AmarMarketDataProvider
import java.util.concurrent.atomic.AtomicReference

/** Explicit MT5 runtime wiring point; empty by default and therefore fail-closed. */
object AmarMt5RuntimeRegistry {
    data class Runtime(
        val client: AmarMt5BridgeClient,
        val marketData: AmarMt5LiveMarketDataProvider,
    ) {
        val provider: AmarMarketDataProvider get() = marketData
    }

    private val current = AtomicReference<Runtime?>(null)

    fun install(config: AmarBridgeConfig): Runtime {
        val client = AmarMt5BridgeClient(config)
        val runtime = Runtime(client, AmarMt5LiveMarketDataProvider(client))
        current.set(runtime)
        return runtime
    }

    fun current(): Runtime? = current.get()
    fun provider(): AmarMarketDataProvider? = current.get()?.provider
    fun clear() { current.set(null) }
}
