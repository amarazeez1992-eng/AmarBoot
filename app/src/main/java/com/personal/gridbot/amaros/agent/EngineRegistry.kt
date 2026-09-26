package com.personal.gridbot.amaros.agent

data class EngineSelection(
    val taskId: String,
    val executor: TaskExecutor
)

class EngineRegistry {
    private val executors = linkedMapOf<AmarTaskKind, TaskExecutor>()
    private val fallbacks = linkedMapOf<AmarTaskKind, TaskExecutor>()

    fun register(kind: AmarTaskKind, executor: TaskExecutor): EngineRegistry {
        require(kind !in executors) { "Executor already registered for task kind: " + kind }
        executors[kind] = executor
        return this
    }

    fun registerFallback(kind: AmarTaskKind, executor: TaskExecutor): EngineRegistry {
        require(kind in executors) { "Primary executor must be registered before fallback: " + kind }
        require(kind !in fallbacks) { "Fallback already registered for task kind: " + kind }
        fallbacks[kind] = executor
        return this
    }

    fun get(kind: AmarTaskKind): TaskExecutor =
        executors[kind] ?: error("No executor registered for task kind: " + kind)

    fun getFallback(kind: AmarTaskKind): TaskExecutor? = fallbacks[kind]

    fun hasFallback(kind: AmarTaskKind): Boolean = fallbacks.containsKey(kind)

    fun kinds(): Set<AmarTaskKind> = executors.keys.toSet()
}
