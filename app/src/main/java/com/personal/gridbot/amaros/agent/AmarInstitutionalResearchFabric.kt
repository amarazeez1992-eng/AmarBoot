package com.personal.gridbot.amaros.agent

/**
 * Institutional-grade research fabric.
 *
 * The Director does not depend on one search engine, one vendor, one indicator
 * library, or one model. It dispatches the same research objective to many
 * independently governed channels and then reconciles the evidence.
 */
enum class AmarResearchChannel {
    OPEN_WEB,
    OFFICIAL_REGULATORY,
    CENTRAL_BANK,
    EXCHANGE,
    BROKER_RESEARCH,
    ASSET_MANAGER,
    BANK_RESEARCH,
    ACADEMIC,
    PREPRINT,
    QUANT_RESEARCH,
    OPEN_SOURCE_CODE,
    PACKAGE_REGISTRY,
    DOCUMENTATION,
    NEWS,
    MACROECONOMIC,
    ON_CHAIN,
    SENTIMENT,
    MARKET_DATA,
    OPTIONS_DERIVATIVES,
    ALTERNATIVE_DATA,
    COMMUNITY
}

data class AmarResearchSourceDescriptor(
    val id: String,
    val name: String,
    val channel: AmarResearchChannel,
    val baseUri: String,
    val official: Boolean,
    val openAccess: Boolean,
    val searchable: Boolean,
    val updateSupported: Boolean,
    val licenseOrTermsUri: String? = null,
    val notes: String = ""
) {
    init {
        require(id.isNotBlank())
        require(name.isNotBlank())
        require(baseUri.startsWith("https://"))
        require(licenseOrTermsUri == null || licenseOrTermsUri.startsWith("https://"))
    }
}

/** Seed catalog: discovery targets, not claims that every site permits unrestricted scraping. */
object AmarInstitutionalSourceCatalog {
    val seeds: List<AmarResearchSourceDescriptor> = listOf(
        AmarResearchSourceDescriptor("sec", "U.S. SEC", AmarResearchChannel.OFFICIAL_REGULATORY, "https://www.sec.gov", true, true, true, true),
        AmarResearchSourceDescriptor("cftc", "U.S. CFTC", AmarResearchChannel.OFFICIAL_REGULATORY, "https://www.cftc.gov", true, true, true, true),
        AmarResearchSourceDescriptor("fed", "Federal Reserve", AmarResearchChannel.CENTRAL_BANK, "https://www.federalreserve.gov", true, true, true, true),
        AmarResearchSourceDescriptor("fred", "FRED", AmarResearchChannel.MACROECONOMIC, "https://fred.stlouisfed.org", true, true, true, true),
        AmarResearchSourceDescriptor("ecb", "European Central Bank", AmarResearchChannel.CENTRAL_BANK, "https://www.ecb.europa.eu", true, true, true, true),
        AmarResearchSourceDescriptor("bis", "Bank for International Settlements", AmarResearchChannel.CENTRAL_BANK, "https://www.bis.org", true, true, true, true),
        AmarResearchSourceDescriptor("imf", "International Monetary Fund", AmarResearchChannel.MACROECONOMIC, "https://www.imf.org", true, true, true, true),
        AmarResearchSourceDescriptor("world-bank", "World Bank", AmarResearchChannel.MACROECONOMIC, "https://www.worldbank.org", true, true, true, true),
        AmarResearchSourceDescriptor("eia", "U.S. Energy Information Administration", AmarResearchChannel.MACROECONOMIC, "https://www.eia.gov", true, true, true, true),
        AmarResearchSourceDescriptor("nasdaq", "Nasdaq", AmarResearchChannel.EXCHANGE, "https://www.nasdaq.com", true, true, true, true),
        AmarResearchSourceDescriptor("nyse", "NYSE", AmarResearchChannel.EXCHANGE, "https://www.nyse.com", true, true, true, true),
        AmarResearchSourceDescriptor("cme", "CME Group", AmarResearchChannel.EXCHANGE, "https://www.cmegroup.com", true, true, true, true),
        AmarResearchSourceDescriptor("ice", "Intercontinental Exchange", AmarResearchChannel.EXCHANGE, "https://www.ice.com", true, true, true, true),
        AmarResearchSourceDescriptor("eurex", "Eurex", AmarResearchChannel.EXCHANGE, "https://www.eurex.com", true, true, true, true),
        AmarResearchSourceDescriptor("arxiv", "arXiv", AmarResearchChannel.PREPRINT, "https://arxiv.org", true, true, true, true),
        AmarResearchSourceDescriptor("ssrn", "SSRN", AmarResearchChannel.ACADEMIC, "https://www.ssrn.com", false, true, true, true),
        AmarResearchSourceDescriptor("github", "GitHub", AmarResearchChannel.OPEN_SOURCE_CODE, "https://github.com", false, true, true, true),
        AmarResearchSourceDescriptor("pypi", "Python Package Index", AmarResearchChannel.PACKAGE_REGISTRY, "https://pypi.org", true, true, true, true),
        AmarResearchSourceDescriptor("npm", "npm", AmarResearchChannel.PACKAGE_REGISTRY, "https://www.npmjs.com", false, true, true, true),
        AmarResearchSourceDescriptor("huggingface", "Hugging Face", AmarResearchChannel.OPEN_SOURCE_CODE, "https://huggingface.co", false, true, true, true),
        AmarResearchSourceDescriptor("ta-lib", "TA-Lib", AmarResearchChannel.OPEN_SOURCE_CODE, "https://ta-lib.org", false, true, true, true, "https://ta-lib.org"),
        AmarResearchSourceDescriptor("model-context-protocol", "Model Context Protocol", AmarResearchChannel.OPEN_SOURCE_CODE, "https://modelcontextprotocol.io", false, true, true, true, "https://github.com/modelcontextprotocol/modelcontextprotocol"),
        AmarResearchSourceDescriptor("blackrock", "BlackRock", AmarResearchChannel.ASSET_MANAGER, "https://www.blackrock.com", true, true, true, true),
        AmarResearchSourceDescriptor("jpm-research", "J.P. Morgan Research", AmarResearchChannel.BANK_RESEARCH, "https://www.jpmorgan.com/insights", true, true, true, true),
        AmarResearchSourceDescriptor("quantpedia", "Quantpedia", AmarResearchChannel.QUANT_RESEARCH, "https://quantpedia.com", false, true, true, true)
    )
}

