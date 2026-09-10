package com.personal.gridbot.amaros.security

/** B18 security runtime state. Live trading is structurally unavailable here. */
enum class AmarOperatingMode { DEMO, READ_ONLY, LIVE }

data class AmarSecurityState(
    val mode: AmarOperatingMode = AmarOperatingMode.DEMO,
    val emergencyLock: Boolean = false,
    val liveActivationRequired: Boolean = true,
    val executionAuthorized: Boolean = false,
    val credentialAccessAllowed: Boolean = false
)

class AmarSecurityRuntime(initial: AmarSecurityState = AmarSecurityState()) {
    @Volatile private var state: AmarSecurityState = initial.copy(
        mode = AmarOperatingMode.DEMO,
        executionAuthorized = false,
        credentialAccessAllowed = false
    )

    fun snapshot(): AmarSecurityState = state

    fun lock(reason: String = "manual_lock"): AmarSecurityState {
        state = state.copy(emergencyLock = true, executionAuthorized = false, credentialAccessAllowed = false)
        return state
    }

    fun unlockForDemo(): AmarSecurityState {
        state = state.copy(mode = AmarOperatingMode.DEMO, emergencyLock = false, executionAuthorized = false, credentialAccessAllowed = false)
        return state
    }

    fun authorizeReadOnly(): AmarSecurityState {
        state = state.copy(mode = AmarOperatingMode.READ_ONLY, emergencyLock = false, executionAuthorized = false, credentialAccessAllowed = false)
        return state
    }
}
