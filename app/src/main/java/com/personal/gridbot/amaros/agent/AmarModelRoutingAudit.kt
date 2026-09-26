package com.personal.gridbot.amaros.agent

data class AmarModelRoutingAuditRecord(
    val level: Int,
    val authority: String,
    val complexity: AmarModelComplexity,
    val selectedProvider: String?,
    val decisionState: String,
    val reason: String
)

class AmarModelRoutingAudit {
    private val records = mutableListOf<AmarModelRoutingAuditRecord>()

    @Synchronized
    fun record(
        level: Int,
        authority: String,
        complexity: AmarModelComplexity,
        selectedProvider: String?,
        decisionState: String,
        reason: String
    ) {
        records += AmarModelRoutingAuditRecord(
            level = level,
            authority = authority,
            complexity = complexity,
            selectedProvider = selectedProvider,
            decisionState = decisionState,
            reason = reason
        )
    }

    @Synchronized
    fun records(): List<AmarModelRoutingAuditRecord> = records.toList()
}
