package com.personal.gridbot.amaros.agent

data class ContextEnvelope(
    val sessionId: String,
    val taskId: String,
    val values: Map<String, String>,
    val provenance: List<String> = emptyList()
) {
    init {
        require(sessionId.isNotBlank())
        require(taskId.isNotBlank())
    }
    fun scoped(childTaskId: String, additions: Map<String, String> = emptyMap()): ContextEnvelope {
        require(childTaskId.isNotBlank())
        return copy(taskId = childTaskId, values = values + additions, provenance = provenance + taskId)
    }
}
