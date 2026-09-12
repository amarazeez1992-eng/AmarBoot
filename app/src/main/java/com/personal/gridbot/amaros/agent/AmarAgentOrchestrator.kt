package com.personal.gridbot.amaros.agent

/**
 * Central pipeline for high-confidence answers.
 * It coordinates research, verification, layered decision review and criticism
 * without granting execution.
 */
class AmarAgentOrchestrator(
    private val planner: AmarAgentPlanner,
    private val researchEngine: AmarResearchEngine,
    private val sourceVerifier: AmarSourceVerifier,
    private val consensusEngine: AmarAgentEvidenceConsensus,
    private val critic: AmarAgentCritic,
    private val verifier: AmarAgentVerifier,
    private val reasoningProvider: AmarReasoningProvider,
    private val hierarchy: AmarAgentHierarchy = AmarAgentHierarchy(),
    private val decisionCouncil: AmarDecisionCouncil = AmarDecisionCouncil()
) {
    suspend fun run(
        request: AmarAgentRequest,
        availableTools: List<AmarAgentTool>,
        budget: AmarAgentBudget = AmarAgentBudget()
    ): AmarAgentRunResult {
        val safeBudget = budget.normalized()
        val session = AmarAgentSession(budget = safeBudget)
        session.record(AmarAgentStage.INTAKE, request.text)

        val mandates = hierarchy.defaultMandates()
        session.record(
            AmarAgentStage.PLAN,
            "roles=${mandates.joinToString(",") { it.role.name }}"
        )

        val plan = planner.plan(request, availableTools)
        session.record(AmarAgentStage.PLAN, plan.steps.joinToString(" -> "))

        val needsResearch = plan.intent == AgentIntent.RESEARCH || plan.intent == AgentIntent.TRADE_ANALYSIS
        val report = if (needsResearch) {
            session.record(AmarAgentStage.RETRIEVE, "RESEARCHER: multi-source research")
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
            session.record(AmarAgentStage.VERIFY, "RESEARCHER: source quality and independence")
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

        session.record(AmarAgentStage.REASON, "ANALYST/ADVISOR: reasoning with evidence and uncertainty")
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

        session.record(AmarAgentStage.CHALLENGE, "ADVISOR/RISK_GUARD: adversarial critique")
        val critique = critic.review(answer.answer, report?.findings.orEmpty())

        val direction = inferDecisionDirection(answer.answer)
        val primaryOpinion = AmarAgentOpinion(
            role = AmarAgentRole.ANALYST,
            conclusion = answer.answer,
            confidence = (verification?.confidence ?: consensus?.consensusScore ?: 0.0),
            direction = direction,
            risks = critique.issues
        )
        val councilReview = decisionCouncil.review(listOf(primaryOpinion))
        session.record(
            AmarAgentStage.VALIDATE,
            "DECISION_CONFIRMATION: direction=${direction.name}, consensus=${councilReview.consensusScore}, conflicts=${councilReview.conflicts.size}"
        )

        val decisionVerification = verifier.verify(answer.answer, consensus, critique)
        val hierarchyApproved = councilReview.approved && councilReview.conflicts.isEmpty()
        val finalApproved = decisionVerification.approved && hierarchyApproved

        session.record(
            if (finalApproved) AmarAgentStage.COMPLETE else AmarAgentStage.BLOCKED,
            if (finalApproved) "AUDITOR: final decision accepted" else "RISK_GUARD: final decision blocked"
        )

        val finalResponse = if (finalApproved) answer else answer.copy(
            status = AmarAgentResponse.Status.ERROR,
            answer = "لم يتم اعتماد الإجابة بعد: ${buildList {
                addAll(decisionVerification.issues)
                addAll(councilReview.conflicts)
                if (!councilReview.approved && councilReview.conflicts.isEmpty()) add(councilReview.reason)
            }.distinct().joinToString(", ")}"
        )

        return AmarAgentRunResult(
            response = finalResponse,
            plan = plan,
            research = report,
            sourceVerification = verification,
            consensus = consensus,
            critique = critique,
            finalVerification = decisionVerification,
            sessionEvents = session.events()
        )
    }

    private fun inferDecisionDirection(text: String): AmarDecisionDirection {
        val normalized = text.lowercase()
        val hasBuy = listOf("buy", "شراء", "شراءً", "شراء ").any(normalized::contains)
        val hasSell = listOf("sell", "بيع", "بيعاً", "بيع ").any(normalized::contains)
        val hasHold = listOf("hold", "انتظار", "محايد", "لا تدخل").any(normalized::contains)

        return when {
            hasBuy && !hasSell -> AmarDecisionDirection.BUY
            hasSell && !hasBuy -> AmarDecisionDirection.SELL
            hasHold && !hasBuy && !hasSell -> AmarDecisionDirection.HOLD
            else -> AmarDecisionDirection.UNKNOWN
        }
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
