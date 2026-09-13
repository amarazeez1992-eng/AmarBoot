package com.personal.gridbot.amaros.runtime

import com.personal.gridbot.bridge.AmarBridgeContract

/**
 * In-memory runtime state coordinator for the Android layer.
 * It validates state transitions without executing broker operations.
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

    @Synchronized
    fun setCommandState(commandId: Long, state: String) {
        require(commandId > 0) { "Invalid command id" }
        require(state.isNotBlank()) { "Command state is blank" }
        commandStates[commandId] = state.trim()
    }

    @Synchronized
    fun canTransitionCommand(commandId: Long, nextState: String): Boolean {
        require(commandId > 0) { "Invalid command id" }
        require(nextState.isNotBlank()) { "Command state is blank" }
        val current = commandStates[commandId] ?: return true
        if (AmarBridgeContract.isTerminal(current)) return false
        return !AmarBridgeContract.isTerminal(nextState) || current == AmarBridgeContract.PENDING_MT5
    }

    @Synchronized
    fun transitionCommand(commandId: Long, nextState: String): Boolean {
        if (!canTransitionCommand(commandId, nextState)) return false
        commandStates[commandId] = nextState.trim()
        return true
    }
}
