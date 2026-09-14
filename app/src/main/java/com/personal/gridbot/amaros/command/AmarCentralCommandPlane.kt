package com.personal.gridbot.amaros.command

import com.personal.gridbot.amaros.governance.AmarExecutionGovernance

/**
 * Stage 8 application command authority.
 *
 * The command plane routes only decisions already produced by Amar AI. Control-room
 * modules are consumers and cannot grant themselves capabilities or device access.
 * This class deliberately does not execute broker/device operations.
 */
object AmarCentralCommandPlane {
    enum class Route { CONTROL_ROOM, EXECUTION_GATEWAY, DEVICE_GATEWAY, AUDIT }

    enum class DeviceCapability {
        READ_STATUS,
        USER_APPROVED_INPUT,
        EXPORT_ARTIFACT,
        NOTIFY_USER
    }

    enum class Decision { ROUTED, REJECTED, REVOKED, EMERGENCY_LOCKED, STALE }

    data class Permission(
        val principal: String,
        val executionCapabilities: Set<AmarExecutionGovernance.Capability> = emptySet(),
        val deviceCapabilities: Set<DeviceCapability> = emptySet(),
        val expiresAtEpochMs: Long,
        val version: Long = 1L,
        val active: Boolean = true,
    ) {
        init {
            require(principal.isNotBlank())
            require(executionCapabilities.isNotEmpty() || deviceCapabilities.isNotEmpty())
            require(expiresAtEpochMs > 0L)
            require(version > 0L)
        }

        fun permits(capability: AmarExecutionGovernance.Capability, nowEpochMs: Long): Boolean =
            active && expiresAtEpochMs > nowEpochMs && capability in executionCapabilities

        fun permits(capability: DeviceCapability, nowEpochMs: Long): Boolean =
            active && expiresAtEpochMs > nowEpochMs && capability in deviceCapabilities
    }

    data class AgentDecision(
        val decisionId: String,
        val principal: String,
        val capability: AmarExecutionGovernance.Capability,
        val route: Route,
        val command: String,
        val issuedAtEpochMs: Long,
        val expiresAtEpochMs: Long,
        val policyVersion: Long,
        val policyApproved: Boolean,
        val auditRequired: Boolean = true,
    ) {
        init {
            require(decisionId.isNotBlank())
            require(principal.isNotBlank())
            require(command.isNotBlank())
            require(issuedAtEpochMs >= 0L)
            require(expiresAtEpochMs > issuedAtEpochMs)
            require(policyVersion > 0L)
        }
    }

    data class Receipt(
        val decisionId: String,
        val decision: Decision,
        val route: Route?,
        val message: String,
        val sequence: Long,
    )

    private val lock = Any()
    private val permissions = mutableMapOf<String, Permission>()
    private val receipts = mutableMapOf<String, Receipt>()
    private var emergencyLock = false
    private var sequence = 0L

    fun grant(permission: Permission, nowEpochMs: Long): Boolean = synchronized(lock) {
        if (permission.expiresAtEpochMs <= nowEpochMs) return@synchronized false
        permissions[permission.principal] = permission
        true
    }

    fun revoke(principal: String): Boolean = synchronized(lock) {
        permissions.remove(principal) != null
    }

    fun setEmergencyLock(enabled: Boolean): Unit = synchronized(lock) {
        emergencyLock = enabled
    }

    fun isEmergencyLocked(): Boolean = synchronized(lock) { emergencyLock }

    /** Routes an Agent decision. Repeated decision IDs are idempotent. */
    fun route(decision: AgentDecision, nowEpochMs: Long): Receipt = synchronized(lock) {
        receipts[decision.decisionId]?.let { return@synchronized it }

        val permission = permissions[decision.principal]
        val result = when {
            emergencyLock -> Decision.EMERGENCY_LOCKED to "central emergency lock active"
            !decision.policyApproved -> Decision.REJECTED to "Agent policy decision not approved"
            nowEpochMs < decision.issuedAtEpochMs || nowEpochMs >= decision.expiresAtEpochMs ->
                Decision.STALE to "Agent decision outside validity window"
            permission?.permits(decision.capability, nowEpochMs) != true ->
                Decision.REVOKED to "principal lacks active delegated capability"
            decision.route == Route.DEVICE_GATEWAY && decision.capability == AmarExecutionGovernance.Capability.SUBMIT_EXECUTION ->
                Decision.REJECTED to "execution cannot bypass the execution governance boundary"
            else -> Decision.ROUTED to "Agent decision routed to subordinate gateway"
        }
        val receipt = Receipt(
            decisionId = decision.decisionId,
            decision = result.first,
            route = decision.route.takeIf { result.first == Decision.ROUTED },
            message = result.second,
            sequence = ++sequence,
        )
        receipts[decision.decisionId] = receipt
        receipt
    }

    /** Device access is separately bounded and cannot be inferred from an Agent route. */
    fun authorizeDevice(principal: String, capability: DeviceCapability, nowEpochMs: Long): Boolean =
        synchronized(lock) {
            !emergencyLock && permissions[principal]?.permits(capability, nowEpochMs) == true
        }

    fun receipt(decisionId: String): Receipt? = synchronized(lock) { receipts[decisionId] }

    /** Test-only state reset; production callers should use explicit revocation/lock. */
    fun clearForTests() = synchronized(lock) {
        permissions.clear()
        receipts.clear()
        emergencyLock = false
        sequence = 0L
    }
}
