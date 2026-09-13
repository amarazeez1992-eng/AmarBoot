package com.personal.gridbot.amaros.runtime

/** Immutable status event emitted by the Amar runtime coordination layer. */
data class AmarRuntimeStatusEvent(
    val commandId: Long? = null,
    val botNumber: Int? = null,
    val previousState: String? = null,
    val state: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String? = null,
) {
    init {
        require(commandId == null || commandId > 0) { "commandId must be positive when provided" }
        require(botNumber == null || botNumber in 1..10) { "botNumber must be between 1 and 10 when provided" }
        require(state.isNotBlank()) { "state must not be blank" }
        require(previousState == null || previousState.isNotBlank()) { "previousState must not be blank when provided" }
        require(reason == null || reason.isNotBlank()) { "reason must not be blank when provided" }
    }
}
