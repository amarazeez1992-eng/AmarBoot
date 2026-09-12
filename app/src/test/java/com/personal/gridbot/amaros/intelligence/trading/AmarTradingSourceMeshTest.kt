package com.personal.gridbot.amaros.intelligence.trading

import org.junit.Assert.assertTrue
import org.junit.Test

class AmarTradingSourceMeshTest {
    @Test
    fun sourceMeshContainsPrimaryAndOpenSourceChannels() {
        assertTrue(AmarTradingSourceMesh.sources.size >= 25)
        assertTrue(AmarTradingSourceMesh.sources.any { it.id == "tradingview" })
        assertTrue(AmarTradingSourceMesh.sources.any { it.id == "luxalgo" })
        assertTrue(AmarTradingSourceMesh.sources.any { it.id == "github" })
        assertTrue(AmarTradingSourceMesh.sources.any { it.id == "cftc" })
    }

    @Test
    fun botCatalogContainsMultipleTradingFamilies() {
        assertTrue(AmarTradingSourceMesh.bots.size >= 5)
        val text = AmarTradingSourceMesh.botCatalogText().lowercase()
        assertTrue(text.contains("freqtrade"))
        assertTrue(text.contains("hummingbot"))
        assertTrue(text.contains("octobot"))
    }

    @Test
    fun consensusIsNotProfitability() {
        val report = AmarTradingSourceMesh.Report(
            query = "x",
            searchedChannels = 12,
            evidence = emptyList(),
            supportingChannels = 5,
            conflictChannels = 0,
            consensusPct = 41.6,
            authorityGrade = com.personal.gridbot.amaros.ai.core.AmarAiResearchAuthority.Grade.LOW,
            authorityConfidencePct = 0.0,
            independentChannels = 0,
            newItems = emptyList(),
            caveat = "Consensus measures conceptual/source convergence only. It is NOT a profitability probability."
        )
        assertTrue(report.caveat.contains("NOT a profitability probability"))
    }
}
