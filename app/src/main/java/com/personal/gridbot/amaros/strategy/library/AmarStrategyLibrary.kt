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
        requireValid(strategy)
        val key = key(strategy.id, strategy.version)
        if (entries.containsKey(key)) return false
        entries[key] = strategy
        return true
    }

    fun saveNextVersion(id: String, name: String, rules: List<String>): StrategyVersion {
        require(id.isNotBlank()) { "STRATEGY_ID_REQUIRED" }
        require(name.isNotBlank()) { "STRATEGY_NAME_REQUIRED" }
        require(rules.isNotEmpty() && rules.none { it.isBlank() }) { "STRATEGY_RULES_INVALID" }
        val nextVersion = entries.values.filter { it.id == id.trim() }.maxOfOrNull { it.version }?.plus(1) ?: 1
        return StrategyVersion(id.trim(), nextVersion, name.trim(), rules.map { it.trim() }, Status.DRAFT).also {
            check(save(it)) { "STRATEGY_ALREADY_EXISTS" }
        }
    }

    fun copy(id: String, version: Int, newId: String, newName: String): StrategyVersion {
        val source = get(id, version) ?: error("STRATEGY_NOT_FOUND")
        require(newId.isNotBlank()) { "STRATEGY_ID_REQUIRED" }
        require(newName.isNotBlank()) { "STRATEGY_NAME_REQUIRED" }
        val next = StrategyVersion(newId.trim(), 1, newName.trim(), source.rules.map { it.trim() }, Status.DRAFT)
        check(save(next)) { "STRATEGY_ALREADY_EXISTS" }
        return next
    }

    /** Existing versions may only be edited while still a draft. Tested/approved versions are immutable. */
    fun update(strategy: StrategyVersion): Boolean {
        requireValid(strategy)
        val key = key(strategy.id, strategy.version)
        val current = entries[key] ?: return false
        if (current.status != Status.DRAFT || strategy.status != Status.DRAFT) return false
        entries[key] = strategy
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
        if (target == Status.TESTED && current.status != Status.DRAFT) return false
        if (target == Status.APPROVED && current.status != Status.TESTED) return false
        if (current.status == Status.ARCHIVED) return false
        entries[key(id, version)] = current.copy(status = target)
        return true
    }

    private fun requireValid(strategy: StrategyVersion) {
        require(strategy.id.isNotBlank()) { "STRATEGY_ID_REQUIRED" }
        require(strategy.name.isNotBlank()) { "STRATEGY_NAME_REQUIRED" }
        require(strategy.version > 0) { "STRATEGY_VERSION_INVALID" }
        require(strategy.rules.isNotEmpty() && strategy.rules.none { it.isBlank() }) { "STRATEGY_RULES_INVALID" }
    }

    private fun key(id: String, version: Int): String = "${id.trim()}::$version"
}
