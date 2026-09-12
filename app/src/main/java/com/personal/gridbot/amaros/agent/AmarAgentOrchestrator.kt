package com.personal.gridbot.amaros.agent

/**
 * Central pipeline for high-confidence answers.
 * It coordinates research, verification and criticism without granting execution.
 */
class AmarAgentOrchestrator(
    private val planner: AmarAgentPlanner,
    private val researchEngine: AmarResearchEngine,
    private val sourceVerifier: AmarSourceVerifier,
    private val consensusEngine: AmarAgentEvidenceConsensus,
    private val critic: AmarAgentCritic,
    private val verifier: AmarAgentVerifier,
    private val reasoningProvider: AmarReasoningProvider
) {
    suspend fun run(
        request: AmarAgentRequest,
        availableTools: List<AmarAgentTool>,
        budget: AmarAgentBudget = AmarAgentBudget()
    ): AmarAgentRunResult {
        val safeBudget = budget.normalized()
        val session = AmarAgentSession(budget = safeBudget)
        session.record(AmarAgentStage.INTAKE, request.text)

        val plan = planner.plan(request, availableTools)
        session.record(AmarAgentStage.PLAN, plan.steps.joinToString(" -> "))

        val needsResearch = plan.intent == AgentIntent.RESEARCH || plan.intent == AgentIntent.TRADE_ANALYSIS
        val report = if (needsResearch) {
            session.record(AmarAgentStage.RETRIEVE, "multi-source research")
            val sourceLimit = minOf(request.maximumSourceCount, safeBudget.maxSources).coerceAtLeast(1)
            researchEngine.research(
                ResearchRequest(
                    question = request.text,
                    maxSources = sourceLimit,
                    requireIndependentSources = request.requireCrossValidation,
                    targetIndependentSources = minOf(safeBudget.targetIndependentSources, sourceLimit)
                )
            )
        } else null

        val verification = report?.let {
            session.record(AmarAgentStage.VERIFY, "source quality and independence")
            sourceVerifier.verify(it.findings)
        }
        val consensus = report?.let { consensusEngine.summarize(it.findings) }

        val evidenceText = buildString {
            appendLine("Evidence summary:")
            if (report == null) appendLine("No external research required.")
            else {
                appendLine("sources=${report.findings.size}")
                appendLine("confidence=${verification?.confidence ?: 0.0}")
                appendLine("consensus=${consensus?.consensusScore ?: 0.0}")
                appendLine("supporting=${consensus?.supportingSources ?: 0}")
                appendLine("opposing=${consensus?.opposingSources ?: 0}")
                appendLine("unknown=${consensus?.unknownSources ?: 0}")
                report.conflicts.take(20).forEach { appendLine("conflict=$it") }
            }
        }

        session.record(AmarAgentStage.REASON, "reasoning with evidence and uncertainty")
        val enriched = request.copy(text = request.text + "\n\n" + evidenceText)
        val answer = reasoningProvider.respond(
            AmarAgentContext(
                userText = enriched.text,
                tools = availableTools,
                executionAllowed = false,
                brokerAccessAllowed = false,
                requestedSourceCount = request.requestedSourceCount,
                maximumSourceCount = safeBudget.maxSources,
                requireCrossValidation = request.requireCrossValidation,
                requireBacktestWhenApplicable = request.requireBacktestWhenApplicable
            )
        )

        session.record(AmarAgentStage.CHALLENGE, "adversarial critique")
        val critique = critic.review(answer.answer, report?.findings.orEmpty())
        session.record(AmarAgentStage.VALIDATE, critique.recommendation)
        val decisionVerification = verifier.verify(answer.answer, consensus, critique)
        session.record(if (decisionVerification.approved) AmarAgentStage.COMPLETE else AmarAgentStage.BLOCKED, "final gate")

        return AmarAgentRunResult(
            response = if (decisionVerification.approved) answer else answer.copy(
                status = AmarAgentResponse.Status.ERROR,
                answer = "لم يتم اعتماد الإجابة بعد: ${decisionVerification.issues.joinToString(", ")}" 
            ),
            plan = plan,
            research = report,
            sourceVerification = verification,
            consensus = consensus,
            critique = critique,
            finalVerification = decisionVerification,
            sessionEvents = session.events()
        )
    }
}

data class AmarAgentRunResult(
    val response: AmarAgentResponse,
    val plan: AmarAgentPlan,
    val research: ResearchReport?,
    val sourceVerification: AmarSourceVerification?,
    val consensus: AmarConsensusReport?,
    val critique: AmarCritique,
    val finalVerification: AmarDecisionVerification,
    val sessionEvents: List<AmarAgentEvent>
)
