package com.personal.gridbot.amaros.bots

import kotlin.math.abs

/** Stable runtime identity for the approved BOT 1. */
data class AmarBotIdentity(
    val botId: String = "BOT_1",
    val magic: Long = 20260908L,
    val version: String = "2.00",
    val strategyId: String = "STRATEGY_01"
)

enum class AmarBotRuntimeState {
    OFF, STARTING, RUNNING, STOPPING, REBUILDING, CLOSING, ERROR, EMERGENCY_LOCK
}

enum class AmarBotSyncState { MATCHED, DRIFT, UNKNOWN, ERROR }

data class AmarBot1RuntimeConfig(
    val lot: Double = 0.01,
    val gridStep: Double = 30.0,
    val maxOrders: Int = 10,
    val multiplier: Double = 2.0,
    val basketTp: Double = 50.0,
    val basketSl: Double = -30.0,
    val trailing: Double = 0.0,
    val buyEnabled: Boolean = true,
    val sellEnabled: Boolean = true
) {
    init {
        require(lot.isFinite() && lot > 0.0)
        require(gridStep.isFinite() && gridStep > 0.0)
        require(maxOrders > 0)
        require(multiplier.isFinite() && multiplier > 0.0)
        require(basketTp.isFinite())
        require(basketSl.isFinite())
        require(trailing.isFinite() && trailing >= 0.0)
    }
}

data class AmarBot1DesiredState(
    val identity: AmarBotIdentity = AmarBotIdentity(),
    val runtimeState: AmarBotRuntimeState = AmarBotRuntimeState.OFF,
    val config: AmarBot1RuntimeConfig = AmarBot1RuntimeConfig()
)

data class AmarBot1ActualState(
    val identity: AmarBotIdentity? = null,
    val runtimeState: AmarBotRuntimeState? = null,
    val config: AmarBot1RuntimeConfig? = null,
    val openPositions: Int = 0,
    val pendingOrders: Int = 0,
    val equity: Double? = null,
    val balance: Double? = null,
    val lastError: String? = null
)

fun reconcileBot1(desired: AmarBot1DesiredState, actual: AmarBot1ActualState): AmarBotSyncState {
    if (actual.identity == null || actual.config == null || actual.runtimeState == null) return AmarBotSyncState.UNKNOWN
    if (desired.identity.botId != actual.identity.botId ||
        desired.identity.magic != actual.identity.magic ||
        desired.identity.version != actual.identity.version ||
        desired.identity.strategyId != actual.identity.strategyId) return AmarBotSyncState.ERROR
    if (desired.runtimeState != actual.runtimeState) return AmarBotSyncState.DRIFT
    val d = desired.config
    val a = actual.config
    val same = abs(d.lot - a.lot) < 1e-9 &&
        abs(d.gridStep - a.gridStep) < 1e-9 &&
        d.maxOrders == a.maxOrders &&
        abs(d.multiplier - a.multiplier) < 1e-9 &&
        abs(d.basketTp - a.basketTp) < 1e-9 &&
        abs(d.basketSl - a.basketSl) < 1e-9 &&
        abs(d.trailing - a.trailing) < 1e-9 &&
        d.buyEnabled == a.buyEnabled && d.sellEnabled == a.sellEnabled
    return if (same) AmarBotSyncState.MATCHED else AmarBotSyncState.DRIFT
}

/** B34 execution policy. Every live gate must be explicitly true; any failure blocks. */
enum class AmarBotExecutionDecision { ALLOW, BLOCK }

data class AmarBotExecutionPolicyResult(
    val decision: AmarBotExecutionDecision,
    val reasons: List<String>
) {
    val allowed: Boolean get() = decision == AmarBotExecutionDecision.ALLOW
}

object AmarBot1ExecutionPolicy {
    fun evaluate(
        userPermission: Boolean,
        accountPermission: Boolean,
        botPermission: Boolean,
        strategyPermission: Boolean,
        symbolPermission: Boolean,
        riskPermission: Boolean,
        marketCondition: Boolean,
        connectorHealthy: Boolean,
        commandValid: Boolean,
        idempotencyValid: Boolean,
        emergencyLock: Boolean
    ): AmarBotExecutionPolicyResult {
        val gates = listOf(
            "USER_PERMISSION" to userPermission,
            "ACCOUNT_PERMISSION" to accountPermission,
            "BOT_PERMISSION" to botPermission,
            "STRATEGY_PERMISSION" to strategyPermission,
            "SYMBOL_PERMISSION" to symbolPermission,
            "RISK_PERMISSION" to riskPermission,
            "MARKET_CONDITION" to marketCondition,
            "CONNECTOR_HEALTH" to connectorHealthy,
            "COMMAND_VALID" to commandValid,
            "IDEMPOTENCY_VALID" to idempotencyValid,
        )
        val reasons = gates.filterNot { it.second }.map { it.first }
        val allValid = reasons.isEmpty() && !emergencyLock
        return AmarBotExecutionPolicyResult(
            if (allValid) AmarBotExecutionDecision.ALLOW else AmarBotExecutionDecision.BLOCK,
            if (emergencyLock) listOf("EMERGENCY_LOCK") + reasons else reasons
        )
    }
}

/** Pure reconciliation service: UI cannot manufacture a MATCHED state. */
class AmarBot1ReconciliationEngine {
    fun evaluate(desired: AmarBot1DesiredState, actual: AmarBot1ActualState): AmarBotSyncState =
        reconcileBot1(desired, actual)
}

/** B36 advisory proposal. It is data only and has no execution capability. */
data class AmarBotSupervisorProposal(
    val proposalId: String,
    val botId: String = "BOT_1",
    val title: String,
    val rationale: String,
    val confidence: Double,
    val requiresSimulation: Boolean = true,
    val requiresUserApproval: Boolean = true,
    val executable: Boolean = false
) {
    init {
        require(proposalId.isNotBlank())
        require(botId.isNotBlank())
        require(title.isNotBlank())
        require(rationale.isNotBlank())
        require(confidence in 0.0..1.0)
        require(!executable) { "Supervisor proposals are advisory-only" }
    }
}
