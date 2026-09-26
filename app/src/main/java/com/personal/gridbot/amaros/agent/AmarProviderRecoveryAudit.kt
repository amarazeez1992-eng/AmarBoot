package com.personal.gridbot.amaros.agent

data class AmarProviderRecoveryAuditRecord(
    val operationId: String,
    val providerId: String?,
    val event: String,
    val attempt: Int,
    val failureClass: AmarProviderFailureClass?,
    val healthState: AmarProviderHealthState,
    val fallbackProviderId: String?,
    val success: Boolean,
    val reason: String,
    val timestampMs: Long
)

class AmarProviderRecoveryAudit(
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val records = mutableListOf<AmarProviderRecoveryAuditRecord>()

    @Synchronized
    fun record(
        operationId: String,
        providerId: String?,
        event: String,
        attempt: Int,
        failureClass: AmarProviderFailureClass?,
        healthState: AmarProviderHealthState,
        fallbackProviderId: String?,
        success: Boolean,
        reason: String
    ) {
        require(operationId.isNotBlank())
        require(event.isNotBlank())
        require(attempt >= 1)
        records += AmarProviderRecoveryAuditRecord(
            operationId, providerId, event, attempt, failureClass,
            healthState, fallbackProviderId, success, reason, clock()
        )
    }

    @Synchronized
    fun records(): List<AmarProviderRecoveryAuditRecord> = records.toList()
}
