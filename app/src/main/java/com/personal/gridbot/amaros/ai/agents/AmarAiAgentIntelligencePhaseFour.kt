package com.personal.gridbot.amaros.ai.agents

/** Phase 4: specialist-agent contracts. Read-only; no broker execution. */

enum class AmarAgentRole { PLANNER, RESEARCHER, QUANT_ANALYST, RISK_ANALYST, ADVERSARIAL_CRITIC, VERIFIER, SYNTHESIZER }

data class AmarAgentFinding(
    val agent: AmarAgentRole,
    val claim: String,
    val evidenceIds: List<String>,
    val score: Double,
    val confidence: Double
) {
    init {
        require(claim.isNotBlank())
        require(evidenceIds.distinct().size == evidenceIds.size)
        require(score.isFinite() && score in -1.0..1.0)
        require(confidence.isFinite() && confidence in 0.0..1.0)
    }
}

data class AmarAgentTask(
    val id: String,
    val objective: String,
    val allowedRoles: Set<AmarAgentRole>
) {
    init {
        require(id.isNotBlank() && objective.isNotBlank())
        require(allowedRoles.isNotEmpty())
    }
}

interface AmarSpecialistAgent {
    val role: AmarAgentRole
    fun analyze(task: AmarAgentTask, evidenceIds: List<String>): AmarAgentFinding
}

class AmarDeterministicSpecialistAgent(
    override val role: AmarAgentRole,
    private val score: Double = 0.0,
    private val confidence: Double = 0.5
) : AmarSpecialistAgent {
    override fun analyze(task: AmarAgentTask, evidenceIds: List<String>): AmarAgentFinding {
        require(role in task.allowedRoles)
        return AmarAgentFinding(role, task.objective, evidenceIds.distinct(), score, confidence)
    }
}

class AmarAgentPlanner {
    fun plan(objective: String): List<AmarAgentTask> {
        require(objective.isNotBlank())
        return listOf(
            AmarAgentTask("research", objective, setOf(AmarAgentRole.RESEARCHER)),
            AmarAgentTask("quant", objective, setOf(AmarAgentRole.QUANT_ANALYST)),
            AmarAgentTask("risk", objective, setOf(AmarAgentRole.RISK_ANALYST)),
            AmarAgentTask("critic", objective, setOf(AmarAgentRole.ADVERSARIAL_CRITIC)),
            AmarAgentTask("verify", objective, setOf(AmarAgentRole.VERIFIER))
        )
    }
}

data class AmarConsensus(
    val directionScore: Double,
    val agreement: Double,
    val disagreement: Double,
    val supportingAgents: Int,
    val opposingAgents: Int
) {
    init {
        require(directionScore.isFinite() && directionScore in -1.0..1.0)
        require(agreement in 0.0..1.0 && disagreement in 0.0..1.0)
        require(supportingAgents >= 0 && opposingAgents >= 0)
    }
}

class AmarAgentConsensusEngine {
    fun evaluate(findings: List<AmarAgentFinding>): AmarConsensus {
        if (findings.isEmpty()) return AmarConsensus(0.0, 0.0, 0.0, 0, 0)
        val weighted = findings.map { it.score * it.confidence }
        val direction = weighted.average().coerceIn(-1.0, 1.0)
        val positive = findings.count { it.score > 0.0 }
        val negative = findings.count { it.score < 0.0 }
        val agreement = maxOf(positive, negative).toDouble() / findings.size
        val disagreement = minOf(positive, negative).toDouble() / findings.size
        return AmarConsensus(direction, agreement, disagreement, positive, negative)
    }
}

data class AmarDecisionSynthesis(
    val label: String,
    val score: Double,
    val confidence: Double,
    val reasons: List<String>
) {
    init {
        require(label.isNotBlank())
        require(score.isFinite() && score in -1.0..1.0)
        require(confidence.isFinite() && confidence in 0.0..1.0)
    }
}

class AmarAgentSynthesizer {
    fun synthesize(consensus: AmarConsensus, findings: List<AmarAgentFinding>): AmarDecisionSynthesis {
        val label = when {
            consensus.directionScore > 0.2 -> "شراء"
            consensus.directionScore < -0.2 -> "بيع"
            else -> "محايد"
        }
        val reasons = findings.filter { it.score != 0.0 }.map { it.agent.name }
        return AmarDecisionSynthesis(
            label = label,
            score = consensus.directionScore,
            confidence = (consensus.agreement * (1.0 - consensus.disagreement)).coerceIn(0.0, 1.0),
            reasons = reasons
        )
    }
}