data class AmarResearchQuery(
    val objective: String,
    val symbols: List<String> = emptyList(),
    val channels: Set<AmarResearchChannel> = AmarResearchChannel.entries.toSet(),
    val requireOfficialEvidence: Boolean = false,
    val requireOpenSourceEvidence: Boolean = false,
    val freshnessDays: Int = 30,
    val maxFindings: Int = 250,
    val deepResearch: Boolean = true
) {
    init {
        require(objective.isNotBlank())
        require(freshnessDays >= 0)
        require(maxFindings in 1..5_000)
    }
}

data class AmarResearchEvidence(
    val source: AmarResearchSourceDescriptor,
    val title: String,
    val uri: String,
    val excerpt: String,
    val retrievedAtEpochMs: Long,
    val evidenceFingerprint: String,
    val authorityScore: Double,
    val relevanceScore: Double,
    val freshnessScore: Double
) {
    val compositeScore: Double
        get() = (authorityScore * 0.40 + relevanceScore * 0.40 + freshnessScore * 0.20).coerceIn(0.0, 1.0)
}

interface AmarResearchChannelAdapter {
    val channel: AmarResearchChannel
    suspend fun search(query: AmarResearchQuery, source: AmarResearchSourceDescriptor): List<AmarResearchEvidence>
}

/**
 * MCB = Multi-Channel Browser/Research Bus.
 * It is deliberately provider-neutral: adapters can target search APIs, RSS,
 * official APIs, Git repositories, package indexes, academic services, or local mirrors.
 */
class AmarMcbResearchBus(
    private val adapters: List<AmarResearchChannelAdapter>
) {
    suspend fun execute(
        query: AmarResearchQuery,
        sources: List<AmarResearchSourceDescriptor> = AmarInstitutionalSourceCatalog.seeds
    ): AmarMcbResearchResult {
        val selected = sources.filter { it.channel in query.channels && it.searchable }
        val evidence = selected.flatMap { source ->
            val adapter = adapters.firstOrNull { it.channel == source.channel } ?: return@flatMap emptyList()
            adapter.search(query, source)
        }
            .filter { it.uri.startsWith("https://") }
            .distinctBy { it.evidenceFingerprint }
            .sortedWith(compareByDescending<AmarResearchEvidence> { it.compositeScore }.thenBy { it.source.id }.thenBy { it.uri })
            .take(query.maxFindings)

        val byChannel = evidence.groupingBy { it.source.channel }.eachCount()
        val bySource = evidence.groupingBy { it.source.id }.eachCount()
        val conflicts = detectConflicts(evidence)
        return AmarMcbResearchResult(
            objective = query.objective,
            evidence = evidence,
            channelCounts = byChannel,
            sourceCounts = bySource,
            conflicts = conflicts,
            deepResearch = query.deepResearch
        )
    }

    private fun detectConflicts(evidence: List<AmarResearchEvidence>): List<String> {
        val official = evidence.filter { it.source.official }
        return if (official.map { it.source.id }.distinct().size >= 2 && evidence.size >= 4) {
            listOf("Multi-source evidence collected; semantic conflict resolution is required before a final claim.")
        } else emptyList()
    }
}

data class AmarMcbResearchResult(
    val objective: String,
    val evidence: List<AmarResearchEvidence>,
    val channelCounts: Map<AmarResearchChannel, Int>,
    val sourceCounts: Map<String, Int>,
    val conflicts: List<String>,
    val deepResearch: Boolean
)
