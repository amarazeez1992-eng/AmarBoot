package com.personal.gridbot.amaros.agent

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

/** Deterministic, provider-neutral strategy compiler. It never owns execution authority. */
class AmarStageFiveCompiler {
    fun compile(request: AmarStrategyCompileRequest): AmarStrategyCompileResult {
        val normalized = normalize(request.naturalLanguageIdea)
        val direction = parseDirection(normalized)
        val timeframe = if (request.timeframe != "UNSPECIFIED") request.timeframe.uppercase(Locale.US) else parseTimeframe(normalized)
        val entry = parseEntryRules(normalized)
        val exit = parseExitRules(normalized)
        val risk = parseRiskRules(normalized)
        val assumptions = mutableListOf<String>()
        if (exit.isEmpty()) assumptions += "default_exit_on_opposite_signal"
        if (request.symbol == "UNSPECIFIED") assumptions += "symbol_not_bound_at_compile_time"
        if (timeframe == "UNSPECIFIED") assumptions += "timeframe_not_bound_at_compile_time"
        val actualExit = if (exit.isEmpty()) listOf(AmarStrategyRule("EXIT_DEFAULT", "close_on_opposite_signal")) else exit
        val provenance = request.sourceReferences.filter { it.isNotBlank() }.distinct().sorted()
        val spec = if (direction != AmarStrategyDirection.UNDEFINED && entry.isNotEmpty()) {
            val canonical = canonical(request.strategyId, 1, request.symbol.uppercase(Locale.US), timeframe, direction, entry, actualExit, risk, assumptions, provenance)
            AmarStrategySpecification(request.strategyId.trim(), 1, request.symbol.trim().uppercase(Locale.US), timeframe, direction, entry, actualExit, risk, assumptions, provenance, sha256(canonical))
        } else null
        val opinions = buildOpinions(spec, normalized, direction, entry, risk)
        val conflicts = detectConflicts(opinions)
        val approved = spec != null && conflicts.isEmpty() && opinions.all { it.approved }
        val reasons = buildList {
            if (spec == null) add("strategy_specification_incomplete")
            conflicts.forEach { add(it.reason) }
            opinions.filterNot { it.approved }.forEach { add("${it.role.name.lowercase(Locale.US)}_rejected") }
            if (approved) add("strategy_specification_approved")
        }.distinct()
        return AmarStrategyCompileResult(spec, opinions, conflicts, approved, reasons)
    }

    private fun buildOpinions(spec: AmarStrategySpecification?, normalized: String, direction: AmarStrategyDirection, entry: List<AmarStrategyRule>, risk: List<AmarStrategyRule>): List<AmarRoleOpinion> {
        val executionWords = listOf("execute", "open", "place order", "broker", "trade now", "نفذ", "افتح صفقة")
        val executionRequested = executionWords.any { normalized.contains(it) }
        return listOf(
            AmarRoleOpinion(AmarStrategyRole.STRATEGY_ANALYST, spec != null, if (spec != null) .95 else .25, listOf(if (spec != null) "strategy_rules_normalized" else "direction_or_entry_missing")),
            AmarRoleOpinion(AmarStrategyRole.MARKET_ANALYST, spec != null && direction != AmarStrategyDirection.UNDEFINED, if (spec != null) .90 else .30, listOf(if (spec != null) "market_context_fields_present" else "market_context_incomplete")),
            AmarRoleOpinion(AmarStrategyRole.QUANTITATIVE_REVIEWER, entry.isNotEmpty() && spec != null, if (entry.isNotEmpty() && spec != null) .90 else .25, listOf(if (entry.isNotEmpty()) "entry_is_explicit" else "entry_is_not_explicit")),
            AmarRoleOpinion(AmarStrategyRole.RISK_REVIEWER, risk.isNotEmpty(), if (risk.isNotEmpty()) .95 else .20, listOf(if (risk.isNotEmpty()) "risk_rule_present" else "risk_rule_missing")),
            AmarRoleOpinion(AmarStrategyRole.ADVERSARIAL_REVIEWER, !executionRequested, if (!executionRequested) .90 else .05, listOf(if (!executionRequested) "no_execution_authority_requested" else "execution_request_blocked_inside_compiler")),
            AmarRoleOpinion(AmarStrategyRole.DECISION_CONFIRMER, spec != null && risk.isNotEmpty() && !executionRequested, if (spec != null && risk.isNotEmpty() && !executionRequested) .92 else .10, listOf(if (spec != null && risk.isNotEmpty() && !executionRequested) "all_compile_gates_satisfied" else "compile_gates_not_satisfied"))
        )
    }

