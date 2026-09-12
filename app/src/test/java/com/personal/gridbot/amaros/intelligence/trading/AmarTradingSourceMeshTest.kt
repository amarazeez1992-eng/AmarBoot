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
        val text = AmarTradingSourceMesh.Report(
            "x", 12, emptyList(), 5, 0, 41.6, emptyList(), "Consensus measures conceptual/source convergence only. It is NOT a profitability probability."
        ).caveat
        assertTrue(text.contains("NOT a profitability probability"))
    }
}
