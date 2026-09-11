package com.personal.gridbot.amaros.bots

import java.util.UUID

enum class AmarBot1CommandStatus { CREATED, VALIDATED, ACCEPTED, EXECUTING, ACKNOWLEDGED, VERIFIED, FAILED, REJECTED }

data class AmarBot1CommandEnvelope(
    val commandId: String = UUID.randomUUID().toString(),
    val version: Int = 1,
    val issuedAtMs: Long,
    val expiresAtMs: Long,
    val idempotencyKey: String,
    val type: AmarBot1CommandType,
    val expectedBotId: String = "BOT_1",
    val expectedMagic: Long = 20260908L
) {
    init {
        require(commandId.isNotBlank())
        require(version > 0)
        require(expiresAtMs > issuedAtMs)
        require(idempotencyKey.isNotBlank())
        require(expectedBotId.isNotBlank())
        require(expectedMagic >= 0)
    }
}

data class AmarBot1CommandLifecycle(
    val envelope: AmarBot1CommandEnvelope,
    val status: AmarBot1CommandStatus = AmarBot1CommandStatus.CREATED,
    val acceptedAtMs: Long? = null,
    val acknowledgedAtMs: Long? = null,
    val verifiedAtMs: Long? = null,
    val resultCode: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val actualState: AmarBot1ActualState? = null
)

enum class AmarBot1LifecycleError {
    EXPIRED, REPLAY, WRONG_SCOPE, INVALID_TRANSITION, EXECUTION_FAILED, VERIFICATION_FAILED
}

object AmarBot1CommandLifecycleRules {
    fun isTerminal(status: AmarBot1CommandStatus): Boolean = status in setOf(
        AmarBot1CommandStatus.VERIFIED,
        AmarBot1CommandStatus.FAILED,
        AmarBot1CommandStatus.REJECTED
    )

    fun canMove(from: AmarBot1CommandStatus, to: AmarBot1CommandStatus): Boolean = when (from) {
        AmarBot1CommandStatus.CREATED -> to == AmarBot1CommandStatus.VALIDATED || to == AmarBot1CommandStatus.REJECTED
        AmarBot1CommandStatus.VALIDATED -> to == AmarBot1CommandStatus.ACCEPTED || to == AmarBot1CommandStatus.REJECTED
        AmarBot1CommandStatus.ACCEPTED -> to == AmarBot1CommandStatus.EXECUTING || to == AmarBot1CommandStatus.FAILED
        AmarBot1CommandStatus.EXECUTING -> to == AmarBot1CommandStatus.ACKNOWLEDGED || to == AmarBot1CommandStatus.FAILED
        AmarBot1CommandStatus.ACKNOWLEDGED -> to == AmarBot1CommandStatus.VERIFIED || to == AmarBot1CommandStatus.FAILED
        AmarBot1CommandStatus.VERIFIED, AmarBot1CommandStatus.FAILED, AmarBot1CommandStatus.REJECTED -> false
    }

    fun transition(
        lifecycle: AmarBot1CommandLifecycle,
        next: AmarBot1CommandStatus,
        nowMs: Long,
        actual: AmarBot1ActualState? = lifecycle.actualState,
        resultCode: String? = lifecycle.resultCode,
        errorCode: String? = lifecycle.errorCode,
        errorMessage: String? = lifecycle.errorMessage,
    ): AmarBot1CommandLifecycle {
        require(canMove(lifecycle.status, next)) { "Invalid BOT1 lifecycle transition: ${lifecycle.status} -> $next" }
        require(nowMs >= lifecycle.envelope.issuedAtMs) { "Transition time precedes command issue time" }
        return lifecycle.copy(
            status = next,
            acceptedAtMs = if (next == AmarBot1CommandStatus.ACCEPTED) nowMs else lifecycle.acceptedAtMs,
            acknowledgedAtMs = if (next == AmarBot1CommandStatus.ACKNOWLEDGED) nowMs else lifecycle.acknowledgedAtMs,
            verifiedAtMs = if (next == AmarBot1CommandStatus.VERIFIED) nowMs else lifecycle.verifiedAtMs,
            actualState = actual,
            resultCode = resultCode,
            errorCode = errorCode,
            errorMessage = errorMessage,
        )
    }
}
