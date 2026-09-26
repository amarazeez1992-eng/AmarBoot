package com.personal.gridbot.amaros.agent

enum class AmarModelComplexity {
    LOW, MEDIUM, HIGH
}

data class AmarModelComplexityContext(
    val text: String,
    val contextTokens: Int = 0,
    val evidenceCount: Int = 0,
    val constraintCount: Int = 0
)

class AmarModelComplexityEstimator {
    fun estimate(task: AmarModelTask, context: AmarModelComplexityContext): AmarModelComplexity =
        when {
            task == AmarModelTask.UNKNOWN -> AmarModelComplexity.HIGH
            context.constraintCount >= 4 || context.evidenceCount >= 8 -> AmarModelComplexity.HIGH
            task == AmarModelTask.MULTI_FACTOR_ANALYSIS -> AmarModelComplexity.HIGH
            context.contextTokens >= 1800 || context.text.length >= 7200 -> AmarModelComplexity.MEDIUM
            task == AmarModelTask.TEXT_GENERATION && context.text.length >= 2400 -> AmarModelComplexity.MEDIUM
            else -> AmarModelComplexity.LOW
        }
}