    private fun detectConflicts(opinions: List<AmarRoleOpinion>): List<AmarStrategyConflict> {
        val rejected = opinions.filterNot { it.approved }.map { it.role }.toSet()
        if (rejected.isEmpty()) return emptyList()
        val accepted = opinions.filter { it.approved }.map { it.role }.toSet()
        return if (accepted.isNotEmpty()) listOf(AmarStrategyConflict(accepted + rejected, "workforce_role_conflict")) else emptyList()
    }

    private fun parseDirection(text: String): AmarStrategyDirection {
        val long = listOf("buy", "long", "شراء", "لونغ", "صعود").any { text.contains(it) }
        val short = listOf("sell", "short", "بيع", "شورت", "هبوط").any { text.contains(it) }
        return when { long && short -> AmarStrategyDirection.BOTH; long -> AmarStrategyDirection.LONG; short -> AmarStrategyDirection.SHORT; else -> AmarStrategyDirection.UNDEFINED }
    }

    private fun parseTimeframe(text: String): String = Regex("\\b(M1|M3|M5|M15|M30|H1|H4|D1)\\b", RegexOption.IGNORE_CASE).find(text)?.value?.uppercase(Locale.US) ?: "UNSPECIFIED"

    private fun parseEntryRules(text: String): List<AmarStrategyRule> {
        val keys = listOf("when ", "if ", "buy ", "sell ", "long ", "short ", "above", "below", "cross", "فوق", "تحت", "عندما", "إذا", "شراء", "بيع")
        return if (keys.any { text.contains(it) }) listOf(AmarStrategyRule("ENTRY_1", text)) else emptyList()
    }

    private fun parseExitRules(text: String): List<AmarStrategyRule> {
        val keys = listOf("take profit", "tp", "stop", "exit", "close", "هدف", "وقف", "اغلاق", "إغلاق")
        return if (keys.any { text.contains(it) }) listOf(AmarStrategyRule("EXIT_1", text)) else emptyList()
    }

    private fun parseRiskRules(text: String): List<AmarStrategyRule> {
        val keys = listOf("stop loss", "risk", "max loss", "drawdown", "وقف خسارة", "مخاطرة", "أقصى خسارة", "درو داون")
        return if (keys.any { text.contains(it) }) listOf(AmarStrategyRule("RISK_1", text)) else emptyList()
    }

    private fun normalize(text: String): String = text.trim().replace(Regex("\\s+"), " ").lowercase(Locale.US)

    private fun canonical(strategyId: String, version: Int, symbol: String, timeframe: String, direction: AmarStrategyDirection, entry: List<AmarStrategyRule>, exit: List<AmarStrategyRule>, risk: List<AmarStrategyRule>, assumptions: List<String>, provenance: List<String>): String = listOf(
        strategyId.trim(), version, symbol, timeframe, direction.name,
        entry.sortedBy { it.id }.joinToString("|") { "${it.id}:${it.expression}" },
        exit.sortedBy { it.id }.joinToString("|") { "${it.id}:${it.expression}" },
        risk.sortedBy { it.id }.joinToString("|") { "${it.id}:${it.expression}" },
        assumptions.sorted().joinToString("|"), provenance.sorted().joinToString("|")
    ).joinToString("\u001f")

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
