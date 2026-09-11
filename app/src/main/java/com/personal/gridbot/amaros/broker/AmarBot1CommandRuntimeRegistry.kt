package com.personal.gridbot.amaros.broker

import android.content.Context
import java.util.concurrent.atomic.AtomicReference

/**
 * B54: explicit production wiring boundary for verified BOT1 lifecycle commands.
 *
 * The registry is empty by default. Installing it requires the caller to supply
 * bridge configuration and signing material. Device identity is constructed only
 * through the production factory so Android Keystore + persistent sequencing
 * cannot be accidentally omitted.
 */
object AmarBot1CommandRuntimeRegistry {
    data class Runtime(
        val client: AmarMt5CommandClient,
        val verifier: AmarBot1CommandVerifier,
    )

    private val current = AtomicReference<Runtime?>(null)

    fun install(
        context: Context,
        config: AmarBridgeConfig,
        signingSecret: String,
    ): Runtime {
        require(signingSecret.isNotBlank()) { "مفتاح توقيع BOT1 مطلوب" }
        val client = AmarMt5CommandClientFactory.create(
            context = context.applicationContext,
            config = config,
            signingSecret = signingSecret,
        )
        val runtime = Runtime(
            client = client,
            verifier = AmarBot1CommandVerifier(client),
        )
        current.set(runtime)
        return runtime
    }

    fun current(): Runtime? = current.get()

    fun clear() {
        current.set(null)
    }
}
