package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Item7AgentOrchestrationTest {
    @Test fun engine_selector_resolves_every_task_kind() {
        val selector = EngineSelector()
        AmarTaskKind.values().forEach { kind ->
            val selection = selector.select(AmarTaskUnit(kind.name, kind, "output", emptyList()))
            assertTrue(selection.engineId.isNotBlank())
        }
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

    @Test fun context_envelope_propagates_parent_to_child_task() {
        val root = ContextEnvelope("session", "root", mapOf("intent" to "RESEARCH"))
        val parent = root.scoped("parent", mapOf("source" to "research"))
        val child = parent.scoped("child", mapOf("phase" to "verify"))
        assertEquals("research", child.values["source"])
        assertEquals("verify", child.values["phase"])
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

    @Test fun result_aggregation_blocks_when_any_task_is_blocked() {
        val result = ResultAggregator().aggregate(listOf(
            OrchestrationTaskResult("a", TaskResultStatus.SUCCESS, "ok"),
            OrchestrationTaskResult("b", TaskResultStatus.BLOCKED, null, conflicts = listOf("blocked"))
        ))
        assertEquals(TaskResultStatus.BLOCKED, result.status)
        assertTrue(result.conflicts.contains("blocked"))
    }

    @Test fun planner_builds_typed_dependency_graph() {
        val plan = AmarAgentPlanner().plan(AmarAgentRequest("كيف أختبر استراتيجية ذهب؟"), emptyList())
        assertEquals(plan.tasks.size, plan.tasks.map { it.id }.distinct().size)
        assertTrue(plan.tasks.any { it.kind == AmarTaskKind.EVIDENCE })
        assertEquals(plan.tasks.map { it.id }.toSet().size, plan.tasks.flatMap { it.dependencies }.count { it in plan.tasks.map { t -> t.id } } + 1)
        assertEquals(plan.tasks.map { it.id }, OrchestrationDependencyGraph(plan.tasks).topologicalOrder().map { it.id })
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

    @Test fun failure_router_fails_closed_without_recovery() {
        val result = FailureRouter().route(IllegalStateException("provider failed"), false, false)
        assertEquals(FailureRoute.TERMINATE, result.route)
    }

    @Test fun result_aggregation_preserves_partial_conflict_and_provenance() {
        val result = ResultAggregator().aggregate(listOf(
            OrchestrationTaskResult("a", TaskResultStatus.SUCCESS, "ok", listOf("s1")),
            OrchestrationTaskResult("b", TaskResultStatus.PARTIAL, "partial", listOf("s2"), listOf("c1"))
        ))
        assertEquals(TaskResultStatus.PARTIAL, result.status)
        assertEquals(setOf("s1", "s2"), result.provenance.toSet())
        assertEquals(listOf("c1"), result.conflicts)
    }

    @Test fun context_envelope_preserves_provenance_across_scope() {
        val root = ContextEnvelope("session", "a", mapOf("x" to "1"))
        val child = root.scoped("b", mapOf("y" to "2"))
        assertEquals("1", child.values["x"])
        assertEquals("2", child.values["y"])
        assertEquals(listOf("a"), child.provenance)
    }

    @Test fun state_machine_rejects_terminal_revival() {
        val reducer = OrchestrationStateReducer()
        val completed = reducer.transition(
            reducer.transition(OrchestrationStateSnapshot(), OrchestrationState.PLANNED),
            OrchestrationState.RUNNING
        ).let { reducer.transition(it, OrchestrationState.COMPLETED, taskId = "done") }
        assertEquals(OrchestrationState.COMPLETED, completed.state)
        try {
            reducer.transition(completed, OrchestrationState.RUNNING)
            throw AssertionError("terminal state revived")
        } catch (_: IllegalArgumentException) { }
    }
}
