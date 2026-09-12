package com.personal.gridbot.amaros.agent

/**
 * Controls how far the agent may act autonomously.
 * Autonomy is graduated and reversible; broker execution remains a separate future capability.
 */
class AmarAutonomyGovernor(private val policy: AmarAutonomyPolicy = AmarAutonomyPolicy()) {
    fun decide(stage: AmarAutonomyStage, evidence: AmarEvidenceState): AmarAutonomyDecision {
        if (!policy.enabled) return AmarAutonomyDecision.BLOCKED
        if (evidence.conflicts && stage >= AmarAutonomyStage.RECOMMEND) return AmarAutonomyDecision.BLOCKED
        if (evidence.confidence < policy.minimumConfidence) return AmarAutonomyDecision.BLOCKED
        return when (stage) {
            AmarAutonomyStage.OBSERVE,
            AmarAutonomyStage.RESEARCH,
            AmarAutonomyStage.SIMULATE -> AmarAutonomyDecision.ALLOWED
            AmarAutonomyStage.RECOMMEND -> AmarAutonomyDecision.ALLOWED
            AmarAutonomyStage.EXECUTE_FUTURE -> AmarAutonomyDecision.REQUIRES_SEPARATE_EXECUTION_GATE
        }
    }
}

data class AmarAutonomyPolicy(
    val enabled: Boolean = true,
    val minimumConfidence: Double = 0.80
)

data class AmarEvidenceState(val confidence: Double, val conflicts: Boolean = false)

enum class AmarAutonomyStage { OBSERVE, RESEARCH, SIMULATE, RECOMMEND, EXECUTE_FUTURE }
enum class AmarAutonomyDecision { ALLOWED, BLOCKED, REQUIRES_SEPARATE_EXECUTION_GATE }
