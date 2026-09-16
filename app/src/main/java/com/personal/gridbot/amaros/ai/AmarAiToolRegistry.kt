package com.personal.gridbot.amaros.ai

/** Explicit authority classification for tools callable by the AI Copilot. */
object AmarAiToolRegistry {
    enum class Authority { READ_ONLY, DRAFT_ONLY }

    data class ToolSpec(
        val name: String,
        val authority: Authority,
        val requiresContext: Boolean = false
    )

    private val specs = listOf(
        "inspect_app" to Authority.READ_ONLY,
        "engine_market" to Authority.READ_ONLY,
        "tracking" to Authority.READ_ONLY,
        "candle" to Authority.READ_ONLY,
        "analyze_market" to Authority.READ_ONLY,
        "file_analyze" to Authority.READ_ONLY,
        "code_analyze" to Authority.READ_ONLY,
        "file_compare" to Authority.READ_ONLY,
        "research_external" to Authority.READ_ONLY,
        "multi_source_research" to Authority.READ_ONLY,
        "trading_library_search" to Authority.READ_ONLY,
        "library_search" to Authority.READ_ONLY,
        "bot_discovery" to Authority.READ_ONLY,
        "inspect_bot" to Authority.READ_ONLY,
        "strategy_quality" to Authority.READ_ONLY,
        "test_strategy" to Authority.READ_ONLY,
        "validate_results" to Authority.READ_ONLY,
        "precision_audit" to Authority.READ_ONLY,
        "uncertainty_audit" to Authority.READ_ONLY,
        "self_audit" to Authority.READ_ONLY,
        "evolution_gate" to Authority.READ_ONLY,
        "champion_challenger" to Authority.READ_ONLY,
        "counterfactual" to Authority.READ_ONLY,
        "strategy_load" to Authority.READ_ONLY,
        "approval_proposal" to Authority.DRAFT_ONLY,
        "strategy_save" to Authority.DRAFT_ONLY
    ).associate { (name, authority) ->
        val needsContext = name in setOf("file_analyze", "code_analyze", "file_compare")
        name to ToolSpec(name, authority, needsContext || authority == Authority.DRAFT_ONLY)
    }

    fun resolve(name: String): ToolSpec? = specs[name.trim()]

    fun isKnown(name: String): Boolean = resolve(name) != null

    fun isExecutionCapable(name: String): Boolean = false

    fun all(): List<ToolSpec> = specs.values.sortedBy { it.name }
}
