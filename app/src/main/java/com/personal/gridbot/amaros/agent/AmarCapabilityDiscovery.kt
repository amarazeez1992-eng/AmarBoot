package com.personal.gridbot.amaros.agent

/** A discovered capability is a candidate until provenance, compatibility and security checks pass. */
data class AmarCapabilityCandidate(
    val id: String,
    val name: String,
    val category: AmarCapabilityCategory,
    val version: String,
    val repository: String,
    val documentation: String,
    val licenseSpdxId: String?,
    val licenseUri: String?,
    val discoveredAtEpochMs: Long,
    val fingerprint: String
) {
    init {
        require(id.isNotBlank() && name.isNotBlank())
        require(version.isNotBlank())
        require(repository.startsWith("https://"))
        require(documentation.startsWith("https://"))
        require(licenseUri == null || licenseUri.startsWith("https://"))
    }
}

enum class AmarCapabilityCategory {
    INDICATOR,
    DATA_CONNECTOR,
    SEARCH_CONNECTOR,
    BACKTEST_ENGINE,
    STATISTICS_ENGINE,
    MACHINE_LEARNING,
    MARKET_STRUCTURE,
    OPTIONS_ENGINE,
    SENTIMENT_ENGINE,
    NEWS_ENGINE,
    MACRO_ENGINE,
    ON_CHAIN_ENGINE,
    PORTFOLIO_ENGINE,
    OPTIMIZATION_ENGINE,
    VISUALIZATION,
    RESEARCH_TOOL,
    PROTOCOL,
    OTHER
}

interface AmarCapabilityDiscoveryAdapter {
    val id: String
    suspend fun discover(query: String, limit: Int): List<AmarCapabilityCandidate>
}

class AmarCapabilityDiscoveryRegistry {
    private val adapters = linkedMapOf<String, AmarCapabilityDiscoveryAdapter>()

    fun register(adapter: AmarCapabilityDiscoveryAdapter) {
        require(adapter.id.isNotBlank())
        require(adapters[adapter.id] == null) { "Capability discovery adapter already exists: ${adapter.id}" }
        adapters[adapter.id] = adapter
    }

    fun all(): List<AmarCapabilityDiscoveryAdapter> = adapters.values.toList()
}

/**
 * Automatic refresh planner. It never silently installs code. It discovers,
 * scores and queues candidates for admission; installation remains a separately
 * auditable lifecycle operation.
 */
class AmarCapabilityRefreshPlanner(
    private val discoveryRegistry: AmarCapabilityDiscoveryRegistry
) {
    suspend fun refresh(
        queries: List<String>,
        limitPerAdapter: Int = 50
    ): AmarCapabilityRefreshReport {
        require(queries.isNotEmpty())
        require(limitPerAdapter in 1..500)

        // Keep suspension points in the coroutine body; do not hide suspend calls
        // inside Sequence lambdas, which are not suspend-aware.
        val candidates = buildList {
            for (query in queries) {
                if (query.isBlank()) continue
                for (adapter in discoveryRegistry.all()) {
                    addAll(adapter.discover(query, limitPerAdapter))
                }
            }
        }.distinctBy { it.fingerprint }

        val admissible = candidates.filter { candidate ->
            candidate.licenseSpdxId in setOf("MIT", "Apache-2.0", "BSD-2-Clause", "BSD-3-Clause", "ISC", "0BSD", "MPL-2.0") &&
                candidate.licenseUri != null
        }
        return AmarCapabilityRefreshReport(
            discovered = candidates,
            admissible = admissible,
            rejected = candidates.filterNot { it in admissible }
        )
    }
}

data class AmarCapabilityRefreshReport(
    val discovered: List<AmarCapabilityCandidate>,
    val admissible: List<AmarCapabilityCandidate>,
    val rejected: List<AmarCapabilityCandidate>
)
