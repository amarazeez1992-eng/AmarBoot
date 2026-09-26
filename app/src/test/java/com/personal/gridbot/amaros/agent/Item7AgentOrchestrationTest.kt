package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Item7AgentOrchestrationTest {
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
