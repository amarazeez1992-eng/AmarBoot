package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.agent.admission.AmarEvidenceIntake
import com.personal.gridbot.amaros.agent.admission.AmarFindingToCandidateConverter
import com.personal.gridbot.amaros.intelligence.verification.AmarVerificationLayer

data class TaskExecutionRuntime(
    val request: AmarAgentRequest,
    val budget: AmarAgentBudget,
    val plan: AmarAgentPlan,
    val safeRequestedSources: Int,
    val safeMaximumSources: Int,
    val safeTools: List<AmarAgentTool>,
    val plannedTools: List<AmarAgentTool>,
    val understanding: AmarIntentUnderstanding,
    val queryPolicy: AmarQueryPolicy,
    val researchEngine: AmarResearchEngine,
    val sourceVerifier: AmarSourceVerifier,
    val consensusEngine: AmarAgentEvidenceConsensus,
    val critic: AmarAgentCritic,
    val verifier: AmarAgentVerifier,
    val reasoningProvider: AmarReasoningProvider,
    val stageTwoEngine: AmarStageTwoEngine,
    val stageThreeEngine: AmarStageThreeEngine,
    val evidenceQualityEngine: AmarEvidenceQualityEngine,
    val claimVerificationEngine: AmarClaimVerificationEngine,
    val confidenceCalibrationEngine: AmarConfidenceCalibrationEngine,
    val canonicalEvidenceQuality: AmarCanonicalEvidenceQualityAssembler,
    val evidenceIntake: AmarEvidenceIntake,
    val findingToCandidateConverter: AmarFindingToCandidateConverter,
    val verificationLayer: AmarVerificationLayer,
    val hierarchy: AmarAgentHierarchy,
    val decisionCouncil: AmarDecisionCouncil,
    val directionEngine: AmarDecisionDirectionEngine,
    val roleOpinionEngine: AmarRoleOpinionEngine
)

data class EvidenceTaskArtifact(
    val report: ResearchReport?,
    val stageThree: AmarStageThreeResult?,
    val findings: List<ResearchFinding>,
    val sourceVerification: AmarSourceVerification?,
    val verificationReport: com.personal.gridbot.amaros.intelligence.verification.AmarVerificationReport?,
    val consensus: AmarConsensusReport?,
    val intakeResult: EvidenceIntakeResult?,
    val canonicalEvidenceCertification: AmarCanonicalEvidenceQualityCertificationReport?
)

data class ReasonTaskArtifact(
    val answer: AmarAgentResponse,
    val stageTwo: AmarStageTwoResult?,
    val evidence: EvidenceTaskArtifact
)

data class ChallengeTaskArtifact(
    val critique: AmarCritique,
    val reason: ReasonTaskArtifact
)

data class ValidateTaskArtifact(
    val verification: com.personal.gridbot.amaros.intelligence.verification.AmarVerificationReport?,
    val stageThree: AmarStageThreeResult?,
    val reason: ReasonTaskArtifact,
    val challenge: ChallengeTaskArtifact?
)

data class AuditTaskArtifact(
    val response: AmarAgentResponse,
    val finalVerification: AmarDecisionVerification,
    val hardening: AmarStageTwoHardeningReport,
    val councilReview: AmarDecisionReview,
    val critique: AmarCritique,
    val evidence: EvidenceTaskArtifact,
    val stageTwo: AmarStageTwoResult?,
    val stageThree: AmarStageThreeResult?,
    val canonicalEvidenceCertification: AmarCanonicalEvidenceQualityCertificationReport?
)

private fun ContextEnvelope.runtime(): TaskExecutionRuntime =
    artifact("runtime") as? TaskExecutionRuntime
        ?: error("Task execution runtime missing from context")

private fun ContextEnvelope.artifactOrNull(key: String): Any? = artifact(key)

