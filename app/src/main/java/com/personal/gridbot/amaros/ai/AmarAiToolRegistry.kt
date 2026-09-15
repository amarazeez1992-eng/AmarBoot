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
        "code_analysis" to Authority.READ_ONLY,
        "code_generation" to Authority.DRAFT_ONLY,
        "code_repair" to Authority.DRAFT_ONLY,
        "code_refactor" to Authority.DRAFT_ONLY,
        "test_generation" to Authority.DRAFT_ONLY,
        "visual_idea_to_design" to Authority.DRAFT_ONLY,
        "visual_code_to_image" to Authority.DRAFT_ONLY,
        "visual_image_to_code" to Authority.DRAFT_ONLY,
        "visual_image_edit" to Authority.DRAFT_ONLY,
        "visual_high_res_generation" to Authority.DRAFT_ONLY,
        "visual_ui_redesign" to Authority.DRAFT_ONLY,
        "visual_library_research" to Authority.READ_ONLY,
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
        "strategy_save" to Authority.DRAFT_ONLY,
        "github_repo_search" to Authority.READ_ONLY,
        "github_repo_inspect" to Authority.READ_ONLY,
        "github_code_search" to Authority.READ_ONLY,
        "github_file_read" to Authority.READ_ONLY,
        "github_license_inspect" to Authority.READ_ONLY,
        "github_branch_create" to Authority.DRAFT_ONLY,
        "github_file_create" to Authority.DRAFT_ONLY,
        "github_file_update" to Authority.DRAFT_ONLY,
        "github_file_delete" to Authority.DRAFT_ONLY,
        "github_pull_request_create" to Authority.DRAFT_ONLY,
        "github_pull_request_merge" to Authority.DRAFT_ONLY
    ).associate { (name, authority) ->
        name to ToolSpec(name, authority, authority == Authority.DRAFT_ONLY)
    }

    fun resolve(name: String): ToolSpec? = specs[name.trim()]
    fun isKnown(name: String): Boolean = resolve(name) != null
    fun isExecutionCapable(name: String): Boolean = false
    fun all(): List<ToolSpec> = specs.values.sortedBy { it.name }
}
