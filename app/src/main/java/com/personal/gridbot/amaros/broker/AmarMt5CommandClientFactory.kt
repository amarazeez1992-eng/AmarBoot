package com.personal.gridbot.amaros.broker

import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient

/** B48: production construction path that cannot accidentally omit device-bound security. */
object AmarMt5CommandClientFactory {
    fun create(
        context: Context,
        config: AmarBridgeConfig,
        signingSecret: String,
        httpClient: OkHttpClient? = null,
        gson: Gson = Gson(),
    ): AmarMt5CommandClient = AmarMt5CommandClient(
        config = config,
        signingSecret = signingSecret,
        httpClient = httpClient ?: OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .readTimeout(config.readTimeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build(),
        gson = gson,
        deviceSecurity = AmarDeviceSecurity(
            sequenceStore = AmarSharedPreferencesDeviceSequenceStore(context)
        ),
    )
}
