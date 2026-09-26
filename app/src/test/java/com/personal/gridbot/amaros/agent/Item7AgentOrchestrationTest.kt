package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class Item7AgentOrchestrationTest {
    @Test fun engine_registry_resolves_real_executor_for_every_task_kind() {
        val registry = defaultStage11EngineRegistry()
        val selector = EngineSelector(registry)
        AmarTaskKind.values().forEach { kind ->
            val selection = selector.select(AmarTaskUnit(kind.name, kind, "output", emptyList()))
            assertTrue(selection.executor is TaskExecutor)
        }
        assertEquals(AmarTaskKind.values().toSet(), registry.kinds())
    }

    @Test fun execution_order_respects_dependencies() {
        val tasks = listOf(
            AmarTaskUnit("b", AmarTaskKind.REASON, "b", listOf("a")),
            AmarTaskUnit("a", AmarTaskKind.CONTEXT, "a", emptyList()),
            AmarTaskUnit("c", AmarTaskKind.AUDIT, "c", listOf("b"))
        )
        val executed = OrchestrationDependencyGraph(tasks).topologicalOrder().map { it.id }
        assertEquals(listOf("a", "b", "c"), executed)
    }

    @Test fun context_envelope_propagates_parent_and_typed_artifacts_to_child_task() {
        val root = ContextEnvelope(
            "session",
            "root",
            mapOf("intent" to "RESEARCH"),
            artifacts = mapOf("runtime" to "runtime")
        )
        val parent = root.scoped("parent", mapOf("source" to "research")).withArtifact("evidence", "real")
        val child = parent.scoped("child", mapOf("phase" to "verify"))
        assertEquals("research", child.values["source"])
        assertEquals("verify", child.values["phase"])
        assertEquals("real", child.artifact("evidence"))
        assertEquals(listOf("root", "parent"), child.provenance)
    }

    @Test fun failure_router_supports_all_fail_closed_routes() {
        val router = FailureRouter()
        assertEquals(FailureRoute.RETRY, router.route(IllegalStateException("x"), true, true).route)
        assertEquals(FailureRoute.FALLBACK, router.route(IllegalStateException("x"), false, true).route)
        assertEquals(FailureRoute.TERMINATE, router.route(IllegalStateException("x"), false, false).route)
        assertEquals(FailureRoute.BLOCK, router.route(IllegalArgumentException("bad"), true, true).route)
        assertEquals(FailureRoute.BLOCK, router.route(IllegalStateException(), true, true).route)
    }

    @Test fun result_aggregation_preserves_real_typed_results() {
        val report = ResearchReport(emptyList())
        val result = ResultAggregator().aggregate(
            listOf(
                OrchestrationTaskResult("evidence", TaskResultStatus.SUCCESS, report, listOf("researchEngine")),
                OrchestrationTaskResult("response", TaskResultStatus.SUCCESS, AmarAgentResponse("answer"), listOf("reasoningProvider"))
            )
        )
        assertEquals(TaskResultStatus.SUCCESS, result.status)
        assertTrue(result.results[0].value is ResearchReport)
        assertTrue(result.results[1].value is AmarAgentResponse)
        assertEquals(setOf("researchEngine", "reasoningProvider"), result.provenance.toSet())
    }

    @Test fun planner_builds_typed_dependency_graph() {
        val plan = AmarAgentPlanner().plan(AmarAgentRequest("ابحث عن مصادر حول موضوع الذهب"), emptyList())
        assertEquals(plan.tasks.size, plan.tasks.map { it.id }.distinct().size)
        assertTrue(plan.tasks.any { it.kind == AmarTaskKind.EVIDENCE })
        val taskIds = plan.tasks.map { it.id }.toSet()
        assertTrue(plan.tasks.all { task -> task.dependencies.all { it in taskIds } })
        assertEquals(
            plan.tasks.map { it.id },
            OrchestrationDependencyGraph(plan.tasks).topologicalOrder().map { it.id }
        )
    }

    @Test fun dependency_graph_blocks_missing_dependency_and_cycles() {
        val missing = AmarTaskUnit("a", AmarTaskKind.CONTEXT, "x", listOf("missing"))
        try {
            OrchestrationDependencyGraph(listOf(missing))
            throw AssertionError("missing dependency accepted")
        } catch (_: IllegalArgumentException) { }
        val a = AmarTaskUnit("a", AmarTaskKind.CONTEXT, "x", listOf("b"))
        val b = AmarTaskUnit("b", AmarTaskKind.CONTEXT, "x", listOf("a"))
        try {
            OrchestrationDependencyGraph(listOf(a, b)).topologicalOrder()
            throw AssertionError("cycle accepted")
        } catch (_: IllegalStateException) { }
    }

    @Test fun state_machine_rejects_terminal_revival() {
        val reducer = OrchestrationStateReducer()
        val completed = reducer.transition(
            reducer.transition(OrchestrationStateSnapshot(), OrchestrationState.PLANNED),
            OrchestrationState.RUNNING
        ).let { reducer.transition(it, OrchestrationState.COMPLETED) }
        assertEquals(OrchestrationState.COMPLETED, completed.state)
        try {
            reducer.transition(completed, OrchestrationState.RUNNING)
            throw AssertionError("terminal state revived")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun state_completed_tasks_are_single_source_of_truth() {
        val reducer = OrchestrationStateReducer()
        var state = reducer.transition(OrchestrationStateSnapshot(), OrchestrationState.PLANNED)
        state = reducer.transition(state, OrchestrationState.RUNNING)
        state = reducer.markCompleted(state, "a")
        state = reducer.markCompleted(state, "b")
        assertEquals(setOf("a", "b"), state.completedTasks)
    }

    @Test fun real_integration_executes_research_and_reasoning_through_task_executors() = runBlocking {
        val researchCalls = AtomicInteger(0)
        val reasoningCalls = AtomicInteger(0)

        val research = object : AmarResearchEngine {
            override suspend fun research(request: ResearchRequest): ResearchReport {
                researchCalls.incrementAndGet()
                return ResearchReport(
                    findings = listOf(
                        ResearchFinding(
                            sourceTitle = "Source A",
                            sourceUri = "https://a.example/source",
                            evidence = "The documented subject has a measured property.",
                            authority = Authority.OFFICIAL,
                            publisher = "source-a"
                        ),
                        ResearchFinding(
                            sourceTitle = "Source B",
                            sourceUri = "https://b.example/source",
                            evidence = "An independent source documents the same property.",
                            authority = Authority.REPUTABLE,
                            publisher = "source-b"
                        )
                    )
                )
            }
        }

        val reasoning = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext): AmarAgentResponse {
                reasoningCalls.incrementAndGet()
                return AmarAgentResponse("الإجابة مبنية على الأدلة المتاحة.")
            }
        }

        val orchestrator = AmarAgentOrchestrator(
            planner = AmarAgentPlanner(),
            researchEngine = research,
            sourceVerifier = AmarSourceVerifier(),
            consensusEngine = AmarAgentEvidenceConsensus(),
            critic = AmarAgentCritic(),
            verifier = AmarAgentVerifier(),
            reasoningProvider = reasoning
        )

        val result = orchestrator.run(
            AmarAgentRequest("ابحث عن مصادر عن موضوع محدد", requireCrossValidation = true),
            emptyList()
        )

        assertEquals(1, researchCalls.get())
        assertEquals(1, reasoningCalls.get())
        assertEquals(OrchestrationState.COMPLETED, result.orchestrationState.state)
        assertEquals(TaskResultStatus.SUCCESS, result.orchestrationResult.status)
        assertTrue(result.orchestrationResult.results.any { it.value is EvidenceTaskArtifact })
        assertTrue(result.orchestrationResult.results.any { it.value is ReasonTaskArtifact })
        assertTrue(result.orchestrationResult.results.any { it.value is AmarAgentResponse })
        assertTrue(result.orchestrationResult.results.any { it.value is AuditTaskArtifact })
        assertEquals("الإجابة مبنية على الأدلة المتاحة.", result.response.answer)
    }
    @Test fun debug_local_agent_output() = runBlocking {
        val orchestrator = AmarAgentOrchestrator(
            planner = AmarAgentPlanner(),
            researchEngine = object : AmarResearchEngine {
                override suspend fun research(request: ResearchRequest) = ResearchReport(emptyList())
            },
            sourceVerifier = AmarSourceVerifier(),
            consensusEngine = AmarAgentEvidenceConsensus(),
            critic = AmarAgentCritic(),
            verifier = AmarAgentVerifier(),
            reasoningProvider = AmarLocalReasoning()
        )
        val result = orchestrator.run(AmarAgentRequest("Hello"), emptyList())
        throw AssertionError(
            "STATE=" + result.orchestrationState +
                ";RESULT=" + result.orchestrationResult.status +
                ";ANSWER=" + result.response.answer +
                ";EVENTS=" + result.sessionEvents.joinToString(" || ") { it.stage.name + ":" + it.message }
        )
    }e com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class Item7AgentOrchestrationTest {
    @Test fun engine_registry_resolves_real_executor_for_every_task_kind() {
        val registry = defaultStage11EngineRegistry()
        val selector = EngineSelector(registry)
        AmarTaskKind.values().forEach { kind ->
            val selection = selector.select(AmarTaskUnit(kind.name, kind, "output", emptyList()))
            assertTrue(selection.executor is TaskExecutor)
        }
        assertEquals(AmarTaskKind.values().toSet(), registry.kinds())
    }

    @Test fun execution_order_respects_dependencies() {
        val tasks = listOf(
            AmarTaskUnit("b", AmarTaskKind.REASON, "b", listOf("a")),
            AmarTaskUnit("a", AmarTaskKind.CONTEXT, "a", emptyList()),
            AmarTaskUnit("c", AmarTaskKind.AUDIT, "c", listOf("b"))
        )
        val executed = OrchestrationDependencyGraph(tasks).topologicalOrder().map { it.id }
        assertEquals(listOf("a", "b", "c"), executed)
    }

    @Test fun context_envelope_propagates_parent_and_typed_artifacts_to_child_task() {
        val root = ContextEnvelope(
            "session",
            "root",
            mapOf("intent" to "RESEARCH"),
            artifacts = mapOf("runtime" to "runtime")
        )
        val parent = root.scoped("parent", mapOf("source" to "research")).withArtifact("evidence", "real")
        val child = parent.scoped("child", mapOf("phase" to "verify"))
        assertEquals("research", child.values["source"])
        assertEquals("verify", child.values["phase"])
        assertEquals("real", child.artifact("evidence"))
        assertEquals(listOf("root", "parent"), child.provenance)
    }

    @Test fun failure_router_supports_all_fail_closed_routes() {
        val router = FailureRouter()
        assertEquals(FailureRoute.RETRY, router.route(IllegalStateException("x"), true, true).route)
        assertEquals(FailureRoute.FALLBACK, router.route(IllegalStateException("x"), false, true).route)
        assertEquals(FailureRoute.TERMINATE, router.route(IllegalStateException("x"), false, false).route)
        assertEquals(FailureRoute.BLOCK, router.route(IllegalArgumentException("bad"), true, true).route)
        assertEquals(FailureRoute.BLOCK, router.route(IllegalStateException(), true, true).route)
    }

    @Test fun result_aggregation_preserves_real_typed_results() {
        val report = ResearchReport(emptyList())
        val result = ResultAggregator().aggregate(
            listOf(
                OrchestrationTaskResult("evidence", TaskResultStatus.SUCCESS, report, listOf("researchEngine")),
                OrchestrationTaskResult("response", TaskResultStatus.SUCCESS, AmarAgentResponse("answer"), listOf("reasoningProvider"))
            )
        )
        assertEquals(TaskResultStatus.SUCCESS, result.status)
        assertTrue(result.results[0].value is ResearchReport)
        assertTrue(result.results[1].value is AmarAgentResponse)
        assertEquals(setOf("researchEngine", "reasoningProvider"), result.provenance.toSet())
    }

    @Test fun planner_builds_typed_dependency_graph() {
        val plan = AmarAgentPlanner().plan(AmarAgentRequest("ابحث عن مصادر حول موضوع الذهب"), emptyList())
        assertEquals(plan.tasks.size, plan.tasks.map { it.id }.distinct().size)
        assertTrue(plan.tasks.any { it.kind == AmarTaskKind.EVIDENCE })
        val taskIds = plan.tasks.map { it.id }.toSet()
        assertTrue(plan.tasks.all { task -> task.dependencies.all { it in taskIds } })
        assertEquals(
            plan.tasks.map { it.id },
            OrchestrationDependencyGraph(plan.tasks).topologicalOrder().map { it.id }
        )
    }

    @Test fun dependency_graph_blocks_missing_dependency_and_cycles() {
        val missing = AmarTaskUnit("a", AmarTaskKind.CONTEXT, "x", listOf("missing"))
        try {
            OrchestrationDependencyGraph(listOf(missing))
            throw AssertionError("missing dependency accepted")
        } catch (_: IllegalArgumentException) { }
        val a = AmarTaskUnit("a", AmarTaskKind.CONTEXT, "x", listOf("b"))
        val b = AmarTaskUnit("b", AmarTaskKind.CONTEXT, "x", listOf("a"))
        try {
            OrchestrationDependencyGraph(listOf(a, b)).topologicalOrder()
            throw AssertionError("cycle accepted")
        } catch (_: IllegalStateException) { }
    }

    @Test fun state_machine_rejects_terminal_revival() {
        val reducer = OrchestrationStateReducer()
        val completed = reducer.transition(
            reducer.transition(OrchestrationStateSnapshot(), OrchestrationState.PLANNED),
            OrchestrationState.RUNNING
        ).let { reducer.transition(it, OrchestrationState.COMPLETED) }
        assertEquals(OrchestrationState.COMPLETED, completed.state)
        try {
            reducer.transition(completed, OrchestrationState.RUNNING)
            throw AssertionError("terminal state revived")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun state_completed_tasks_are_single_source_of_truth() {
        val reducer = OrchestrationStateReducer()
        var state = reducer.transition(OrchestrationStateSnapshot(), OrchestrationState.PLANNED)
        state = reducer.transition(state, OrchestrationState.RUNNING)
        state = reducer.markCompleted(state, "a")
        state = reducer.markCompleted(state, "b")
        assertEquals(setOf("a", "b"), state.completedTasks)
    }

    @Test fun real_integration_executes_research_and_reasoning_through_task_executors() = runBlocking {
        val researchCalls = AtomicInteger(0)
        val reasoningCalls = AtomicInteger(0)

        val research = object : AmarResearchEngine {
            override suspend fun research(request: ResearchRequest): ResearchReport {
                researchCalls.incrementAndGet()
                return ResearchReport(
                    findings = listOf(
                        ResearchFinding(
                            sourceTitle = "Source A",
                            sourceUri = "https://a.example/source",
                            evidence = "The documented subject has a measured property.",
                            authority = Authority.OFFICIAL,
                            publisher = "source-a"
                        ),
                        ResearchFinding(
                            sourceTitle = "Source B",
                            sourceUri = "https://b.example/source",
                            evidence = "An independent source documents the same property.",
                            authority = Authority.REPUTABLE,
                            publisher = "source-b"
                        )
                    )
                )
            }
        }

        val reasoning = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext): AmarAgentResponse {
                reasoningCalls.incrementAndGet()
                return AmarAgentResponse("الإجابة مبنية على الأدلة المتاحة.")
            }
        }

        val orchestrator = AmarAgentOrchestrator(
            planner = AmarAgentPlanner(),
            researchEngine = research,
            sourceVerifier = AmarSourceVerifier(),
            consensusEngine = AmarAgentEvidenceConsensus(),
            critic = AmarAgentCritic(),
            verifier = AmarAgentVerifier(),
            reasoningProvider = reasoning
        )

        val result = orchestrator.run(
            AmarAgentRequest("ابحث عن مصادر عن موضوع محدد", requireCrossValidation = true),
            emptyList()
        )

        assertEquals(1, researchCalls.get())
        assertEquals(1, reasoningCalls.get())
        assertEquals(OrchestrationState.COMPLETED, result.orchestrationState.state)
        assertEquals(TaskResultStatus.SUCCESS, result.orchestrationResult.status)
        assertTrue(result.orchestrationResult.results.any { it.value is EvidenceTaskArtifact })
        assertTrue(result.orchestrationResult.results.any { it.value is ReasonTaskArtifact })
        assertTrue(result.orchestrationResult.results.any { it.value is AmarAgentResponse })
        assertTrue(result.orchestrationResult.results.any { it.value is AuditTaskArtifact })
        assertEquals("الإجابة مبنية على الأدلة المتاحة.", result.response.answer)
    }
    @Test fun debug_local_agent_output() = runBlocking {
        val engine = com.personal.gridbot.amaros.ai.AmarAiAgentEngine()
        val result = engine.ask("", "", "Hello")
        throw AssertionError("ACTUAL=" + result.answer)
    }

}