private fun evidenceText(artifact: EvidenceTaskArtifact): String = buildString {
    appendLine("Evidence summary:")
    if (artifact.report == null) {
        appendLine("No external research required.")
    } else {
        appendLine("newSources=" + (artifact.stageThree?.newEvidenceCount ?: artifact.report.findings.size))
        appendLine("unifiedEvidence=" + artifact.findings.size)
        appendLine("retrievedMemory=" + (artifact.stageThree?.retrievedMemoryCount ?: 0))
        appendLine("memorySize=" + (artifact.stageThree?.memorySize ?: 0))
        appendLine("independentSources=" + (artifact.stageThree?.independentSourceCount ?: 0))
        appendLine("verificationAccepted=" + (artifact.sourceVerification?.accepted ?: false))
        val confidencePercent = kotlin.math.round(((artifact.sourceVerification?.confidence ?: 0.0).coerceIn(0.0, 1.0)) * 100.0).toInt()
        appendLine("مستوى الثقة: " + confidencePercent + "%")
        appendLine("consensus=" + (artifact.consensus?.consensusScore ?: 0.0))
        appendLine("supporting=" + (artifact.consensus?.supportingSources ?: 0))
        appendLine("opposing=" + (artifact.consensus?.opposingSources ?: 0))
        appendLine("unknown=" + (artifact.consensus?.unknownSources ?: 0))
        artifact.report.conflicts.take(20).forEach { appendLine("conflict=" + it) }
        artifact.findings.take(20).forEachIndexed { index, finding ->
            appendLine("source=" + index + "|title=" + finding.sourceTitle + "|authority=" + finding.authority + "|stance=" + finding.stance + "|publisher=" + finding.publisher + "|uri=" + finding.sourceUri)
            appendLine("evidence=" + finding.evidence.take(1200))
        }
    }
}

private fun stageTwoText(result: AmarStageTwoResult?): String = buildString {
    if (result == null) return@buildString
    appendLine()
    appendLine("Stage 2 deliberation:")
    appendLine("chosenDirection=" + result.chosenDirection)
    val confidencePercent = kotlin.math.round(result.confidence.coerceIn(0.0, 1.0) * 100.0).toInt()
    appendLine("مستوى ثقة التحليل: " + confidencePercent + "%")
    appendLine("approvedForSimulation=" + result.approvedForSimulation)
    appendLine("consensusDirection=" + result.deliberation.consensusDirection)
    appendLine("conflicts=" + result.deliberation.conflicts.joinToString(" | "))
    result.deliberation.reports.forEach {
        appendLine("role=" + it.roleId + ";direction=" + it.direction + ";confidence=" + it.confidence + ";conclusion=" + it.conclusion)
    }
    appendLine("executionAllowed=false")
    appendLine("brokerAccessAllowed=false")
}

class NormalizeTaskExecutor : TaskExecutor {
    override suspend fun execute(task: AmarTaskUnit, context: ContextEnvelope): TaskExecutionResult {
        val runtime = context.runtime()
        val analysis = runtime.understanding.understand(runtime.request.text)
        return TaskExecutionResult(TaskResultStatus.SUCCESS, analysis, context.provenance + task.id)
    }
}

class UnderstandTaskExecutor : TaskExecutor {
    override suspend fun execute(task: AmarTaskUnit, context: ContextEnvelope): TaskExecutionResult {
        val analysis = context.artifactOrNull("normalize") as? AmarIntentAnalysis
            ?: error("NORMALIZE result missing before UNDERSTAND")
        return TaskExecutionResult(TaskResultStatus.SUCCESS, analysis, context.provenance + task.id)
    }
}

class ContextTaskExecutor : TaskExecutor {
    override suspend fun execute(task: AmarTaskUnit, context: ContextEnvelope): TaskExecutionResult {
        val runtime = context.runtime()
        val analysis = context.artifactOrNull("understand") as? AmarIntentAnalysis
        val value = if (task.id == "extract_entities") {
            analysis?.entities ?: emptyList<String>()
        } else {
            runtime.stageThreeEngine.synchronize(runtime.request.text, emptyList())
        }
        return TaskExecutionResult(TaskResultStatus.SUCCESS, value, context.provenance + task.id)
    }
}

class ConstraintTaskExecutor : TaskExecutor {
    override suspend fun execute(task: AmarTaskUnit, context: ContextEnvelope): TaskExecutionResult {
        val runtime = context.runtime()
        val decision = runtime.queryPolicy.classify(runtime.plan, runtime.request)
        return TaskExecutionResult(TaskResultStatus.SUCCESS, decision, context.provenance + task.id)
    }
}

