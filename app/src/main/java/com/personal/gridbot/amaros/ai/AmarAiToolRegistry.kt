package com.personal.gridbot.amaros.ai

class AmarAiToolRegistry {
    data class ToolSpec(val name: String, val authority: Authority, val draftOnly: Boolean)

    enum class Authority { DRAFT_ONLY, EXECUTION_CAPABLE }

    private val specs = linkedMapOf(
        "chart" to ToolSpec("chart", Authority.DRAFT_ONLY, true),
        "news" to ToolSpec("news", Authority.DRAFT_ONLY, true),
        "market" to ToolSpec("market", Authority.DRAFT_ONLY, true),
        "bot_lab" to ToolSpec("bot_lab", Authority.DRAFT_ONLY, true),
        "mt5" to ToolSpec("mt5", Authority.DRAFT_ONLY, true)
    )

    fun resolve(name: String): ToolSpec? = specs[name.trim()]

    fun isKnown(name: String): Boolean = resolve(name) != null

    fun isExecutionCapable(_name: String): Boolean = false

    fun all(): List<ToolSpec> = specs.values.sortedBy { it.name }
}
