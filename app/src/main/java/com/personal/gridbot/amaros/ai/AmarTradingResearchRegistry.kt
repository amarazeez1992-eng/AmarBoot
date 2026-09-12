package com.personal.gridbot.amaros.ai

/** Curated research hierarchy used to steer AMAR AI toward higher-quality trading evidence. */
object AmarTradingResearchRegistry {
    data class Source(
        val name: String,
        val tier: Int,
        val domains: List<String>,
        val reliability: String,
        val url: String
    )

    val sources: List<Source> = listOf(
        Source("CFTC", 1, listOf("COT", "futures positioning", "speculative positioning"), "Primary regulator", "https://www.cftc.gov/MarketReports/CommitmentsofTraders/index.htm"),
        Source("SEC", 1, listOf("filings", "public companies", "market disclosures"), "Primary regulator", "https://www.sec.gov/"),
        Source("Federal Reserve", 1, listOf("rates", "liquidity", "macro", "financial conditions"), "Primary institution", "https://www.federalreserve.gov/"),
        Source("BIS", 1, listOf("FX", "liquidity", "banking", "macro"), "Primary institution", "https://www.bis.org/"),
        Source("CME Group", 1, listOf("futures", "options", "volatility", "market structure"), "Primary exchange", "https://www.cmegroup.com/"),
        Source("IMF", 1, listOf("macro", "growth", "inflation", "global economy"), "Primary institution", "https://www.imf.org/"),
        Source("GitHub", 3, listOf("open source", "code", "research implementations"), "Community code; validate independently", "https://github.com/"),
        Source("Wikipedia", 4, listOf("definitions", "background", "terminology"), "Secondary reference; verify critical claims", "https://www.wikipedia.org/")
    )

    fun protocol(): String = buildString {
        appendLine("Research hierarchy: Tier 1 primary institutions/regulators first; Tier 3/4 are discovery aids only.")
        appendLine("Never treat a social post, marketing page, repository README, or unverified backtest as proof of profitability.")
        appendLine("For strategy claims, require: source identity, publication/date when available, methodology, sample/data period, assumptions, limitations, and independent validation.")
        appendLine("Separate market facts, academic/statistical evidence, implementation evidence, and opinion.")
        appendLine("When sources conflict, preserve both claims, explain the conflict, and prefer the higher-quality primary evidence.")
    }

    fun catalogText(): String = sources.joinToString(" | ") { "${it.name}[T${it.tier}; ${it.domains.joinToString(", ")}]" }
}
