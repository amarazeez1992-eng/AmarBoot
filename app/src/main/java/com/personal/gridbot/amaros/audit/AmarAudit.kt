package com.personal.gridbot.amaros.audit

import java.util.UUID

/** B19 bounded, structured audit trail. No secrets and no broker credentials are stored. */
data class AmarAuditRecord(
    val id: String = UUID.randomUUID().toString(),
    val epochMs: Long,
    val category: String,
    val action: String,
    val outcome: String,
    val traceId: String? = null,
    val details: Map<String, String> = emptyMap()
)

class AmarAuditLog(private val capacity: Int = 5_000) {
    private val records = ArrayDeque<AmarAuditRecord>()

    @Synchronized fun append(record: AmarAuditRecord) {
        records.addLast(record.copy(details = record.details.filterKeys { !it.contains("password", true) && !it.contains("token", true) && !it.contains("secret", true) }))
        while (records.size > capacity) records.removeFirst()
    }

    @Synchronized fun snapshot(): List<AmarAuditRecord> = records.toList()

    @Synchronized fun clear() { records.clear() }
}
