package com.personal.gridbot.amaros.broker

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/** B29/B30: Android-side read-only client. No trade endpoint is exposed here. */
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

    suspend fun candles(symbol: String, timeframe: String, count: Int = 80): AmarMt5CandleSeries {
        require(symbol.isNotBlank()) { "رمز السوق مطلوب" }
        require(timeframe.isNotBlank()) { "الفاصل الزمني مطلوب" }
        require(count in 10..500) { "عدد الشموع يجب أن يكون بين 10 و500" }
        return get("/candles?symbol=${encode(symbol)}&timeframe=${encode(timeframe)}&count=$count")
    }

    suspend fun botStatus(symbol: String? = null, magic: Long? = null): AmarMt5BotStatus {
        val params = buildList {
            if (!symbol.isNullOrBlank()) add("symbol=${encode(symbol)}")
            if (magic != null) add("magic=$magic")
        }.joinToString("&")
        return get("/bot-status${if (params.isEmpty()) "" else "?$params"}")
    }

    suspend fun positions(symbol: String? = null, magic: Long? = null): AmarMt5ItemsResponse<AmarMt5Position> =
        get(filterPath("/positions", symbol, magic))

    suspend fun pendingOrders(symbol: String? = null, magic: Long? = null): AmarMt5ItemsResponse<AmarMt5PendingOrder> =
        get(filterPath("/pending-orders", symbol, magic))

    private fun filterPath(path: String, symbol: String?, magic: Long?): String {
        val params = buildList {
            if (!symbol.isNullOrBlank()) add("symbol=${encode(symbol)}")
            if (magic != null) add("magic=$magic")
        }.joinToString("&")
        return "$path${if (params.isEmpty()) "" else "?$params"}"
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

data class AmarMt5ItemsResponse<T>(val ok: Boolean, val items: List<T>)

data class AmarMt5Position(
    val ticket: Long,
    val symbol: String,
    val type: Int,
    val volume: Double,
    val priceOpen: Double,
    val priceCurrent: Double,
    val sl: Double,
    val tp: Double,
    val profit: Double,
    val swap: Double,
    val magic: Long,
    val comment: String,
)

data class AmarMt5PendingOrder(
    val ticket: Long,
    val symbol: String,
    val type: Int,
    val volume: Double,
    val priceOpen: Double,
    val sl: Double,
    val tp: Double,
    val magic: Long,
    val comment: String,
)
