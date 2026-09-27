package com.personal.gridbot.amaros.intelligence.decision

class AmarDecisionIntelligence(
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    enum class Decision { APPROVE, REJECT, DEFER, ESCALATE }

    data class Context(val goal: String, val constraints: List<String>, val riskTolerance: Double, val confidenceScore: Double)
    data class Option(val id: String, val description: String, val expectedBenefit: Double, val expectedRisk: Double, val evidenceSupport: Double, val conflicts: Int)
    data class Evaluation(val optionId: String, val meetsConstraints: Boolean, val riskWithinTolerance: Boolean, val overallScore: Double, val reasons: List<String>)
    data class DecisionReport(val decision: Decision, val selectedOptionId: String?, val context: Context, val options: List<Option>, val evaluations: List<Evaluation>, val confidence: Double, val explanation: String, val decidedAtEpochMs: Long)

    fun buildContext(goal: String, constraints: List<String>, riskTolerance: Double, confidenceScore: Double): Context {
        require(goal.isNotBlank())
        require(riskTolerance in 0.0..1.0)
        require(confidenceScore in 0.0..1.0)
        return Context(goal, constraints.distinct(), riskTolerance, confidenceScore)
    }

    fun generateOptions(candidateDescriptions: List<String>, evidenceSupport: Double): List<Option> =
        candidateDescriptions.distinct().mapIndexed { i, desc ->
            Option("opt-${i + 1}", desc, evidenceSupport.coerceIn(0.0, 1.0), (1.0 - evidenceSupport).coerceIn(0.0, 1.0), evidenceSupport.coerceIn(0.0, 1.0), 0)
        }

    fun decide(context: Context, options: List<Option>): DecisionReport {
        val evaluations = options.map { evaluate(context, it) }
        val viable = evaluations.filter { it.meetsConstraints && it.riskWithinTolerance }
        val best = viable.maxByOrNull { it.overallScore }
        val globalRisk = options.map { it.expectedRisk }.average().takeIf { options.isNotEmpty() } ?: 1.0
        val riskOk = globalRisk <= (1.0 - context.riskTolerance)
        val confidence = (context.confidenceScore * 0.5 + (best?.overallScore ?: 0.0) * 0.5).coerceIn(0.0, 1.0)

        val decision = when {
            options.isEmpty() -> Decision.REJECT
            !riskOk -> Decision.DEFER
            best == null -> Decision.ESCALATE
            confidence < 0.4 -> Decision.DEFER
            confidence >= 0.7 -> Decision.APPROVE
            else -> Decision.DEFER
        }

        return DecisionReport(decision, if (decision == Decision.APPROVE) best?.optionId else null, context, options, evaluations, confidence, "decision=$decision best=${best?.optionId ?: "none"} confidence=${"%.2f".format(confidence)}", clock())
    }

    private fun evaluate(context: Context, option: Option): Evaluation {
        val reasons = mutableListOf<String>()
        val meetsConstraints = context.constraints.isEmpty() || context.constraints.all { c -> option.description.lowercase().contains(c.lowercase()) }
        if (!meetsConstraints) reasons += "constraint_violation"
        val riskWithinTolerance = option.expectedRisk <= (1.0 - context.riskTolerance)
        if (!riskWithinTolerance) reasons += "risk_exceeds_tolerance"
        val score = (option.expectedBenefit * 0.4 + option.evidenceSupport * 0.4 + (1.0 - option.expectedRisk) * 0.2 - option.conflicts * 0.1).coerceIn(0.0, 1.0)
        if (score > 0.7) reasons += "strong_candidate"
        return Evaluation(option.id, meetsConstraints, riskWithinTolerance, score, reasons)
    }
}
