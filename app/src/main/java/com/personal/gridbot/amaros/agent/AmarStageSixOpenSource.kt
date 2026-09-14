package com.personal.gridbot.amaros.agent

/**
 * Provider-neutral gateway for broad open-source research.
 * Connectors may target public web indexes, Git repositories, package registries,
 * documentation, academic indexes, or local mirrors. The Agent never assumes a
 * source is reusable until provenance and licensing evidence has been admitted.
 */
interface AmarOpenSourceConnector {
    val connectorId: String
    suspend fun search(query: String, limit: Int): List<AmarOpenSourceCandidate>
}

data class AmarOpenSourceCandidate(
    val sourceId: String,
    val title: String,
    val uri: String,
    val repository: String?,
    val version: String,
    val licenseSpdxId: String?,
    val licenseUri: String?,
    val publisher: String,
    val description: String,
    val sourceType: AmarSourceType = AmarSourceType.KNOWLEDGE
) {
    init {
        require(sourceId.isNotBlank())
        require(title.isNotBlank())
        require(uri.startsWith("https://"))
        require(version.isNotBlank())
        require(publisher.isNotBlank())
    }
}

data class AmarAdmittedOpenSource(
    val candidate: AmarOpenSourceCandidate,
    val provenance: AmarSourceProvenance
)

class AmarOpenSourceResearchGateway(
    private val registry: AmarSourceRegistry = AmarSourceRegistry(),
    connectors: Iterable<AmarOpenSourceConnector> = emptyList()
) {
    private val connectors = connectors.associateBy { it.connectorId }.toMutableMap()

    fun registerConnector(connector: AmarOpenSourceConnector) {
        require(connector.connectorId.isNotBlank())
        require(connectors[connector.connectorId] == null) { "Connector already registered: ${connector.connectorId}" }
        connectors[connector.connectorId] = connector
    }

    fun connectorIds(): List<String> = connectors.keys.sorted()

    suspend fun searchAll(query: String, maxPerConnector: Int = 25): List<AmarOpenSourceCandidate> {
        require(query.isNotBlank())
        require(maxPerConnector in 1..1_000)
        return connectors.values
            .sortedBy { it.connectorId }
            .flatMap { it.search(query, maxPerConnector) }
            .distinctBy { it.sourceId }
    }

    fun admit(candidate: AmarOpenSourceCandidate): AmarAdmittedOpenSource {
        val license = candidate.licenseSpdxId
        require(!license.isNullOrBlank()) { "Open-source candidate has no SPDX license evidence" }
        require(!candidate.repository.isNullOrBlank()) { "Open-source candidate has no repository provenance" }
        val licenseUri = candidate.licenseUri
        require(!licenseUri.isNullOrBlank() && licenseUri.startsWith("https://")) {
            "Open-source candidate has no license evidence URI"
        }
        val provenance = AmarSourceProvenance(
            sourceId = candidate.sourceId,
            name = candidate.title,
            version = candidate.version,
            homepage = candidate.uri,
            sourceType = candidate.sourceType,
            license = AmarSourceLicense(license, licenseUri),
            repository = candidate.repository
        )
        registry.register(provenance)
        return AmarAdmittedOpenSource(candidate, provenance)
    }

    fun admittedSources(): List<AmarSourceProvenance> = registry.all()
}