class EvidenceTaskExecutor : TaskExecutor {
    override suspend fun execute(task: AmarTaskUnit, context: ContextEnvelope): TaskExecutionResult {
        val runtime = context.runtime()
        val policy = runtime.queryPolicy.classify(runtime.plan, runtime.request)
        if (!policy.requiresResearch) {
            return TaskExecutionResult(
                TaskResultStatus.SUCCESS,
                EvidenceTaskArtifact(null, null, emptyList(), null, null, null, null, null),
                context.provenance + task.id
            )
        }

        val report = runtime.researchEngine.research(
            ResearchRequest(
                runtime.request.text,
                runtime.safeRequestedSources,
                runtime.request.requireCrossValidation,
                minOf(runtime.budget.targetIndependentSources, runtime.safeRequestedSources)
            )
        )
        val stageThree = runtime.stageThreeEngine.synchronize(runtime.request.text, report.findings)
        val unifiedFindings = stageThree.unifiedEvidence
        val intakeResult = runtime.evidenceIntake.intake(
            runtime.request.text,
            unifiedFindings.map { runtime.findingToCandidateConverter.toCandidate(it) }
        )
        val verification = runtime.verificationLayer.verifyEvidenceOnly(unifiedFindings)
        val sourceVerification = runtime.sourceVerifier.verify(unifiedFindings)
        val consensus = runtime.consensusEngine.summarize(unifiedFindings)
        val canonical = runtime.canonicalEvidenceQuality.certify(
            unifiedFindings,
            System.currentTimeMillis(),
            verification,
            intakeResult
        )
        val artifact = EvidenceTaskArtifact(
            report = report,
            stageThree = stageThree,
            findings = unifiedFindings,
            sourceVerification = sourceVerification,
            verificationReport = verification,
            consensus = consensus,
            intakeResult = intakeResult,
            canonicalEvidenceCertification = canonical
        )
        return TaskExecutionResult(
            TaskResultStatus.SUCCESS,
            artifact,
            context.provenance + task.id,
            report.conflicts
        )
    }
}

class ReasonTaskExecutor : TaskExecutor {
    override suspend fun execute(task: AmarTaskUnit, context: ContextEnvelope): TaskExecutionResult {
        val runtime = context.runtime()
        val evidence = context.artifactOrNull("evidence") as? EvidenceTaskArtifact
            ?: error("EVIDENCE result missing before REASON")
        val decisionRelevant = runtime.plan.intent == AgentIntent.TRADE_ANALYSIS
        val stageTwo = if (decisionRelevant) {
            runtime.stageTwoEngine.deliberate(runtime.request.text, evidence.findings)
        } else null
        val answer = runtime.reasoningProvider.respond(
            AmarAgentContext(
                userText = runtime.request.text + "\n\n" + evidenceText(evidence) + stageTwoText(stageTwo),
                tools = runtime.plannedTools,
                executionAllowed = false,
                brokerAccessAllowed = false,
                requestedSourceCount = runtime.safeRequestedSources,
                maximumSourceCount = runtime.safeMaximumSources,
                requireCrossValidation = runtime.request.requireCrossValidation,
                requireBacktestWhenApplicable = runtime.request.requireBacktestWhenApplicable
            )
        )
        return TaskExecutionResult(
            TaskResultStatus.SUCCESS,
            ReasonTaskArtifact(answer, stageTwo, evidence),
            context.provenance + task.id
        )
    }
}

class ChallengeTaskExecutor : TaskExecutor {
    override suspend fun execute(task: AmarTaskUnit, context: ContextEnvelope): TaskExecutionResult {
        val runtime = context.runtime()
        val reason = context.artifactOrNull("reason") as? ReasonTaskArtifact
            ?: error("REASON result missing before CHALLENGE")
        val strictEvidence = runtime.queryPolicy.classify(runtime.plan, runtime.request).requiresStrictEvidence
        val critique = runtime.critic.review(
            reason.answer.answer,
            reason.evidence.findings,
            requireEvidence = strictEvidence
        )
        return TaskExecutionResult(
            TaskResultStatus.SUCCESS,
            ChallengeTaskArtifact(critique, reason),
            context.provenance + task.id,
            critique.issues
        )
    }
}

