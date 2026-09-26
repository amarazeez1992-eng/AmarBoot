package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.agent.admission.AmarEvidenceIntake
import com.personal.gridbot.amaros.agent.admission.AmarFindingToCandidateConverter
import com.personal.gridbot.amaros.intelligence.verification.AmarVerificationLayer

/** Single task execution pipeline for high-confidence answers without execution authority. */
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
    private val evidenceIntake: AmarEvidenceIntake = AmarEvidenceIntake(),
    private val findingToCandidateConverter: AmarFindingToCandidateConverter = AmarFindingToCandidateConverter(),
    private val verificationLayer: AmarVerificationLayer = AmarVerificationLayer(),
    private val engineRegistry: EngineRegistry? = null,
    private val failureRouter: FailureRouter = FailureRouter(),
    private val resultAggregator: ResultAggregator = ResultAggregator()
) {
    private val registry: EngineRegistry = engineRegistry ?: defaultStage11EngineRegistry()
    private val engineSelector = EngineSelector(registry)

    suspend fun run(
        request: AmarAgentRequest,
        availableTools: List<AmarAgentTool>,
        budget: AmarAgentBudget = AmarAgentBudget(),
        progress: ((AmarAgentProgress) -> Unit)? = null
    ): AmarAgentRunResult {
        val safeBudget = budget.normalized()
        val safeMaximumSources = request.maximumSourceCount.coerceIn(1, safeBudget.maxSources.coerceAtLeast(1))
        val safeRequestedSources = request.requestedSourceCount.coerceIn(1, safeMaximumSources)
        val safeTools = availableTools
            .filter { it.scope != AmarToolScope.EXECUTION_FUTURE }
            .distinctBy { it.id }

        val session = AmarAgentSession(budget = safeBudget)
        val startedAt = System.currentTimeMillis()
        fun emit(state: AgentTaskState, message: String, searched: Int = 0, accepted: Int = 0) {
            session.state(state, message)
            progress?.invoke(
                AmarAgentProgress(
                    state,
                    message,
                    searched,
                    accepted,
                    System.currentTimeMillis() - startedAt
                )
            )
        }

        emit(AgentTaskState.UNDERSTANDING, "فهم الطلب")
        session.record(AmarAgentStage.INTAKE, request.text)
        val mandates = hierarchy.defaultMandates()
        session.record(AmarAgentStage.PLAN, "roles=" + mandates.joinToString(",") { it.role.name })

        val plan = planner.plan(request, safeTools)
        val taskGraph = OrchestrationDependencyGraph(plan.tasks)
        val orderedTasks = taskGraph.topologicalOrder()
        session.transitionOrchestration(OrchestrationState.PLANNED)
        emit(AgentTaskState.PLANNING, "تخطيط مسار التحقق")
        session.transitionOrchestration(OrchestrationState.RUNNING)

        val plannedTools = safeTools.filter { it.id in plan.requiredTools }
        val runtime = TaskExecutionRuntime(
            request = request,
            budget = safeBudget,
            plan = plan,
            safeRequestedSources = safeRequestedSources,
            safeMaximumSources = safeMaximumSources,
            safeTools = safeTools,
            plannedTools = plannedTools,
            understanding = AmarIntentUnderstanding(),
            queryPolicy = queryPolicy,
            researchEngine = researchEngine,
            sourceVerifier = sourceVerifier,
            consensusEngine = consensusEngine,
            critic = critic,
            verifier = verifier,
            reasoningProvider = reasoningProvider,
            stageTwoEngine = stageTwoEngine,
            stageThreeEngine = stageThreeEngine,
            evidenceQualityEngine = evidenceQualityEngine,
            claimVerificationEngine = claimVerificationEngine,
            confidenceCalibrationEngine = confidenceCalibrationEngine,
            canonicalEvidenceQuality = canonicalEvidenceQuality,
            evidenceIntake = evidenceIntake,
            findingToCandidateConverter = findingToCandidateConverter,
            verificationLayer = verificationLayer,
            hierarchy = hierarchy,
            decisionCouncil = decisionCouncil,
            directionEngine = directionEngine,
            roleOpinionEngine = roleOpinionEngine
        )

        var executionContext = ContextEnvelope(
            sessionId = session.sessionId,
            taskId = "root",
            values = mapOf("intent" to plan.intent.name),
            provenance = listOf("planner"),
            artifacts = mapOf("runtime" to runtime)
        )
        val orchestrationResults = mutableListOf<OrchestrationTaskResult>()
        var blocked = false

        for (task in orderedTasks) {
            val completed = session.orchestrationState().completedTasks
            require(task.dependencies.all(completed::contains)) {
                "Task dependency not completed: " + task.id
            }

            val context = executionContext.scoped(
                task.id,
                mapOf("taskKind" to task.kind.name)
            )
            val selection = engineSelector.select(task)
            val taskState = when (task.kind) {
                AmarTaskKind.NORMALIZE, AmarTaskKind.UNDERSTAND, AmarTaskKind.CONTEXT, AmarTaskKind.CONSTRAINT -> AgentTaskState.UNDERSTANDING
                AmarTaskKind.EVIDENCE -> AgentTaskState.RESEARCHING
                AmarTaskKind.REASON -> AgentTaskState.REASONING
                AmarTaskKind.CHALLENGE -> AgentTaskState.REASONING
                AmarTaskKind.VALIDATE -> AgentTaskState.VERIFYING
                AmarTaskKind.RESPONSE, AmarTaskKind.AUDIT -> AgentTaskState.RESPONDING
            }
            emit(taskState, "تنفيذ task=" + task.id)
            session.record(
                AmarAgentStage.TASK_STATE,
                "TASK_EXECUTE=" + task.id + ";executor=" + selection.executor::class.simpleName + ";context=" + context.taskId
            )

            var executor: TaskExecutor = selection.executor
            var attempt = 0
            var completedByExecutor = false
            while (!completedByExecutor) {
                attempt += 1
                try {
                    val result = executor.execute(task, context)
                    if (!result.failed) {
                        orchestrationResults += OrchestrationTaskResult(
                            taskId = task.id,
                            status = result.status,
                            value = result.value,
                            provenance = result.provenance,
                            conflicts = result.conflicts
                        )
                        executionContext = executionContext
                            .withArtifact(task.kind.name.lowercase(), result.value)
                            .withArtifact(task.id, result.value)
                        session.markTaskCompleted(task.id)
                        completedByExecutor = true
                        continue
                    }

                    val retryAvailable = attempt < MAX_RETRIES
                    val fallbackAvailable = registry.hasFallback(task.kind)
                    val decision = failureRouter.route(
                        IllegalStateException("Executor returned " + result.status + " for " + task.id),
                        retryAvailable = retryAvailable,
                        fallbackAvailable = fallbackAvailable
                    )
                    when (decision.route) {
                        FailureRoute.RETRY -> {
                            session.transitionOrchestration(
                                OrchestrationState.RECOVERING,
                                taskId = task.id,
                                reason = decision.reason
                            )
                            session.transitionOrchestration(OrchestrationState.RUNNING)
                            session.record(AmarAgentStage.TASK_STATE, "TASK_RETRY=" + task.id + ";attempt=" + attempt)
                        }
                        FailureRoute.FALLBACK -> {
                            executor = registry.getFallback(task.kind)
                                ?: error("Fallback route selected without a registered fallback")
                            session.transitionOrchestration(
                                OrchestrationState.RECOVERING,
                                taskId = task.id,
                                reason = decision.reason
                            )
                            session.transitionOrchestration(OrchestrationState.RUNNING)
                            session.record(AmarAgentStage.TASK_STATE, "TASK_FALLBACK=" + task.id)
                        }
                        FailureRoute.BLOCK, FailureRoute.TERMINATE -> {
                            orchestrationResults += OrchestrationTaskResult(
                                taskId = task.id,
                                status = TaskResultStatus.BLOCKED,
                                value = null,
                                provenance = context.provenance,
                                conflicts = result.conflicts + decision.reason
                            )
                            session.transitionOrchestration(
                                OrchestrationState.BLOCKED,
                                taskId = task.id,
                                reason = decision.reason
                            )
                            blocked = true
                            completedByExecutor = true
                        }
                    }
                } catch (failure: Throwable) {
                    val retryAvailable = attempt < MAX_RETRIES
                    val fallbackAvailable = registry.hasFallback(task.kind)
                    val decision = failureRouter.route(
                        failure,
                        retryAvailable = retryAvailable,
                        fallbackAvailable = fallbackAvailable
                    )
                    when (decision.route) {
                        FailureRoute.RETRY -> {
                            session.transitionOrchestration(
                                OrchestrationState.RECOVERING,
                                taskId = task.id,
                                reason = decision.reason
                            )
                            session.transitionOrchestration(OrchestrationState.RUNNING)
                            session.record(AmarAgentStage.TASK_STATE, "TASK_RETRY=" + task.id + ";attempt=" + attempt)
                        }
                        FailureRoute.FALLBACK -> {
                            executor = registry.getFallback(task.kind)
                                ?: error("Fallback route selected without a registered fallback")
                            session.transitionOrchestration(
                                OrchestrationState.RECOVERING,
                                taskId = task.id,
                                reason = decision.reason
                            )
                            session.transitionOrchestration(OrchestrationState.RUNNING)
                            session.record(AmarAgentStage.TASK_STATE, "TASK_FALLBACK=" + task.id)
                        }
                        FailureRoute.BLOCK, FailureRoute.TERMINATE -> {
                            orchestrationResults += OrchestrationTaskResult(
                                taskId = task.id,
                                status = TaskResultStatus.BLOCKED,
                                value = null,
                                provenance = context.provenance,
                                conflicts = listOf(
                                    failure.message ?: failure::class.simpleName.orEmpty(),
                                    decision.reason
                                )
                            )
                            session.transitionOrchestration(
                                OrchestrationState.BLOCKED,
                                taskId = task.id,
                                reason = decision.reason
                            )
                            blocked = true
                            completedByExecutor = true
                        }
                    }
                }
            }

            if (blocked) break
        }

        val orchestrationResult = resultAggregator.aggregate(orchestrationResults)
        val allTasksCompleted = session.orchestrationState().completedTasks.containsAll(orderedTasks.map { it.id })
        if (!blocked && allTasksCompleted && orchestrationResult.status == TaskResultStatus.SUCCESS) {
            session.transitionOrchestration(OrchestrationState.COMPLETED)
        } else if (!blocked) {
            session.transitionOrchestration(
                OrchestrationState.BLOCKED,
                reason = "task_execution_not_completed"
            )
        }

        val audit = executionContext.artifact("audit") as? AuditTaskArtifact
        val evidence = audit?.evidence ?: (executionContext.artifact("evidence") as? EvidenceTaskArtifact)
        val response = audit?.response ?: AmarAgentResponse(
            answer = "لم يتم اعتماد الإجابة بعد. لم تكتمل سلسلة التنفيذ والتحقق.",
            status = AmarAgentResponse.Status.ERROR
        )
        val critique = audit?.critique ?: AmarCritique(
            accepted = false,
            issues = listOf("audit_not_reached"),
            recommendation = "BLOCK",
            score = 0.0
        )
        val finalVerification = audit?.finalVerification ?: AmarDecisionVerification(
            approved = false,
            issues = listOf("audit_not_reached"),
            evidenceConfidence = 0.0
        )

        return AmarAgentRunResult(
            response = response,
            plan = plan,
            orchestrationState = session.orchestrationState(),
            orchestrationResult = orchestrationResult,
            research = evidence?.report,
            sourceVerification = evidence?.sourceVerification,
            consensus = evidence?.consensus,
            critique = critique,
            finalVerification = finalVerification,
            stageTwo = audit?.stageTwo,
            stageThree = audit?.stageThree,
            hardening = audit?.hardening,
            canonicalEvidenceCertification = audit?.canonicalEvidenceCertification,
            sessionEvents = session.events()
        )
    }

    private companion object {
        const val MAX_RETRIES = 2
    }
}

data class AmarAgentRunResult(
    val response: AmarAgentResponse,
    val plan: AmarAgentPlan,
    val orchestrationState: OrchestrationStateSnapshot,
    val orchestrationResult: AggregatedOrchestrationResult,
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
