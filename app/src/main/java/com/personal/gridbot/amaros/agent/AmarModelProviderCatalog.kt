package com.personal.gridbot.amaros.agent

/**
 * Runtime registry for model providers.
 *
 * The catalog owns registration and lookup only. It does not make routing decisions.
 */
class AmarModelProviderCatalog(
    providers: List<AmarModelProviderProfile> = emptyList()
) {
    private val entries = providers.associateBy { it.provider.id }.toMutableMap()

    fun register(profile: AmarModelProviderProfile): AmarModelProviderCatalog {
        require(profile.provider.id.isNotBlank()) { "provider id must not be blank" }
        require(profile.maxContextTokens >= 0) { "maxContextTokens must not be negative" }
        require(profile.estimatedRamMb >= 0) { "estimatedRamMb must not be negative" }
        entries[profile.provider.id] = profile
        return this
    }

    fun providers(): List<AmarModelProviderProfile> = entries.values.toList()

    fun isEmpty(): Boolean = entries.isEmpty()

    fun get(providerId: String): AmarModelProviderProfile? = entries[providerId]
}

data class AmarModelProviderProfile(
    val provider: AmarModelProvider,
    val capabilities: Set<AmarModelCapability>,
    val maxContextTokens: Int,
    val estimatedRamMb: Int,
    val quality: AmarModelQuality,
    val cost: AmarModelCost,
    val available: Boolean = true
)

enum class AmarModelCapability {
    SIMPLE_EXPLANATION,
    MULTI_FACTOR_ANALYSIS,
    TEXT_GENERATION
}

enum class AmarModelQuality {
    BASIC,
    STANDARD,
    HIGH
}

enum class AmarModelCost {
    LOW,
    MEDIUM,
    HIGH
}
