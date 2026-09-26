package com.personal.gridbot.amaros.agent

enum class OrchestrationState { INTAKE, PLANNED, RUNNING, WAITING, RECOVERING, COMPLETED, FAILED, BLOCKED }

data class OrchestrationStateSnapshot(
    val state: OrchestrationState = OrchestrationState.INTAKE,
    val completedTasks: Set<String> = emptySet(),
    val blockedTasks: Set<String> = emptySet(),
    val failureReason: String? = null
)

class OrchestrationStateReducer {
    fun transition(
        current: OrchestrationStateSnapshot,
        next: OrchestrationState,
        taskId: String? = null,
        reason: String? = null
    ): OrchestrationStateSnapshot {
        require(isAllowed(current.state, next)) {
            "Invalid orchestration transition: " + current.state + " -> " + next
        }
        val completed = if (next == OrchestrationState.COMPLETED && taskId != null) {
            current.completedTasks + taskId
        } else current.completedTasks
        val blocked = if (next == OrchestrationState.BLOCKED && taskId != null) {
            current.blockedTasks + taskId
        } else current.blockedTasks
        return current.copy(
            state = next,
            completedTasks = completed,
            blockedTasks = blocked,
            failureReason = reason
        )
    }

    fun markCompleted(
        current: OrchestrationStateSnapshot,
        taskId: String
    ): OrchestrationStateSnapshot {
        require(taskId.isNotBlank())
        require(current.state == OrchestrationState.RUNNING) {
            "Tasks can only complete while orchestration is RUNNING"
        }
        require(taskId !in current.blockedTasks) {
            "Blocked task cannot be marked completed: " + taskId
        }
        return current.copy(completedTasks = current.completedTasks + taskId, failureReason = null)
    }

    private fun isAllowed(from: OrchestrationState, to: OrchestrationState): Boolean = when (from) {
        OrchestrationState.INTAKE -> to == OrchestrationState.PLANNED || to == OrchestrationState.BLOCKED
        OrchestrationState.PLANNED -> to == OrchestrationState.RUNNING || to == OrchestrationState.BLOCKED
        OrchestrationState.RUNNING -> to == OrchestrationState.WAITING ||
            to == OrchestrationState.RECOVERING ||
            to == OrchestrationState.COMPLETED ||
            to == OrchestrationState.FAILED ||
            to == OrchestrationState.BLOCKED
        OrchestrationState.WAITING -> to == OrchestrationState.RUNNING || to == OrchestrationState.BLOCKED
        OrchestrationState.RECOVERING -> to == OrchestrationState.RUNNING ||
            to == OrchestrationState.FAILED ||
            to == OrchestrationState.BLOCKED
        OrchestrationState.COMPLETED, OrchestrationState.FAILED, OrchestrationState.BLOCKED -> false
    }
}
