package com.personal.gridbot.amaros.agent

interface TaskExecutor {
    suspend fun execute(task: AmarTaskUnit, context: ContextEnvelope): TaskExecutionResult
}

data class TaskExecutionResult(
    val status: TaskResultStatus,
    val value: Any?,
    val provenance: List<String> = emptyList(),
    val conflicts: List<String> = emptyList()
) {
    val failed: Boolean
        get() = status == TaskResultStatus.FAILED
}
