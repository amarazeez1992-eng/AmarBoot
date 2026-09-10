package com.personal.gridbot.amaros.audit

import java.security.MessageDigest
import java.util.UUID

/** B19 bounded, structured and tamper-evident audit trail. Secrets are never stored. */
data class AmarAuditRecord(
    val id: String = UUID.randomUUID().toString(),
    val epochMs: Long,
    val category: String,
    val action: String,
    val outcome: String,
    val traceId: String? = null,
    val details: Map<String, String> = emptyMap(),
    val previousHash: String = "GENESIS",
    val integrityHash: String = ""
)

class AmarAuditLog(private val capacity: Int = 5_000) {
    private val records = ArrayDeque<AmarAuditRecord>()
    init { require(capacity > 0) }

    @Synchronized fun append(record: AmarAuditRecord) {
        val safeDetails = record.details.filterKeys {
            !it.contains("password", true) && !it.contains("token", true) && !it.contains("secret", true) && !it.contains("credential", true)
        }
        val previous = records.lastOrNull()?.integrityHash?.ifBlank { "GENESIS" } ?: "GENESIS"
        val safe = record.copy(details = safeDetails, previousHash = previous)
        val hash = sha256("${safe.id}|${safe.epochMs}|${safe.category}|${safe.action}|${safe.outcome}|${safe.traceId}|${safe.details}|${safe.previousHash}")
        records.addLast(safe.copy(integrityHash = hash))
        while (records.size > capacity) records.removeFirst()
        rebaseAfterEviction()
    }

    @Synchronized fun snapshot(): List<AmarAuditRecord> = records.toList()

    @Synchronized fun verifyIntegrity(): Boolean {
        var previous = "GENESIS"
        for (record in records) {
            if (record.previousHash != previous) return false
            val expected = sha256("${record.id}|${record.epochMs}|${record.category}|${record.action}|${record.outcome}|${record.traceId}|${record.details}|${record.previousHash}")
            if (record.integrityHash != expected) return false
            previous = record.integrityHash
        }
        return true
    }

    @Synchronized fun clear() { records.clear() }

    private fun rebaseAfterEviction() {
        val first = records.firstOrNull() ?: return
        if (first.previousHash == "GENESIS") return
        val rebased = first.copy(previousHash = "GENESIS")
        val hash = sha256("${rebased.id}|${rebased.epochMs}|${rebased.category}|${rebased.action}|${rebased.outcome}|${rebased.traceId}|${rebased.details}|${rebased.previousHash}")
        records.removeFirst()
        records.addFirst(rebased.copy(integrityHash = hash))
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
