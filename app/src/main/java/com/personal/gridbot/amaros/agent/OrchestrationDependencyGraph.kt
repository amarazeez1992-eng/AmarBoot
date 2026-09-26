package com.personal.gridbot.amaros.agent

class OrchestrationDependencyGraph(tasks: List<AmarTaskUnit>) {
    private val byId = tasks.associateBy { it.id }
    init {
        require(byId.size == tasks.size) { "Duplicate task id" }
        tasks.forEach { task -> task.dependencies.forEach { dependency -> require(dependency in byId) { "Missing dependency: " + dependency } } }
    }
    fun topologicalOrder(): List<AmarTaskUnit> {
        val remaining = byId.toMutableMap()
        val ordered = mutableListOf<AmarTaskUnit>()
        while (remaining.isNotEmpty()) {
            val ready = remaining.values.filter { task -> task.dependencies.all { dependency -> dependency !in remaining } }.sortedBy { it.id }
            if (ready.isEmpty()) error("Dependency cycle detected")
            ready.forEach { task -> ordered += task; remaining.remove(task.id) }
        }
        return ordered
    }
    fun ready(completed: Set<String>): List<AmarTaskUnit> =
        byId.values.filter { it.id !in completed && it.dependencies.all(completed::contains) }.sortedBy { it.id }
}
