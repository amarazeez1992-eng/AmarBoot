package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.intelligence.verification.AmarVerificationLayer

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
    private val roleOpinionEngine: AmarRoleOpinionEngine = AmarRoleOpinionEngine(),
    private val stageTwoEngine: AmarStageTwoEngine = AmarStageTwoEngine(reasoningProvider),
    private val stageThreeEngine: AmarStageThreeEngine = AmarStageThreeEngine(),
    private val evidenceQualityEngine: AmarEvidenceQualityEngine = AmarEvidenceQualityEngine(),
    private val claimVerificationEngine: AmarClaimVerificationEngine = AmarClaimVerificationEngine(),
    private val confidenceCalibrationEngine: AmarConfidenceCalibrationEngine = AmarConfidenceCalibrationEngine(),
    private val queryPolicy: AmarQueryPolicy = AmarQueryPolicy(),
    private val canonicalEvidenceQuality: AmarCanonicalEvidenceQualityAssembler = AmarCanonicalEvidenceQualityAssembler(),
    private val verificationLayer: AmarVerificationLayer = AmarVerificationLayer()
) {
    suspend fun run(request: AmarAgentRequest, availableTools: List<AmarAgentTool>, budget: AmarAgentBudget = AmarAgentBudget()): AmarAgentRunResult {
        val safeBudget = budget.normalized()
        val safeMaximumSources = request.maximumSourceCount.coerceIn(1, safeBudget.maxSources.coerceAtLeast(1))
        val safeRequestedSources = request.requestedSourceCount.coerceIn(1, safeMaximumSources)
        val safeTools = availableTools.filter { it.scope != AmarToolScope.EXECUTION_FUTURE }.distinctBy { it.id }
        val session = AmarAgentSession(budget = safeBudget)
        session.record(AmarAgentStage.INTAKE, request.text)
        val mandates = hierarchy.defaultMandates()
        session.record(AmarAgentStage.PLAN, "roles=${mandates.joinToString(",") { it.role.name }}")
        val plan = planner.plan(request, safeTools)
        val plannedTools = safeTools.filter { it.id in plan.requiredTools }
        session.record(AmarAgentStage.PLAN, plan.steps.joinToString(" -> "))

        val queryPolicyDecision = queryPolicy.classify(plan, request)
        session.record(AmarAgentStage.PLAN, "QUERY_POLICY: mode=${queryPolicyDecision.mode}, research=${queryPolicyDecision.requiresResearch}, strictEvidence=${queryPolicyDecision.requiresStrictEvidence}")
        val needsResearch = queryPolicyDecision.requiresResearch
        val strictEvidence = queryPolicyDecision.requiresStrictEvidence
        val report = if (needsResearch) {
            session.record(AmarAgentStage.RETRIEVE, "RESEARCHER: multi-source research")
            researchEngine.research(ResearchRequest(request.text, safeRequestedSources, request.requireCrossValidation, minOf(safeBudget.targetIndependentSources, safeRequestedSources)))
        } else null

        val stageThree = if (needsResearch) {
            session.record(AmarAgentStage.RETRIEVE, "STAGE_3: memory + unified evidence + freshness")
            stageThreeEngine.synchronize(request.text, report?.findings.orEmpty())
        } else null
        val unifiedFindings = stageThree?.unifiedEvidence ?: report?.findings.orEmpty()

        val verification = report?.let {
            session.record(AmarAgentStage.VERIFY, "RESEARCHER: source quality and independence")
            sourceVerifier.verify(unifiedFindings)
        }
        val verificationReport = report?.let {
            verificationLayer.verifyEvidenceOnly(unifiedFindings)
        }
        val consensus = report?.let { consensusEngine.summarize(unifiedFindings) }
        val canonicalEvidenceCertification = verification?.let {
            canonicalEvidenceQuality.certify(
                findings = unifiedFindings,
                nowEpochMs = System.currentTimeMillis(),
                verification = verificationReport!!
            )
        }
        if (canonicalEvidenceCertification != null) {
            session.record(AmarAgentStage.VERIFY, "POINT10_EVIDENCE_QUALITY: certification=${canonicalEvidenceCertification.certificationScore}")
        }
        val evidenceText = buildString {
            appendLine("Evidence summary:")
            if (report == null) appendLine("No external research required.") else {
                appendLine("newSources=${stageThree?.newEvidenceCount ?: report.findings.size}")
                appendLine("unifiedEvidence=${unifiedFindings.size}")
                appendLine("retrievedMemory=${stageThree?.retrievedMemoryCount ?: 0}")
                appendLine("memorySize=${stageThree?.memorySize ?: 0}")
                appendLine("independentSources=${stageThree?.independentSourceCount ?: 0}")
                appendLine("verificationAccepted=${verification?.accepted ?: false}")
                appendLine("confidence=${verification?.confidence ?: 0.0}")
                appendLine("consensus=${consensus?.consensusScore ?: 0.0}")
                appendLine("supporting=${consensus?.supportingSources ?: 0}")
                appendLine("opposing=${consensus?.opposingSources ?: 0}")
                appendLine("unknown=${consensus?.unknownSources ?: 0}")
                report.conflicts.take(20).forEach { appendLine("conflict=$it") }
                unifiedFindings.take(20).forEachIndexed { index, finding ->
                    appendLine("source=$index|title=${finding.sourceTitle}|authority=${finding.authority}|stance=${finding.stance}|publisher=${finding.publisher}|uri=${finding.sourceUri}")
                    appendLine("evidence=${finding.evidence.take(1200)}")
                }
            }
        }

        val decisionRelevant = plan.intent == AgentIntent.TRADE_ANALYSIS
        val stageTwo = if (decisionRelevant) {
            session.record(AmarAgentStage.REASON, "STAGE_2: ANALYST -> ADVISOR -> RISK_GUARD -> DECISION_CONFIRMATION")
            stageTwoEngine.deliberate(request.text, unifiedFindings)
        } else null

        session.record(AmarAgentStage.REASON, "DIRECTOR: final synthesis with evidence and multi-role deliberation")
        val answer = reasoningProvider.respond(AmarAgentContext(
            userText = request.text + "\n\n" + evidenceText + buildStageTwoText(stageTwo),
            tools = plannedTools,
            executionAllowed = false,
            brokerAccessAllowed = false,
            requestedSourceCount = safeRequestedSources,
            maximumSourceCount = safeMaximumSources,
            requireCrossValidation = request.requireCrossValidation,
            requireBacktestWhenApplicable = request.requireBacktestWhenApplicable
        ))

        session.record(AmarAgentStage.CHALLENGE, "ADVISOR/RISK_GUARD: adversarial critique")
        val critique = critic.review(answer.answer, unifiedFindings, requireEvidence = strictEvidence)
        val hardening = buildHardeningReport(answer.answer, unifiedFindings, verification, consensus, stageTwo)
        val answerDirection = directionEngine.detect(answer.answer)
        val stageDirection = stageTwo?.chosenDirection ?: AmarDecisionDirection.UNKNOWN
        val directionMismatch = decisionRelevant && stageDirection != AmarDecisionDirection.UNKNOWN && answerDirection != AmarDecisionDirection.UNKNOWN && stageDirection != answerDirection

        val councilReview = if (!decisionRelevant) {
            AmarDecisionReview(emptyList(), 1.0, emptyList(), true, "hierarchy review not required for this response")
        } else if (answerDirection != AmarDecisionDirection.UNKNOWN && !directionMismatch) {
            val confidence = minOf(verification?.confidence ?: 0.0, consensus?.consensusScore ?: 0.0, stageTwo?.confidence ?: 0.0)
            val opinions = roleOpinionEngine.buildOpinions(answer, answerDirection, confidence, unifiedFindings)
            decisionCouncil.review(opinions)
        } else {
            AmarDecisionReview(emptyList(), 0.0, if (directionMismatch) listOf("final_answer_direction_mismatch") else emptyList(), false, if (directionMismatch) "final answer conflicts with Stage 2 consensus" else "explicit_direction_required")
        }

        session.record(AmarAgentStage.VALIDATE, "DECISION_CONFIRMATION: direction=${answerDirection.name}, stage2=${stageTwo?.approvedForSimulation ?: true}, calibrated=${hardening.calibratedConfidence}, consensus=${councilReview.consensusScore}, conflicts=${councilReview.conflicts.size}")
        val decisionVerification = verifier.verify(answer.answer, consensus, critique, if (strictEvidence) verification else null)
        val stageTwoApproved = !decisionRelevant || (stageTwo?.approvedForSimulation == true)
        val canonicalEvidenceApproved = !strictEvidence || canonicalEvidenceCertification?.certificationScore == 1.0
        val hardeningApproved = !needsResearch || !strictEvidence || (hardening.approved && canonicalEvidenceApproved)
        val hierarchyApproved = stageTwoApproved && hardeningApproved && !directionMismatch && councilReview.approved && councilReview.conflicts.isEmpty()
        val finalApproved = decisionVerification.approved && hierarchyApproved
        session.record(if (finalApproved) AmarAgentStage.COMPLETE else AmarAgentStage.BLOCKED, if (finalApproved) "AUDITOR: final decision accepted" else "RISK_GUARD: final decision blocked")

        val finalIssues = mutableListOf<String>()
        finalIssues += decisionVerification.issues
        finalIssues += hardening.issues
        finalIssues += councilReview.conflicts
        if (directionMismatch) finalIssues += "final_answer_direction_mismatch"
        if (!stageTwoApproved) finalIssues += "stage_two_deliberation_not_approved"
        if (strictEvidence && !canonicalEvidenceApproved) finalIssues += "point10_evidence_quality_not_verified"
        if (!councilReview.approved && councilReview.conflicts.isEmpty()) finalIssues += councilReview.reason
        val finalResponse = if (finalApproved) answer else answer.copy(status = AmarAgentResponse.Status.ERROR, answer = "لم يتم اعتماد الإجابة بعد: ${finalIssues.distinct().joinToString(", ")}")

        return AmarAgentRunResult(response = finalResponse, plan = plan, research = report, sourceVerification = verification, consensus = consensus, critique = critique, finalVerification = decisionVerification, stageTwo = stageTwo, stageThree = stageThree, hardening = hardening, canonicalEvidenceCertification = canonicalEvidenceCertification, sessionEvents = session.events())
    }

    private fun buildHardeningReport(answer: String, findings: List<ResearchFinding>, verification: AmarSourceVerification?, consensus: AmarConsensusReport?, stageTwo: AmarStageTwoResult?): AmarStageTwoHardeningReport {
        val quality = evidenceQualityEngine.assess(findings)
        val claims = claimVerificationEngine.verify(answer, findings)
        val raw = listOfNotNull(verification?.confidence, consensus?.consensusScore, stageTwo?.confidence).minOrNull() ?: 0.0
        val calibrated = confidenceCalibrationEngine.calibrate(raw, quality.score, claims, (stageTwo?.deliberation?.conflicts?.size ?: 0) + quality.duplicateEvidenceCount)
        val issues = mutableListOf<String>()
        if (findings.isNotEmpty() && quality.independentSourceCount < 2) issues += "insufficient_independent_sources"
        if (quality.duplicateEvidenceCount > 0) issues += "duplicate_evidence_detected"
        if (findings.isNotEmpty() && !claims.accepted) issues += "claim_verification_failed"
        if (findings.isNotEmpty() && calibrated < .80) issues += "confidence_below_threshold"
        return AmarStageTwoHardeningReport(quality, claims, calibrated, issues.isEmpty(), issues.distinct())
    }

    private fun buildStageTwoText(result: AmarStageTwoResult?): String = buildString {
        if (result == null) return@buildString
        appendLine()
        appendLine("Stage 2 deliberation:")
        appendLine("chosenDirection=${result.chosenDirection}")
        appendLine("confidence=${result.confidence}")
        appendLine("approvedForSimulation=${result.approvedForSimulation}")
        appendLine("consensusDirection=${result.deliberation.consensusDirection}")
        appendLine("conflicts=${result.deliberation.conflicts.joinToString(" | ")}")
        result.deliberation.reports.forEach { appendLine("role=${it.roleId};direction=${it.direction};confidence=${it.confidence};conclusion=${it.conclusion}") }
        appendLine("executionAllowed=false")
        appendLine("brokerAccessAllowed=false")
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
    val canonicalEvidenceCertification: AmarCanonicalEvidenceQualityCertificationReport? = null,
    val sessionEvents: List<AmarAgentEvent>
)
