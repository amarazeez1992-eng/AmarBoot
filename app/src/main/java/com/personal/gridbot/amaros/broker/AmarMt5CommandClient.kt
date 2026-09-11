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

/** B31/B37/B38: authenticated transport. It never authorizes live execution by itself. */
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
        return@withContext postJson("/commands", body, command.requestId)
    }

    /** B37/B38: queues a full BOT 1 lifecycle command for the MT5 EA. */
    suspend fun submitBot1(
        accountLogin: Long,
        botMagic: Long,
        symbol: String,
        command: AmarBot1RemoteCommandType,
        targetSymbol: String? = null,
        enabled: Boolean? = null,
        settings: AmarBot1RemoteSettings? = null,
        ttlMs: Long = 15_000L,
        idempotencyKey: String = UUID.randomUUID().toString(),
    ): AmarBrokerResult = withContext(Dispatchers.IO) {
        require(accountLogin > 0)
        require(botMagic >= 0)
        require(symbol.isNotBlank())
        require(targetSymbol == null || targetSymbol.length <= 64 && targetSymbol.isNotBlank() && targetSymbol.none { it == '\n' || it == '\r' || it == '\u0000' })
        require(ttlMs in 1_000L..30_000L)
        require(idempotencyKey.isNotBlank())
        if (command == AmarBot1RemoteCommandType.UPDATE_SETTINGS) require(settings != null)
        if (command == AmarBot1RemoteCommandType.SET_BUY_ENABLED || command == AmarBot1RemoteCommandType.SET_SELL_ENABLED) require(enabled != null)
        val now = System.currentTimeMillis()
        val unsigned = AmarBot1RemoteEnvelope(
            idempotencyKey = idempotencyKey,
            issuedAtMs = now,
            expiresAtMs = now + ttlMs,
            accountLogin = accountLogin,
            botMagic = botMagic,
            symbol = symbol,
            command = command,
            targetSymbol = targetSymbol,
            enabled = enabled,
            settings = settings,
            signature = "pending",
        )
        val envelope = AmarBot1RemoteSigner.sign(unsigned, signingSecret)
        val body = gson.toJson(envelope).toRequestBody("application/json; charset=utf-8".toMediaType())
        postJson("/bot1/commands", body, envelope.requestId)
    }

    /** B38: reads the terminal ACK; QUEUED/PENDING is never reported as verified. */
    suspend fun bot1Status(requestId: String): AmarBot1CommandStatus = withContext(Dispatchers.IO) {
        require(requestId.isNotBlank() && requestId.length <= 128 && requestId.none { it == '/' || it == '\\' || it == '\n' || it == '\r' })
        val request = Request.Builder()
            .url(config.baseUrl.trimEnd('/') + "/bot1/commands/" + requestId)
            .header("Authorization", "Bearer ${config.token}")
            .header("X-AMAR-Command-Version", "1")
            .get()
            .build()
        httpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful || raw.isBlank()) {
                return@withContext AmarBot1CommandStatus(requestId, "PENDING", false, 0L, "تعذر التحقق من ACK: HTTP ${response.code}")
            }
            runCatching { gson.fromJson(raw, AmarBot1CommandStatus::class.java) }
                .getOrElse { AmarBot1CommandStatus(requestId, "PENDING", false, 0L, "استجابة ACK غير صالحة") }
        }
    }

    private fun postJson(path: String, body: okhttp3.RequestBody, requestId: String): AmarBrokerResult {
        val request = Request.Builder()
            .url(config.baseUrl.trimEnd('/') + path)
            .header("Authorization", "Bearer ${config.token}")
            .header("X-AMAR-Command-Version", "1")
            .post(body)
            .build()
        httpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (raw.isBlank()) return AmarBrokerResult(false, false, requestId, "استجابة الأمر فارغة")
            val parsed = runCatching { gson.fromJson(raw, AmarBrokerResult::class.java) }.getOrNull()
            if (parsed != null) return parsed
            if (!response.isSuccessful) return AmarBrokerResult(false, false, requestId, "تم رفض الأمر: HTTP ${response.code}")
            return AmarBrokerResult(false, false, requestId, "استجابة الأمر غير صالحة")
        }
    }
}
