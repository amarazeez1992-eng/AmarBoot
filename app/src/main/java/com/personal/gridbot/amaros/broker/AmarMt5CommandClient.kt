package com.personal.gridbot.amaros.broker

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit

/** B31: write transport. It never authorizes live execution by itself. */
class AmarMt5CommandClient(
    private val config: AmarBridgeConfig,
    private val signingSecret: String,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
        .build(),
    private val gson: Gson = Gson(),
) {
    init { require(signingSecret.isNotBlank()) { "مفتاح توقيع الأوامر مطلوب" } }

    suspend fun submit(
        accountLogin: Long,
        botMagic: Long,
        symbol: String,
        command: AmarBrokerCommand,
        ttlMs: Long = 15_000L,
        idempotencyKey: String = command.requestId,
    ): AmarBrokerResult = withContext(Dispatchers.IO) {
        require(accountLogin > 0)
        require(botMagic >= 0)
        require(symbol.isNotBlank())
        require(ttlMs in 1_000L..30_000L)
        require(idempotencyKey.isNotBlank())
        require(command.requestId.isNotBlank())
        require(command.symbol == symbol) { "رمز الأمر لا يطابق رمز الغلاف" }
        require(command.quantity.isFinite() && command.quantity > 0.0) { "حجم الأمر غير صالح" }
        require(command.price == null || (command.price.isFinite() && command.price > 0.0)) { "سعر الأمر غير صالح" }

        val now = System.currentTimeMillis()
        val unsigned = AmarCommandEnvelope(
            requestId = command.requestId,
            idempotencyKey = idempotencyKey,
            nonce = UUID.randomUUID().toString(),
            issuedAtMs = now,
            expiresAtMs = now + ttlMs,
            accountLogin = accountLogin,
            botMagic = botMagic,
            symbol = symbol,
            command = command,
            signature = "pending",
        )
        val envelope = unsigned.copy(signature = AmarCommandSigner.hmacSha256(signingSecret, AmarCommandSigner.canonical(unsigned)))
        val body = gson.toJson(envelope).toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(config.baseUrl.trimEnd('/') + "/commands")
            .header("Authorization", "Bearer ${config.token}")
            .header("X-AMAR-Command-Version", "1")
            .post(body)
            .build()

        httpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (raw.isBlank()) return@withContext AmarBrokerResult(false, false, command.requestId, "استجابة الأمر فارغة")
            val parsed = runCatching { gson.fromJson(raw, AmarBrokerResult::class.java) }.getOrNull()
            if (parsed != null) return@withContext parsed
            if (!response.isSuccessful) return@withContext AmarBrokerResult(false, false, command.requestId, "تم رفض الأمر: HTTP ${response.code}")
            AmarBrokerResult(false, false, command.requestId, "استجابة الأمر غير صالحة")
        }
    }
}
