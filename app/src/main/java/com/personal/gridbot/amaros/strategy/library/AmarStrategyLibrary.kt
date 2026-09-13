package com.personal.gridbot.amaros.strategy.library

/**
 * In-memory governed strategy library. It stores versioned definitions only;
 * approval never implies execution or live performance.
 */
class AmarStrategyLibrary {
    enum class Status { DRAFT, TESTED, APPROVED, ARCHIVED }

    data class StrategyVersion(
        val id: String,
        val version: Int,
        val name: String,
        val rules: List<String>,
        val status: Status = Status.DRAFT,
    )

    private val entries = linkedMapOf<String, StrategyVersion>()

    fun save(strategy: StrategyVersion): Boolean {
        val normalized = normalize(strategy)
        val key = key(normalized.id, normalized.version)
        if (entries.containsKey(key)) return false
        entries[key] = normalized
        return true
    }

    fun saveNextVersion(id: String, name: String, rules: List<String>): StrategyVersion {
        val normalizedId = id.trim()
        val normalizedName = name.trim()
        val normalizedRules = rules.map { it.trim() }
        require(normalizedId.isNotEmpty()) { "STRATEGY_ID_REQUIRED" }
        require(normalizedName.isNotEmpty()) { "STRATEGY_NAME_REQUIRED" }
        require(normalizedRules.isNotEmpty() && normalizedRules.none { it.isEmpty() }) { "STRATEGY_RULES_INVALID" }
        val nextVersion = entries.values.filter { it.id == normalizedId }.maxOfOrNull { it.version }?.plus(1) ?: 1
        return StrategyVersion(normalizedId, nextVersion, normalizedName, normalizedRules, Status.DRAFT).also {
            check(save(it)) { "STRATEGY_ALREADY_EXISTS" }
        }
    }

    fun copy(id: String, version: Int, newId: String, newName: String): StrategyVersion {
        val source = get(id, version) ?: error("STRATEGY_NOT_FOUND")
        val normalizedId = newId.trim()
        val normalizedName = newName.trim()
        require(normalizedId.isNotEmpty()) { "STRATEGY_ID_REQUIRED" }
        require(normalizedName.isNotEmpty()) { "STRATEGY_NAME_REQUIRED" }
        val next = StrategyVersion(normalizedId, 1, normalizedName, source.rules.map { it.trim() }, Status.DRAFT)
        check(save(next)) { "STRATEGY_ALREADY_EXISTS" }
        return next
    }

    /** Existing versions may only be edited while still a draft. Tested/approved versions are immutable. */
    fun update(strategy: StrategyVersion): Boolean {
        val normalized = normalize(strategy)
        val key = key(normalized.id, normalized.version)
        val current = entries[key] ?: return false
        if (current.status != Status.DRAFT || normalized.status != Status.DRAFT) return false
        entries[key] = normalized
        return true
    }

    /** Marks a draft as tested after an external, measured test has actually completed. */
    fun markTested(id: String, version: Int): Boolean = transition(id, version, Status.TESTED)

    fun approve(id: String, version: Int): Boolean = transition(id, version, Status.APPROVED)

    fun archive(id: String, version: Int): Boolean = transition(id, version, Status.ARCHIVED)

    fun get(id: String, version: Int): StrategyVersion? = entries[key(id, version)]

    fun list(): List<StrategyVersion> = entries.values.toList()

    private fun transition(id: String, version: Int, target: Status): Boolean {
        val current = get(id, version) ?: return false
        val allowed = when (target) {
            Status.TESTED -> current.status == Status.DRAFT
            Status.APPROVED -> current.status == Status.TESTED
            Status.ARCHIVED -> current.status != Status.ARCHIVED
            Status.DRAFT -> false
        }
        if (!allowed) return false
        entries[key(current.id, current.version)] = current.copy(status = target)
        return true
    }

    private fun normalize(strategy: StrategyVersion): StrategyVersion {
        require(strategy.id.isNotBlank()) { "STRATEGY_ID_REQUIRED" }
        require(strategy.name.isNotBlank()) { "STRATEGY_NAME_REQUIRED" }
        require(strategy.version > 0) { "STRATEGY_VERSION_INVALID" }
        require(strategy.rules.isNotEmpty() && strategy.rules.none { it.isBlank() }) { "STRATEGY_RULES_INVALID" }
        return strategy.copy(
            id = strategy.id.trim(),
            name = strategy.name.trim(),
            rules = strategy.rules.map { it.trim() }
        )
    }

    private fun key(id: String, version: Int): String = "${id.trim()}::$version"
}
