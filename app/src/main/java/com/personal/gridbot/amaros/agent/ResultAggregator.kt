package com.personal.gridbot.amaros.agent

enum class TaskResultStatus { SUCCESS, PARTIAL, FAILED, BLOCKED }
data class OrchestrationTaskResult(
    val taskId: String,
    val status: TaskResultStatus,
    val value: String?,
    val provenance: List<String> = emptyList(),
    val conflicts: List<String> = emptyList()
)
data class AggregatedOrchestrationResult(
    val status: TaskResultStatus,
    val results: List<OrchestrationTaskResult>,
    val provenance: List<String>,
    val conflicts: List<String>
)
class ResultAggregator {
    fun aggregate(results: List<OrchestrationTaskResult>): AggregatedOrchestrationResult {
        val status = when {
            results.any { it.status == TaskResultStatus.BLOCKED } -> TaskResultStatus.BLOCKED
            results.any { it.status == TaskResultStatus.FAILED } -> TaskResultStatus.FAILED
            results.any { it.status == TaskResultStatus.PARTIAL } -> TaskResultStatus.PARTIAL
            else -> TaskResultStatus.SUCCESS
        }
        return AggregatedOrchestrationResult(status, results.toList(), results.flatMap { it.provenance }.distinct(), results.flatMap { it.conflicts }.distinct())
    }
}
