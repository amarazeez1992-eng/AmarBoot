package com.personal.gridbot.amaros.runtime

import com.personal.gridbot.bridge.AmarBridgeContract

/**
 * In-memory runtime state coordinator for the Android layer.
 *
 * This coordinator mirrors the durable command lifecycle and cannot be used to bypass it.
 * A command may be initialized only as PENDING_MT5; every later state change must use the
 * governed transition graph.
 */
class AmarRuntimeStateManager {
    private val botStates = mutableMapOf<Int, String>()
    private val commandStates = mutableMapOf<Long, String>()

    @Synchronized
    fun botState(botNumber: Int): String? {
        require(botNumber in 1..10) { "Invalid bot number" }
        return botStates[botNumber]
    }

    @Synchronized
    fun setBotState(botNumber: Int, state: String) {
        require(botNumber in 1..10) { "Invalid bot number" }
        require(state.isNotBlank()) { "Bot state is blank" }
        botStates[botNumber] = state.trim()
    }

    @Synchronized
    fun commandState(commandId: Long): String? {
        require(commandId > 0) { "Invalid command id" }
        return commandStates[commandId]
    }

    /**
     * Initialization boundary only. Existing commands cannot be overwritten through this API,
     * and a new command can enter the lifecycle only at PENDING_MT5.
     */
    @Synchronized
    fun setCommandState(commandId: Long, state: String) {
        require(commandId > 0) { "Invalid command id" }
        val normalized = state.trim()
        require(normalized.isNotEmpty()) { "Command state is blank" }
        require(normalized == AmarBridgeContract.PENDING_MT5) {
            "Command initialization must start at PENDING_MT5"
        }
        require(commandStates.putIfAbsent(commandId, normalized) == null) {
            "Command already initialized; use transitionCommand"
        }
    }

    @Synchronized
    fun canTransitionCommand(commandId: Long, nextState: String): Boolean {
        require(commandId > 0) { "Invalid command id" }
        val normalized = nextState.trim()
        require(normalized.isNotEmpty()) { "Command state is blank" }
        require(AmarBridgeContract.isKnown(normalized)) { "Unknown command state" }
        val current = commandStates[commandId] ?: return normalized == AmarBridgeContract.PENDING_MT5
        if (AmarBridgeContract.isTerminal(current)) return false
        return allowedNextStates(current).contains(normalized)
    }

    @Synchronized
    fun transitionCommand(commandId: Long, nextState: String): Boolean {
        if (!canTransitionCommand(commandId, nextState)) return false
        commandStates[commandId] = nextState.trim()
        return true
    }

    private fun allowedNextStates(current: String): Set<String> = when (current) {
        AmarBridgeContract.PENDING_MT5 -> setOf(
            AmarBridgeContract.ACKNOWLEDGED,
            AmarBridgeContract.REJECTED,
            AmarBridgeContract.FAILED,
            AmarBridgeContract.STALE,
        )
        AmarBridgeContract.ACKNOWLEDGED -> setOf(
            AmarBridgeContract.EXECUTED,
            AmarBridgeContract.REJECTED,
            AmarBridgeContract.FAILED,
            AmarBridgeContract.STALE,
        )
        AmarBridgeContract.EXECUTED -> setOf(
            AmarBridgeContract.VERIFIED,
            AmarBridgeContract.FAILED,
            AmarBridgeContract.STALE,
        )
        else -> emptySet()
    }
}