class ValidateTaskExecutor : TaskExecutor {
    override suspend fun execute(task: AmarTaskUnit, context: ContextEnvelope): TaskExecutionResult {
        val runtime = context.runtime()
        val reason = context.artifactOrNull("reason") as? ReasonTaskArtifact
            ?: error("REASON result missing before VALIDATE")
        val challenge = context.artifactOrNull("challenge") as? ChallengeTaskArtifact
        val findings = reason.evidence.findings
        val stageThree = runtime.stageThreeEngine.synchronize(runtime.request.text, findings)
        val verification = runtime.verificationLayer.verify(reason.answer.answer, stageThree.unifiedEvidence)
        val artifact = ValidateTaskArtifact(verification, stageThree, reason, challenge)
        return TaskExecutionResult(
            TaskResultStatus.SUCCESS,
            artifact,
            context.provenance + task.id
        )
    }
}

class ResponseTaskExecutor : TaskExecutor {
    override suspend fun execute(task: AmarTaskUnit, context: ContextEnvelope): TaskExecutionResult {
        val reason = context.artifactOrNull("reason") as? ReasonTaskArtifact
            ?: error("REASON result missing before RESPONSE")
        return TaskExecutionResult(TaskResultStatus.SUCCESS, reason.answer, context.provenance + task.id)
    }
}

