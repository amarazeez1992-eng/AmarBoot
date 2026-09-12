package com.personal.gridbot.amaros.agent

/** Central pipeline for high-confidence answers without execution authority. */
class AmarAgentOrchestrator(
    private val planner: AmarAgentPlanner,
    private val researchEngine: AmarResearchEngine,
    private val sourceVerifier: AmarSourceVerifier,
    private val consensusEngine: AmarAgentEvidenceConsensus,
    private val critic: AmarAgentCritic,
    private val verifier: AmarAgentVerifier,
    private val reasoningProvider: AmarReasoningProvider,
    private val hierarchy: AmarAgentHierarchy = AmarAgentHierarchy(),
    private val decisionCouncil: AmarDecisionCouncil = AmarDecisionCouncil(),
    private val directionEngine: AmarDecisionDirectionEngine = AmarDecisionDirectionEngine(),
    private val roleOpinionEngine: AmarRoleOpinionEngine = AmarRoleOpinionEngine()
) {
    suspend fun run(request: AmarAgentRequest, availableTools: List<AmarAgentTool>, budget: AmarAgentBudget = AmarAgentBudget()): AmarAgentRunResult {
        val safeBudget = budget.normalized()
        val safeMaximumSources = request.maximumSourceCount.coerceIn(1, safeBudget.maxSources.coerceAtLeast(1))
        val safeRequestedSources = request.requestedSourceCount.coerceIn(1, safeMaximumSources)
        val safeTools = availableTools
            .filter { it.scope != AmarToolScope.EXECUTION_FUTURE && it.readOnly }
            .distinctBy { it.id }
        val session = AmarAgentSession(budget = safeBudget)
        session.record(AmarAgentStage.INTAKE, request.text)
        val mandates = hierarchy.defaultMandates()
        session.record(AmarAgentStage.PLAN, "roles=${mandates.joinToString(",") { it.role.name }}")
        val plan = planner.plan(request, safeTools)
        session.record(AmarAgentStage.PLAN, plan.steps.joinToString(" -> "))

        val needsResearch = plan.intent == AgentIntent.RESEARCH || plan.intent == AgentIntent.TRADE_ANALYSIS
        val report = if (needsResearch) {
            session.record(AmarAgentStage.RETRIEVE, "RESEARCHER: multi-source research")
            researchEngine.research(
                ResearchRequest(
                    request.text,
                    safeRequestedSources,
                    request.requireCrossValidation,
                    minOf(safeBudget.targetIndependentSources, safeRequestedSources)
                )
            )
        } else null

        val verification = report?.let {
            session.record(AmarAgentStage.VERIFY, "RESEARCHER: source quality and independence")
            sourceVerifier.verify(it.findings)
        }
        val consensus = report?.let { consensusEngine.summarize(it.findings) }
        val evidenceText = buildString {
            appendLine("Evidence summary:")
            if (report == null) appendLine("No external research required.") else {
                appendLine("sources=${report.findings.size}")
                appendLine("verificationAccepted=${verification?.accepted ?: false}")
                appendLine("confidence=${verification?.confidence ?: 0.0}")
                appendLine("consensus=${consensus?.consensusScore ?: 0.0}")
                appendLine("supporting=${consensus?.supportingSources ?: 0}")
                appendLine("opposing=${consensus?.opposingSources ?: 0}")
                appendLine("unknown=${consensus?.unknownSources ?: 0}")
                report.conflicts.take(20).forEach { appendLine("conflict=$it") }
            }
        }

        session.record(AmarAgentStage.REASON, "ANALYST/ADVISOR: reasoning with evidence and uncertainty")
        val answer = reasoningProvider.respond(
            AmarAgentContext(
                userText = request.text + "\n\n" + evidenceText,
                tools = safeTools,
                executionAllowed = false,
                brokerAccessAllowed = false,
                requestedSourceCount = safeRequestedSources,
                maximumSourceCount = safeMaximumSources,
                requireCrossValidation = request.requireCrossValidation,
                requireBacktestWhenApplicable = request.requireBacktestWhenApplicable
            )
        )

        session.record(AmarAgentStage.CHALLENGE, "ADVISOR/RISK_GUARD: adversarial critique")
        val critique = critic.review(answer.answer, report?.findings.orEmpty(), requireEvidence = needsResearch)
        val direction = directionEngine.detect(answer.answer)
        val decisionRelevant = plan.intent == AgentIntent.TRADE_ANALYSIS
        val councilReview = if (decisionRelevant && direction != AmarDecisionDirection.UNKNOWN) {
            val confidence = minOf(
                verification?.confidence ?: 0.0,
                consensus?.consensusScore ?: 0.0
            )
            val opinions = roleOpinionEngine.buildOpinions(answer, direction, confidence, report?.findings.orEmpty())
            decisionCouncil.review(opinions)
        } else {
            AmarDecisionReview(
                emptyList(),
                0.0,
                emptyList(),
                !decisionRelevant,
                if (decisionRelevant) "explicit_direction_required" else "hierarchy review not required for this response"
            )
        }

        session.record(AmarAgentStage.VALIDATE, "DECISION_CONFIRMATION: direction=${direction.name}, consensus=${councilReview.consensusScore}, conflicts=${councilReview.conflicts.size}")
        val decisionVerification = verifier.verify(answer.answer, consensus, critique, verification)
        val hierarchyApproved = councilReview.approved && councilReview.conflicts.isEmpty()
        val finalApproved = decisionVerification.approved && hierarchyApproved
        session.record(if (finalApproved) AmarAgentStage.COMPLETE else AmarAgentStage.BLOCKED, if (finalApproved) "AUDITOR: final decision accepted" else "RISK_GUARD: final decision blocked")

        val finalIssues = mutableListOf<String>()
        finalIssues += decisionVerification.issues
        finalIssues += councilReview.conflicts
        if (!councilReview.approved && councilReview.conflicts.isEmpty()) finalIssues += councilReview.reason
        val finalResponse = if (finalApproved) answer else answer.copy(status = AmarAgentResponse.Status.ERROR, answer = "لم يتم اعتماد الإجابة بعد: ${finalIssues.distinct().joinToString(", ")}")

        return AmarAgentRunResult(response = finalResponse, plan = plan, research = report, sourceVerification = verification, consensus = consensus, critique = critique, finalVerification = decisionVerification, sessionEvents = session.events())
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
