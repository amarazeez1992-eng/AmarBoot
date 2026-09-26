package com.personal.gridbot.amaros.agent

data class ContextEnvelope(
    val sessionId: String,
    val taskId: String,
    val values: Map<String, String>,
    val provenance: List<String> = emptyList(),
    val artifacts: Map<String, Any?> = emptyMap()
) {
    init {
        require(sessionId.isNotBlank())
        require(taskId.isNotBlank())
    }

    fun scoped(
        childTaskId: String,
        additions: Map<String, String> = emptyMap()
    ): ContextEnvelope {
        require(childTaskId.isNotBlank())
        return copy(
            taskId = childTaskId,
            values = values + additions,
            provenance = provenance + taskId
        )
    }

    fun withArtifact(key: String, value: Any?): ContextEnvelope {
        require(key.isNotBlank())
        return copy(artifacts = artifacts + (key to value))
    }

    fun artifact(key: String): Any? = artifacts[key]
}