class AuditTaskExecutor : TaskExecutor {
    override suspend fun execute(task: AmarTaskUnit, context: ContextEnvelope): TaskExecutionResult {
        val runtime = context.runtime()
        val reason = context.artifactOrNull("reason") as? ReasonTaskArtifact
            ?: error("REASON result missing before AUDIT")
        val challenge = context.artifactOrNull("challenge") as? ChallengeTaskArtifact
            ?: error("CHALLENGE result missing before AUDIT")
        val evidence = reason.evidence
        val stageTwo = reason.stageTwo
        val strictEvidence = runtime.queryPolicy.classify(runtime.plan, runtime.request).requiresStrictEvidence
        val validate = context.artifactOrNull("validate") as? ValidateTaskArtifact
        val findings = validate?.stageThree?.unifiedEvidence ?: evidence.findings
        val answer = reason.answer

        val quality = runtime.evidenceQualityEngine.assess(findings)
        val claims = runtime.claimVerificationEngine.verify(answer.answer, findings)
        val raw = listOfNotNull(
            evidence.sourceVerification?.confidence,
            evidence.consensus?.consensusScore,
            stageTwo?.confidence
        ).minOrNull() ?: 0.0
        val calibrated = runtime.confidenceCalibrationEngine.calibrate(
            raw,
            quality.score,
            claims,
            (stageTwo?.deliberation?.conflicts?.size ?: 0) + quality.duplicateEvidenceCount
        )
        val hardeningIssues = buildList {
            if (findings.isNotEmpty() && quality.independentSourceCount < 2) add("insufficient_independent_sources")
            if (quality.duplicateEvidenceCount > 0) add("duplicate_evidence_detected")
            if (findings.isNotEmpty() && !claims.accepted) add("claim_verification_failed")
            if (findings.isNotEmpty() && calibrated < .80) add("confidence_below_threshold")
        }.distinct()
        val hardening = AmarStageTwoHardeningReport(
            evidenceQuality = quality,
            claimVerification = claims,
            calibratedConfidence = calibrated,
            approved = hardeningIssues.isEmpty(),
            issues = hardeningIssues
        )

        val direction = runtime.directionEngine.detect(answer.answer)
        val stageDirection = stageTwo?.chosenDirection ?: AmarDecisionDirection.UNKNOWN
        val directionMismatch = runtime.plan.intent == AgentIntent.TRADE_ANALYSIS &&
            stageDirection != AmarDecisionDirection.UNKNOWN &&
            direction != AmarDecisionDirection.UNKNOWN &&
            stageDirection != direction

        val councilReview = if (runtime.plan.intent != AgentIntent.TRADE_ANALYSIS) {
            AmarDecisionReview(emptyList(), 1.0, emptyList(), true, "hierarchy review not required for this response")
        } else if (direction != AmarDecisionDirection.UNKNOWN && !directionMismatch) {
            val confidence = minOf(
                evidence.sourceVerification?.confidence ?: 0.0,
                evidence.consensus?.consensusScore ?: 0.0,
                stageTwo?.confidence ?: 0.0
            )
            runtime.decisionCouncil.review(
                runtime.roleOpinionEngine.buildOpinions(answer, direction, confidence, findings)
            )
        } else {
            AmarDecisionReview(
                emptyList(),
                0.0,
                if (directionMismatch) listOf("final_answer_direction_mismatch") else emptyList(),
                false,
                if (directionMismatch) "final answer conflicts with Stage 2 consensus" else "explicit_direction_required"
            )
        }

        val finalConsensus = if (strictEvidence) evidence.consensus else null
        val finalVerification = runtime.verifier.verify(
            answer.answer,
            finalConsensus,
            challenge.critique,
            if (strictEvidence) evidence.sourceVerification else null
        )
        val stageTwoApproved = runtime.plan.intent != AgentIntent.TRADE_ANALYSIS ||
            (stageTwo?.approvedForSimulation == true)
        val canonicalApproved = !strictEvidence ||
            evidence.canonicalEvidenceCertification?.certificationScore == 1.0
        val hardeningApproved = !strictEvidence ||
            (hardening.approved && canonicalApproved)
        val hierarchyApproved = stageTwoApproved &&
            hardeningApproved &&
            !directionMismatch &&
            councilReview.approved &&
            councilReview.conflicts.isEmpty()
        val finalApproved = finalVerification.approved && hierarchyApproved

        val finalIssues = buildList {
            addAll(finalVerification.issues)
            addAll(hardening.issues)
            addAll(councilReview.conflicts)
            if (directionMismatch) add("final_answer_direction_mismatch")
            if (!stageTwoApproved) add("stage_two_deliberation_not_approved")
            if (strictEvidence && !canonicalApproved) add("point10_evidence_quality_not_verified")
            if (!councilReview.approved && councilReview.conflicts.isEmpty()) add(councilReview.reason)
        }.distinct()

        val finalResponse = if (finalApproved) {
            answer
        } else {
            val reasonCode = when {
                findings.isEmpty() -> "no_evidence"
                evidence.sourceVerification != null && !evidence.sourceVerification.accepted -> "source_verification_failed"
                else -> "final_validation_failed"
            }
            answer.copy(
                status = AmarAgentResponse.Status.ERROR,
                answer = "لم يتم اعتماد الإجابة بعد. لم أجد مصادر كافية ومرتبطة بسؤالك تسمح لي بتقديم إجابة موثوقة. [" + reasonCode + "]"
            )
        }
        val audit = AuditTaskArtifact(
            response = finalResponse,
            finalVerification = finalVerification.copy(
                issues = (finalVerification.issues + finalIssues).distinct(),
                approved = finalApproved
            ),
            hardening = hardening,
            councilReview = councilReview,
            critique = challenge.critique,
            evidence = evidence,
            stageTwo = stageTwo,
            stageThree = validate?.stageThree ?: evidence.stageThree,
            canonicalEvidenceCertification = evidence.canonicalEvidenceCertification
        )
        return TaskExecutionResult(
            status = if (finalApproved) TaskResultStatus.SUCCESS else TaskResultStatus.BLOCKED,
            value = audit,
            provenance = context.provenance + task.id,
            conflicts = finalIssues
        )
    }
}

fun defaultStage11EngineRegistry(): EngineRegistry {
    val registry = EngineRegistry()
        .register(AmarTaskKind.NORMALIZE, NormalizeTaskExecutor())
        .register(AmarTaskKind.UNDERSTAND, UnderstandTaskExecutor())
        .register(AmarTaskKind.CONTEXT, ContextTaskExecutor())
        .register(AmarTaskKind.CONSTRAINT, ConstraintTaskExecutor())
        .register(AmarTaskKind.EVIDENCE, EvidenceTaskExecutor())
        .register(AmarTaskKind.REASON, ReasonTaskExecutor())
        .register(AmarTaskKind.CHALLENGE, ChallengeTaskExecutor())
        .register(AmarTaskKind.VALIDATE, ValidateTaskExecutor())
        .register(AmarTaskKind.RESPONSE, ResponseTaskExecutor())
        .register(AmarTaskKind.AUDIT, AuditTaskExecutor())
    return registry
}
