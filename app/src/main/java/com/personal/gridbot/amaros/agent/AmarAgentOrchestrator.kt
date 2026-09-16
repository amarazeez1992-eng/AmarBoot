package com.personal.gridbot.amaros.agent

/**
 * Canonical AMAR Agent runtime.
 *
 * The Agent is the single application decision boundary. Transport/UI layers do not
 * approve work. Research, verification and critique are subordinate engines.
 * Trading/broker execution is deliberately unavailable in this stage.
 */
class AmarAgentOrchestrator(
    private val planner: AmarAgentPlanner,
    private val researchEngine: AmarResearchEngine,
    private val sourceVerifier: AmarSourceVerifier,
    private val consensusEngine: AmarAgentEvidenceConsensus,
    private val critic: AmarAgentCritic,
    private val verifier: AmarAgentVerifier,
    private val reasoningProvider: AmarReasoningProvider,
    private val stageThreeEngine: AmarStageThreeEngine = AmarStageThreeEngine(),
    private val evidenceQualityEngine: AmarEvidenceQualityEngine = AmarEvidenceQualityEngine(),
    private val claimVerificationEngine: AmarClaimVerificationEngine = AmarClaimVerificationEngine(),
    private val confidenceCalibrationEngine: AmarConfidenceCalibrationEngine = AmarConfidenceCalibrationEngine()
) {
    suspend fun run(
        request: AmarAgentRequest,
        availableTools: List<AmarAgentTool>,
        budget: AmarAgentBudget = AmarAgentBudget()
    ): AmarAgentRunResult {
        val safeBudget = budget.normalized()
        val maxSources = request.maximumSourceCount.coerceIn(1, safeBudget.maxSources.coerceAtLeast(1))
        val requestedSources = request.requestedSourceCount.coerceIn(1, maxSources)
        val tools = availableTools
            .filter { it.scope != AmarToolScope.EXECUTION_FUTURE }
            .distinctBy { it.id }

        val session = AmarAgentSession(budget = safeBudget)
        session.record(AmarAgentStage.INTAKE, request.text)
        val plan = planner.plan(request, tools)
        session.record(AmarAgentStage.PLAN, plan.steps.joinToString(" -> "))

        val researchNeeded = plan.intent == AgentIntent.RESEARCH || plan.intent == AgentIntent.TRADE_ANALYSIS
        val research = if (researchNeeded) {
            session.record(AmarAgentStage.RETRIEVE, "multi-source research")
            researchEngine.research(
                ResearchRequest(
                    request.text,
                    requestedSources,
                    request.requireCrossValidation,
                    minOf(safeBudget.targetIndependentSources, requestedSources)
                )
            )
        } else null

        val stageThree = if (researchNeeded) {
            stageThreeEngine.synchronize(request.text, research?.findings.orEmpty())
        } else null
        val findings = stageThree?.unifiedEvidence ?: research?.findings.orEmpty()

        val sourceVerification = research?.let {
            session.record(AmarAgentStage.VERIFY, "source verification")
            sourceVerifier.verify(findings)
        }
        val consensus = research?.let { consensusEngine.summarize(findings) }

        val evidenceContext = buildEvidenceContext(
            research = research,
            stageThree = stageThree,
            sourceVerification = sourceVerification,
            consensus = consensus
        )

        session.record(AmarAgentStage.REASON, "Agent synthesis")
        val answer = reasoningProvider.respond(
            AmarAgentContext(
                userText = request.text + evidenceContext,
                tools = tools.filter { it.id in plan.requiredTools },
                executionAllowed = false,
                brokerAccessAllowed = false,
                requestedSourceCount = requestedSources,
                maximumSourceCount = maxSources,
                requireCrossValidation = request.requireCrossValidation,
                requireBacktestWhenApplicable = request.requireBacktestWhenApplicable
            )
        )

        session.record(AmarAgentStage.CHALLENGE, "adversarial critique")
        val critique = critic.review(answer.answer, findings, requireEvidence = researchNeeded)
        val hardening = harden(answer.answer, findings, sourceVerification, consensus)

        session.record(AmarAgentStage.VALIDATE, "final Agent validation")
        val finalVerification = verifier.verify(answer.answer, consensus, critique, sourceVerification)

        // Non-trading requests must never be blocked by a trading-only decision council.
        // Trading/broker authority remains disabled until the later MT5 stage.
        val approved = finalVerification.approved && hardening.approved && critique.issues.isEmpty()
        val finalResponse = if (approved) {
            answer
        } else {
            val issues = (finalVerification.issues + hardening.issues + critique.issues)
                .distinct()
                .ifEmpty { listOf("agent_validation_failed") }
            answer.copy(
                status = AmarAgentResponse.Status.ERROR,
                answer = "لم يتم اعتماد الإجابة: ${issues.joinToString(", ") }"
            )
        }

        session.record(
            if (approved) AmarAgentStage.COMPLETE else AmarAgentStage.BLOCKED,
            if (approved) "Agent validation accepted" else "Agent validation blocked"
        )

        return AmarAgentRunResult(
            response = finalResponse,
            plan = plan,
            research = research,
            sourceVerification = sourceVerification,
            consensus = consensus,
            critique = critique,
            finalVerification = finalVerification,
            stageThree = stageThree,
            hardening = hardening,
            sessionEvents = session.events()
        )
    }

    private fun buildEvidenceContext(
        research: ResearchReport?,
        stageThree: AmarStageThreeResult?,
        sourceVerification: AmarSourceVerification?,
        consensus: AmarConsensusReport?
    ): String = buildString {
        appendLine()
        appendLine("AMAR EVIDENCE CONTEXT")
        if (research == null) {
            appendLine("research=not_required")
            return@buildString
        }
        appendLine("newSources=${stageThree?.newEvidenceCount ?: research.findings.size}")
        appendLine("unifiedEvidence=${stageThree?.unifiedEvidence?.size ?: research.findings.size}")
        appendLine("independentSources=${stageThree?.independentSourceCount ?: 0}")
        appendLine("verificationAccepted=${sourceVerification?.accepted ?: false}")
        appendLine("confidence=${sourceVerification?.confidence ?: 0.0}")
        appendLine("consensus=${consensus?.consensusScore ?: 0.0}")
        research.conflicts.take(20).forEach { appendLine("conflict=$it") }
    }

    private fun harden(
        answer: String,
        findings: List<ResearchFinding>,
        verification: AmarSourceVerification?,
        consensus: AmarConsensusReport?
    ): AmarStageTwoHardeningReport {
        val quality = evidenceQualityEngine.assess(findings)
        val claims = claimVerificationEngine.verify(answer, findings)
        val raw = listOfNotNull(verification?.confidence, consensus?.consensusScore)
            .minOrNull() ?: 1.0
        val calibrated = confidenceCalibrationEngine.calibrate(
            raw,
            quality.score,
            claims,
            quality.duplicateEvidenceCount
        )
        val issues = mutableListOf<String>()
        if (findings.isNotEmpty() && quality.independentSourceCount < 2) issues += "insufficient_independent_sources"
        if (quality.duplicateEvidenceCount > 0) issues += "duplicate_evidence_detected"
        if (findings.isNotEmpty() && !claims.accepted) issues += "claim_verification_failed"
        if (findings.isNotEmpty() && calibrated < .80) issues += "confidence_below_threshold"
        return AmarStageTwoHardeningReport(
            evidenceQuality = quality,
            claimVerification = claims,
            calibratedConfidence = calibrated,
            approved = issues.isEmpty(),
            issues = issues.distinct()
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
    val stageTwo: AmarStageTwoResult? = null,
    val stageThree: AmarStageThreeResult? = null,
    val hardening: AmarStageTwoHardeningReport? = null,
    val sessionEvents: List<AmarAgentEvent>
)
