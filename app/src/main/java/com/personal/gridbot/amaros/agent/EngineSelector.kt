package com.personal.gridbot.amaros.agent

class EngineSelector(private val registry: EngineRegistry) {
    fun select(task: AmarTaskUnit): EngineSelection =
        EngineSelection(task.id, registry.get(task.kind))
}
