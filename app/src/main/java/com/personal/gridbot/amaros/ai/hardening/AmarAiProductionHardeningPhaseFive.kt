package com.personal.gridbot.amaros.ai.hardening

/** Production safety contracts. AI-only; no broker execution. */
data class AmarEvidenceCheck(val citation: String, val supported: Boolean, val score: Double) {
    init { require(citation.isNotBlank()); require(score.isFinite() && score in 0.0..1.0) }
}

object AmarEvidenceVerifier {
    fun verify(citations: Set<String>, checks: List<AmarEvidenceCheck>): Boolean =
        checks.isNotEmpty() && checks.all { it.citation in citations && it.supported && it.score > 0.0 }
}

data class AmarPromptBoundaryResult(val allowed: Boolean, val reasons: List<String>)

object AmarPromptBoundary {
    private val patterns = listOf("ignore previous instructions", "system prompt", "developer message", "jailbreak")
    fun inspect(input: String): AmarPromptBoundaryResult {
        val normalized = input.lowercase()
        val hits = patterns.filter { it in normalized }
        return AmarPromptBoundaryResult(hits.isEmpty(), hits)
    }
}

data class AmarResourcePolicy(val maxTokens: Int, val maxContextItems: Int, val timeoutMs: Long) {
    init { require(maxTokens > 0); require(maxContextItems > 0); require(timeoutMs > 0) }
}

data class AmarSandboxRequest(val resource: String, val networkAllowed: Boolean, val writeAllowed: Boolean)

object AmarSandboxPolicy {
    fun allow(request: AmarSandboxRequest): Boolean =
        request.resource.isNotBlank() && !request.networkAllowed && !request.writeAllowed
}

data class AmarEncryptedRecord(val keyId: String, val ciphertext: ByteArray) {
    init { require(keyId.isNotBlank()); require(ciphertext.isNotEmpty()) }
}

interface AmarSecureStore {
    fun put(key: String, plaintext: ByteArray): AmarEncryptedRecord
    fun get(key: String): ByteArray?
}

enum class AmarRecoveryState { HEALTHY, DEGRADED, RECOVERING, FAILED }

class AmarCrashRecovery {
    var state: AmarRecoveryState = AmarRecoveryState.HEALTHY
        private set
    fun markDegraded() { state = AmarRecoveryState.DEGRADED }
    fun beginRecovery() { require(state != AmarRecoveryState.FAILED); state = AmarRecoveryState.RECOVERING }
    fun recover() { state = AmarRecoveryState.HEALTHY }
    fun fail() { state = AmarRecoveryState.FAILED }
}

data class AmarAuditEvent(val sequence: Long, val type: String, val digest: String) {
    init { require(sequence >= 0); require(type.isNotBlank()); require(digest.isNotBlank()) }
}

class AmarAuditTrail {
    private val events = mutableListOf<AmarAuditEvent>()
    fun append(event: AmarAuditEvent) {
        require(events.lastOrNull()?.sequence?.let { event.sequence > it } ?: true)
        events += event
    }
    fun snapshot(): List<AmarAuditEvent> = events.toList()
}
