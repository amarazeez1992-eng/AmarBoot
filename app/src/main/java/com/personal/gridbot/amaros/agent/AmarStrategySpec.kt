package com.personal.gridbot.amaros.agent

/** Versioned declarative strategy specification. It describes a strategy; it cannot execute one. */
data class AmarStrategySpec(
    val id: String,
    val version: Int,
    val name: String,
    val symbols: List<String>,
    val timeframes: List<String>,
    val entryRules: List<String>,
    val exitRules: List<String>,
    val riskRules: List<String>,
    val assumptions: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(id.isNotBlank())
        require(version > 0)
        require(name.isNotBlank())
        require(symbols.isNotEmpty())
        require(entryRules.isNotEmpty())
        require(exitRules.isNotEmpty())
        require(riskRules.isNotEmpty())
    }
}
