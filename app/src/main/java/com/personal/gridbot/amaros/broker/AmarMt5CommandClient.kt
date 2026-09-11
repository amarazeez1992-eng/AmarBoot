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

/** B31/B37-B42: authenticated transport with optional Android Keystore device binding. */
class AmarMt5CommandClient(
    private val config: AmarBridgeConfig,
    private val signingSecret: String,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
        .build(),
    private val gson: Gson = Gson(),
    private val deviceSecurity: AmarDeviceSecurity? = null,
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

    /** B42: BOT1 lifecycle commands are device-bound and strictly sequenced. */
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
        val security = deviceSecurity ?: return@withContext AmarBrokerResult(false, false, idempotencyKey, "هوية الجهاز الآمنة غير مهيأة")
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
            deviceId = security.deviceId,
            sequence = security.nextSequence(),
            devicePublicKey = security.publicKeyBase64(),
            deviceSignature = "pending",
            signature = "pending",
        )
        val deviceSignature = security.sign(AmarBot1RemoteSigner.deviceCanonical(unsigned))
        val deviceBound = unsigned.copy(deviceSignature = deviceSignature)
        val envelope = AmarBot1RemoteSigner.sign(deviceBound, signingSecret)
        val body = gson.toJson(envelope).toRequestBody("application/json; charset=utf-8".toMediaType())
        postJson("/bot1/commands", body, envelope.requestId)
    }

    /** Read-only account discovery used by the live BOT1 UI; never authorizes execution by itself. */
    suspend fun accountSnapshot(): AmarMt5AccountSnapshot? = withContext(Dispatchers.IO) {
        val request = authenticatedGet("/account")
        httpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful || raw.isBlank()) return@withContext null
            runCatching { gson.fromJson(raw, AmarMt5AccountSnapshot::class.java) }.getOrNull()
        }
    }

    suspend fun bot1Status(requestId: String): AmarBot1CommandAck = withContext(Dispatchers.IO) {
        require(requestId.isNotBlank() && requestId.length <= 128 && requestId.none { it == '/' || it == '\\' || it == '\n' || it == '\r' })
        val request = authenticatedGet("/bot1/commands/$requestId")
        httpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful || raw.isBlank()) {
                return@withContext AmarBot1CommandAck(requestId, "PENDING", false, 0L, "تعذر التحقق من ACK: HTTP ${response.code}")
            }
            runCatching { gson.fromJson(raw, AmarBot1CommandAck::class.java) }
                .getOrElse { AmarBot1CommandAck(requestId, "PENDING", false, 0L, "استجابة ACK غير صالحة") }
        }
    }

    suspend fun bot1Symbols(): List<AmarBot1DiscoveredSymbol> = withContext(Dispatchers.IO) {
        val request = authenticatedGet("/bot1/symbols")
        httpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful || raw.isBlank()) return@withContext emptyList()
            runCatching { gson.fromJson(raw, AmarBot1SymbolDiscoveryResponse::class.java)?.items.orEmpty() }
                .getOrElse { emptyList() }
        }
    }

    suspend fun bot1State(): AmarBot1RemoteState = withContext(Dispatchers.IO) {
        val request = authenticatedGet("/bot1/state")
        httpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (raw.isBlank()) return@withContext AmarBot1RemoteState()
            runCatching { gson.fromJson(raw, AmarBot1RemoteState::class.java) }
                .getOrElse { AmarBot1RemoteState() }
        }
    }

    private fun authenticatedGet(path: String): Request = Request.Builder()
        .url(config.baseUrl.trimEnd('/') + path)
        .header("Authorization", "Bearer ${config.token}")
        .header("X-AMAR-Command-Version", "1")
        .get()
        .build()

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