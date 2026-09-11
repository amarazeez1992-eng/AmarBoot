package com.personal.gridbot.amaros.data

/** Broker-neutral command lifecycle. UI submits intent; only verified runtime state is authoritative. */
enum class AmarCommandType { START, STOP, REBUILD, CLOSE_ALL, CLOSE_BUY, CLOSE_SELL, UPDATE_SETTINGS, EMERGENCY_LOCK, CLEAR_EMERGENCY_LOCK }
enum class AmarCommandStage { CREATED, VALIDATED, ACCEPTED, EXECUTING, ACKNOWLEDGED, VERIFIED, FAILED, REJECTED }

data class AmarRuntimeCommand(
    val requestId: String,
    val botId: String,
    val type: AmarCommandType,
    val createdAtEpochMs: Long,
    val ttlMs: Long = 30_000L,
    val stage: AmarCommandStage = AmarCommandStage.CREATED
) {
    init {
        require(requestId.isNotBlank())
        require(botId.isNotBlank())
        require(createdAtEpochMs > 0L)
        require(ttlMs > 0L)
    }

    fun isExpired(nowEpochMs: Long): Boolean = nowEpochMs - createdAtEpochMs > ttlMs
}

object AmarCommandValidation {
    fun validate(command: AmarRuntimeCommand, nowEpochMs: Long): AmarCommandStage =
        if (command.isExpired(nowEpochMs)) AmarCommandStage.REJECTED else AmarCommandStage.VALIDATED
}
