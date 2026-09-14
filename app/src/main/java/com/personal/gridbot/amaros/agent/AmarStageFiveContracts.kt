package com.personal.gridbot.amaros.agent

/** Stage 5: deterministic strategy specification and bounded research-workforce contracts. */
enum class AmarStrategyDirection { LONG, SHORT, BOTH, UNDEFINED }

enum class AmarStrategyRole {
    STRATEGY_ANALYST,
    MARKET_ANALYST,
    QUANTITATIVE_REVIEWER,
    RISK_REVIEWER,
    ADVERSARIAL_REVIEWER,
    DECISION_CONFIRMER
}

data class AmarStrategyRule(val id: String, val expression: String) {
    init { require(id.isNotBlank()); require(expression.isNotBlank()) }
}

data class AmarStrategySpecification(
    val strategyId: String,
    val version: Int,
    val symbol: String,
    val timeframe: String,
    val direction: AmarStrategyDirection,
    val entryRules: List<AmarStrategyRule>,
    val exitRules: List<AmarStrategyRule>,
    val riskRules: List<AmarStrategyRule>,
    val assumptions: List<String>,
    val provenance: List<String>,
    val fingerprint: String
) {
    init {
        require(strategyId.isNotBlank())
        require(version >= 1)
        require(symbol.isNotBlank())
        require(timeframe.isNotBlank())
        require(direction != AmarStrategyDirection.UNDEFINED)
        require(entryRules.isNotEmpty())
        require(fingerprint.length == 64)
    }
}

data class AmarStrategyCompileRequest(
    val strategyId: String,
    val naturalLanguageIdea: String,
    val symbol: String = "UNSPECIFIED",
    val timeframe: String = "UNSPECIFIED",
    val sourceReferences: List<String> = emptyList()
) {
    init {
        require(strategyId.isNotBlank())
        require(naturalLanguageIdea.isNotBlank())
        require(symbol.isNotBlank())
        require(timeframe.isNotBlank())
    }
}

data class AmarRoleOpinion(
    val role: AmarStrategyRole,
    val approved: Boolean,
    val confidence: Double,
    val findings: List<String>
) {
    init {
        require(confidence.isFinite() && confidence in 0.0..1.0)
        require(findings.isNotEmpty())
    }
}

data class AmarStrategyConflict(val roles: Set<AmarStrategyRole>, val reason: String) {
    init { require(roles.size >= 2); require(reason.isNotBlank()) }
}

data class AmarStrategyCompileResult(
    val specification: AmarStrategySpecification?,
    val opinions: List<AmarRoleOpinion>,
    val conflicts: List<AmarStrategyConflict>,
    val approved: Boolean,
    val reasons: List<String>
) {
    init { require(opinions.map { it.role }.distinct().size == opinions.size) }
}
