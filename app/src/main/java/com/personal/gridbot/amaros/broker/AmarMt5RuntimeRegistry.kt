package com.personal.gridbot.amaros.broker

import com.personal.gridbot.amaros.chart.AmarMarketDataProvider
import java.util.concurrent.atomic.AtomicReference

/**
 * Runtime wiring point for the real MT5 bridge.
 * Empty by default: the application therefore remains read-only/offline until
 * an authenticated bridge session is explicitly configured by the host layer.
 */
object AmarMt5RuntimeRegistry {
    data class Runtime(
        val client: AmarMt5BridgeClient,
        val marketData: AmarMt5LiveMarketDataProvider,
    ) {
        val provider: AmarMarketDataProvider get() = marketData
    }

    private val current = AtomicReference<Runtime?>(null)

    fun install(config: AmarBridgeConfig): Runtime {
        val runtime = Runtime(
            client = AmarMt5BridgeClient(config),
            marketData = AmarMt5LiveMarketDataProvider(AmarMt5BridgeClient(config)),
        )
        current.set(runtime)
        return runtime
    }

    fun current(): Runtime? = current.get()

    fun provider(): AmarMarketDataProvider? = current.get()?.provider

    fun clear() { current.set(null) }
}
