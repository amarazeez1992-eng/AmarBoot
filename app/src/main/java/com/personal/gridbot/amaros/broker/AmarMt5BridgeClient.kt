package com.personal.gridbot.amaros.broker

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/** B29: Android-side read-only client. No trade endpoint is exposed here. */
class AmarMt5BridgeClient(
    private val config: AmarBridgeConfig,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
        .build(),
    private val gson: Gson = Gson(),
) {
    suspend fun health(): AmarMt5Health = get("/health")
    suspend fun account(): AmarMt5AccountSnapshot = get("/account")

    suspend fun market(symbol: String): AmarMt5MarketSnapshot {
        require(symbol.isNotBlank()) { "رمز السوق مطلوب" }
        return get("/market?symbol=${encode(symbol)}")
    }

    suspend fun botStatus(symbol: String? = null, magic: Long? = null): AmarMt5BotStatus {
        val params = buildList {
            if (!symbol.isNullOrBlank()) add("symbol=${encode(symbol)}")
            if (magic != null) add("magic=$magic")
        }.joinToString("&")
        return get("/bot-status${if (params.isEmpty()) "" else "?$params"}")
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())

    private suspend inline fun <reified T> get(path: String): T = withContext(Dispatchers.IO) {
        require(path.startsWith("/"))
        val request = Request.Builder()
            .url(config.baseUrl.trimEnd('/') + path)
            .header("Authorization", "Bearer ${config.token}")
            .header("Cache-Control", "no-cache")
            .get()
            .build()
        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("جسر MT5 رفض الطلب: HTTP ${response.code}")
            }
            if (body.isBlank()) throw IllegalStateException("استجابة جسر MT5 فارغة")
            gson.fromJson(body, T::class.java)
                ?: throw IllegalStateException("استجابة جسر MT5 غير صالحة")
        }
    }
}

data class AmarMt5BotStatus(
    val ok: Boolean,
    val available: Boolean,
    val magic: Long,
    val symbol: String?,
    val positions: Int,
    val pendingOrders: Int,
    val floatingProfit: Double,
    val lastCheckMs: Long,
)
