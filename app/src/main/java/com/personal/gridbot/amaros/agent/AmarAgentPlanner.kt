package com.personal.gridbot.amaros.agent

/** Deterministic planning contract: typed task planning produces intent, never broker commands. */
class AmarAgentPlanner(
    private val understanding: AmarIntentUnderstanding = AmarIntentUnderstanding()
) {
    fun plan(request: AmarAgentRequest, availableTools: List<AmarAgentTool>): AmarAgentPlan {
        val analysis = understanding.understand(request.text)
        val intent = analysis.intent
        val tools = availableTools
            .filter { tool ->
                when (intent) {
                    AgentIntent.RESEARCH -> tool.scope == AmarToolScope.RESEARCH || tool.scope == AmarToolScope.READ_ONLY
                    AgentIntent.TRADE_ANALYSIS -> tool.scope == AmarToolScope.RESEARCH ||
                        tool.scope == AmarToolScope.READ_ONLY || tool.scope == AmarToolScope.SIMULATION
                    AgentIntent.STRATEGY_DESIGN -> tool.scope == AmarToolScope.READ_ONLY ||
                        tool.scope == AmarToolScope.STRATEGY_WRITE || tool.scope == AmarToolScope.SIMULATION
                    AgentIntent.GENERAL,
                    AgentIntent.SMALL_TALK,
                    AgentIntent.SYSTEM_IDENTITY,
                    AgentIntent.SYSTEM_TIME,
                    AgentIntent.SYSTEM_DATE -> tool.scope == AmarToolScope.READ_ONLY
                }
            }
            .map { it.id }
            .distinct()

        val ambiguityStep = if (analysis.ambiguous) {
            "resolve_intent_ambiguity_without_guessing"
        } else {
            "intent_unambiguous"
        }

        val tasks = buildTypedTasks(intent, analysis, request)
        return AmarAgentPlan(
            intent = intent,
            tasks = tasks,
            steps = listOf(
                "normalize_request",
                "understand_intent:" + analysis.questionForm + ":confidence=" + String.format(java.util.Locale.US, "%.2f", analysis.confidence),
                "entities:" + analysis.entities.joinToString(",").ifBlank { "none" },
                ambiguityStep,
                if (intent == AgentIntent.RESEARCH || intent == AgentIntent.TRADE_ANALYSIS) "retrieve_and_verify_evidence" else "inspect_local_context",
                "reason_with_constraints",
                "challenge_assumptions",
                if (intent == AgentIntent.TRADE_ANALYSIS && request.requireBacktestWhenApplicable) "require_simulation_and_risk_check" else "produce_answer",
                "audit_output"
            ),
            requiredTools = tools,
            stopConditions = listOf(
                "missing_evidence",
                "policy_denied",
                "unsafe_execution_request",
                "budget_exhausted"
            )
        )
    }

    private fun buildTypedTasks(
        intent: AgentIntent,
        analysis: AmarIntentAnalysis,
        request: AmarAgentRequest
    ): List<AmarTaskUnit> {
        val tasks = mutableListOf(
            AmarTaskUnit("normalize_request", AmarTaskKind.NORMALIZE, "request", emptyList()),
            AmarTaskUnit(
                "understand_intent",
                AmarTaskKind.UNDERSTAND,
                analysis.questionForm,
                listOf("normalize_request")
            ),
            AmarTaskUnit(
                "extract_entities",
                AmarTaskKind.CONTEXT,
                analysis.entities.joinToString(",").ifBlank { "none" },
                listOf("understand_intent")
            )
        )
        tasks += if (analysis.ambiguous) {
            AmarTaskUnit(
                "resolve_ambiguity",
                AmarTaskKind.CONSTRAINT,
                "resolve_without_guessing",
                listOf("understand_intent")
            )
        } else {
            AmarTaskUnit(
                "confirm_intent",
                AmarTaskKind.CONSTRAINT,
                "intent_unambiguous",
                listOf("understand_intent")
            )
        }
        tasks += if (intent == AgentIntent.RESEARCH || intent == AgentIntent.TRADE_ANALYSIS) {
            AmarTaskUnit(
                "retrieve_and_verify_evidence",
                AmarTaskKind.EVIDENCE,
                "verified_evidence",
                listOf(tasks.last().id)
            )
        } else {
            AmarTaskUnit(
                "inspect_local_context",
                AmarTaskKind.CONTEXT,
                "local_context",
                listOf(tasks.last().id)
            )
        }
        tasks += AmarTaskUnit(
            "reason_with_constraints",
            AmarTaskKind.REASON,
            "bounded_reasoning",
            listOf(tasks.last().id)
        )
        tasks += AmarTaskUnit(
            "challenge_assumptions",
            AmarTaskKind.CHALLENGE,
            "adversarial_review",
            listOf(tasks.last().id)
        )
        tasks += if (intent == AgentIntent.TRADE_ANALYSIS && request.requireBacktestWhenApplicable) {
            AmarTaskUnit(
                "require_simulation_and_risk_check",
                AmarTaskKind.VALIDATE,
                "simulation_and_risk_check",
                listOf(tasks.last().id)
            )
        } else {
            AmarTaskUnit(
                "produce_answer",
                AmarTaskKind.RESPONSE,
                "answer",
                listOf(tasks.last().id)
            )
        }
        tasks += AmarTaskUnit(
            "audit_output",
            AmarTaskKind.AUDIT,
            "audited_output",
            listOf(tasks.last().id)
        )
        return tasks
    }
}

data class AmarAgentPlan(
    val intent: AgentIntent,
    val tasks: List<AmarTaskUnit>,
    val steps: List<String>,
    val requiredTools: List<String>,
    val stopConditions: List<String>
)

data class AmarTaskUnit(
    val id: String,
    val kind: AmarTaskKind,
    val outputContract: String,
    val dependencies: List<String>
)

enum class AmarTaskKind {
    NORMALIZE, UNDERSTAND, CONTEXT, CONSTRAINT, EVIDENCE, REASON, CHALLENGE, VALIDATE, RESPONSE, AUDIT
}

enum class AgentIntent { GENERAL, SMALL_TALK, RESEARCH, TRADE_ANALYSIS, STRATEGY_DESIGN, SYSTEM_IDENTITY, SYSTEM_TIME, SYSTEM_DATE }
