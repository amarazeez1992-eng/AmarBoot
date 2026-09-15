package com.personal.gridbot.amaros.workspace

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

enum class AmarProviderCapability {
    QUERY,
    MULTIMODAL_ANALYSIS,
    KNOWLEDGE_READ,
    CONVERSATION_READ
}

data class AmarProviderCredential(
    val credentialId: String,
    val clientId: String,
    val capabilities: Set<AmarProviderCapability>,
    val issuedAtEpochMs: Long,
    val expiresAtEpochMs: Long?,
    val revoked: Boolean = false
)

data class AmarProviderCredentialGrant(
    val credential: AmarProviderCredential,
    val secret: String
)

object AmarProviderApiContract {
    const val CURRENT_VERSION = "v1"
}

data class AmarProviderRequest(
    val requestId: String,
    val credentialId: String,
    val secret: String,
    val capability: AmarProviderCapability,
    val payload: String,
    val nowEpochMs: Long,
    val apiVersion: String = AmarProviderApiContract.CURRENT_VERSION
)

data class AmarProviderResponse(
    val requestId: String,
    val accepted: Boolean,
    val result: String = "",
    val reason: String,
    val apiVersion: String = AmarProviderApiContract.CURRENT_VERSION
)

/** Optional owner-controlled provider boundary. No third-party AI provider is required by this API. */
class AmarProviderCredentialStore(private val random: SecureRandom = SecureRandom()) {
    private data class Stored(val credential: AmarProviderCredential, val secretDigest: ByteArray)
    private val credentials = linkedMapOf<String, Stored>()

    @Synchronized
    fun issue(
        clientId: String,
        capabilities: Set<AmarProviderCapability>,
        nowEpochMs: Long,
        expiresAtEpochMs: Long? = null
    ): AmarProviderCredentialGrant? {
        if (clientId.isBlank() || capabilities.isEmpty() || nowEpochMs < 0L) return null
        if (expiresAtEpochMs != null && expiresAtEpochMs <= nowEpochMs) return null
        val id = randomToken(18)
        val secret = randomToken(32)
        val credential = AmarProviderCredential(id, clientId, capabilities.toSet(), nowEpochMs, expiresAtEpochMs)
        credentials[id] = Stored(credential, digest(secret))
        return AmarProviderCredentialGrant(credential, secret)
    }

    @Synchronized
    fun revoke(credentialId: String): Boolean {
        val stored = credentials[credentialId] ?: return false
        if (stored.credential.revoked) return false
        credentials[credentialId] = stored.copy(credential = stored.credential.copy(revoked = true))
        return true
    }

    @Synchronized
    fun metadata(credentialId: String): AmarProviderCredential? = credentials[credentialId]?.credential

    @Synchronized
    fun authorize(request: AmarProviderRequest): Boolean {
        if (request.apiVersion != AmarProviderApiContract.CURRENT_VERSION) return false
        if (request.requestId.isBlank() || request.credentialId.isBlank() || request.secret.isBlank() || request.payload.isBlank() || request.nowEpochMs < 0L) return false
        val stored = credentials[request.credentialId] ?: return false
        val c = stored.credential
        if (c.revoked || c.capabilities.none { it == request.capability }) return false
        if (c.expiresAtEpochMs != null && request.nowEpochMs >= c.expiresAtEpochMs) return false
        return MessageDigest.isEqual(stored.secretDigest, digest(request.secret))
    }

    private fun digest(secret: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(secret.toByteArray(Charsets.UTF_8))

    private fun randomToken(bytes: Int): String {
        val value = ByteArray(bytes)
        random.nextBytes(value)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value)
    }
}

fun interface AmarProviderCapabilityHandler {
    fun handle(request: AmarProviderRequest): String?
}

/** Rate-limited, auditable adapter; execution authority remains outside the provider API boundary. */
class AmarProviderApiGateway(
    private val credentials: AmarProviderCredentialStore,
    private val audit: AmarWorkspaceAuditLog,
    private val handler: AmarProviderCapabilityHandler? = null,
    private val maxRequestsPerWindow: Int = 60,
    private val windowMs: Long = 60_000L
) {
    private data class Counter(var windowStart: Long, var count: Int)
    private val counters = mutableMapOf<String, Counter>()

    init {
        require(maxRequestsPerWindow > 0)
        require(windowMs > 0L)
    }

    @Synchronized
    fun handle(request: AmarProviderRequest): AmarProviderResponse {
        if (request.apiVersion != AmarProviderApiContract.CURRENT_VERSION) {
            return reject(request, "unsupported_api_version")
        }
        val authorized = credentials.authorize(request)
        val allowed = authorized && consume(request.credentialId, request.nowEpochMs)
        val reason = when {
            !authorized -> "unauthorized_or_revoked"
            !allowed -> "rate_limited"
            else -> "accepted"
        }
        if (!allowed) return reject(request, reason)
        val result = try {
            handler?.handle(request)
        } catch (_: RuntimeException) {
            null
        }
        if (handler != null && result == null) return reject(request, "capability_unavailable")
        audit.record(
            AmarWorkspaceAuditEvent(
                id = request.requestId,
                action = "provider_api_request",
                actor = request.credentialId,
                timestampEpochMs = request.nowEpochMs,
                allowed = true,
                reason = reason
            )
        )
        return AmarProviderResponse(request.requestId, true, result ?: request.payload, reason)
    }

    private fun reject(request: AmarProviderRequest, reason: String): AmarProviderResponse {
        audit.record(
            AmarWorkspaceAuditEvent(
                id = request.requestId,
                action = "provider_api_request",
                actor = request.credentialId.ifBlank { "unknown" },
                timestampEpochMs = request.nowEpochMs.coerceAtLeast(0L),
                allowed = false,
                reason = reason
            )
        )
        return AmarProviderResponse(request.requestId, false, reason = reason)
    }

    private fun consume(credentialId: String, now: Long): Boolean {
        val counter = counters[credentialId]
        if (counter == null || now - counter.windowStart >= windowMs) {
            counters[credentialId] = Counter(now, 1)
            return true
        }
        if (now < counter.windowStart) return false
        if (counter.count >= maxRequestsPerWindow) return false
        counter.count += 1
        return true
    }
}
