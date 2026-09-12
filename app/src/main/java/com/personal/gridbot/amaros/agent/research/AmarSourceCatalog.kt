package com.personal.gridbot.amaros.agent.research

/** Curated open-source/public trading research directory. Network access is supplied by adapters. */
data class AmarSourceLink(
    val name: String,
    val url: String,
    val category: AmarSourceCategory,
    val authorityHint: AmarSourceAuthorityHint
)

enum class AmarSourceCategory { OFFICIAL_MARKET, BROKER, REGULATORY, ECONOMIC, CRYPTO, RESEARCH, NEWS, TECHNOLOGY, EDUCATION }
enum class AmarSourceAuthorityHint { PRIMARY, OFFICIAL, REPUTABLE, COMMUNITY }

class AmarSourceCatalog {
    fun defaultLinks(): List<AmarSourceLink> = listOf(
        AmarSourceLink("MQL5 Documentation", "https://www.mql5.com/en/docs", AmarSourceCategory.BROKER, AmarSourceAuthorityHint.PRIMARY),
        AmarSourceLink("TradingView Pine Script", "https://www.tradingview.com/pine-script-docs/", AmarSourceCategory.TECHNOLOGY, AmarSourceAuthorityHint.PRIMARY),
        AmarSourceLink("CFTC", "https://www.cftc.gov/", AmarSourceCategory.REGULATORY, AmarSourceAuthorityHint.OFFICIAL),
        AmarSourceLink("SEC", "https://www.sec.gov/", AmarSourceCategory.REGULATORY, AmarSourceAuthorityHint.OFFICIAL),
        AmarSourceLink("Federal Reserve", "https://www.federalreserve.gov/", AmarSourceCategory.ECONOMIC, AmarSourceAuthorityHint.OFFICIAL),
        AmarSourceLink("ECB", "https://www.ecb.europa.eu/", AmarSourceCategory.ECONOMIC, AmarSourceAuthorityHint.OFFICIAL),
        AmarSourceLink("BIS", "https://www.bis.org/", AmarSourceCategory.RESEARCH, AmarSourceAuthorityHint.OFFICIAL),
        AmarSourceLink("World Bank", "https://www.worldbank.org/", AmarSourceCategory.ECONOMIC, AmarSourceAuthorityHint.OFFICIAL),
        AmarSourceLink("IMF", "https://www.imf.org/", AmarSourceCategory.ECONOMIC, AmarSourceAuthorityHint.OFFICIAL),
        AmarSourceLink("CCXT", "https://github.com/ccxt/ccxt", AmarSourceCategory.CRYPTO, AmarSourceAuthorityHint.PRIMARY)
    )
}
