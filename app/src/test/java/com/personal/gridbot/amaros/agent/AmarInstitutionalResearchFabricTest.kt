package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertTrue
import org.junit.Test

class AmarInstitutionalResearchFabricTest {
    @Test
    fun institutional_catalog_has_multiple_independent_channels() {
        val channels = AmarInstitutionalSourceCatalog.seeds.map { it.channel }.toSet()
        assertTrue(channels.size >= 10)
        assertTrue(AmarInstitutionalSourceCatalog.seeds.any { it.id == "sec" && it.official })
        assertTrue(AmarInstitutionalSourceCatalog.seeds.any { it.id == "github" })
        assertTrue(AmarInstitutionalSourceCatalog.seeds.any { it.id == "cme" })
    }

    @Test
    fun school_catalog_is_broad() {
        assertTrue(AmarTradingSchoolCatalog.all.size >= 40)
        assertTrue(AmarTradingSchoolCatalog.all.any { it.school == AmarTradingSchool.ORDER_FLOW })
        assertTrue(AmarTradingSchoolCatalog.all.any { it.school == AmarTradingSchool.STATISTICAL_ARBITRAGE })
        assertTrue(AmarTradingSchoolCatalog.all.any { it.school == AmarTradingSchool.MARKET_MICROSTRUCTURE })
    }

    @Test
    fun capability_refresh_rejects_unlicensed_candidates() = kotlinx.coroutines.runBlocking {
        val registry = AmarCapabilityDiscoveryRegistry()
        registry.register(object : AmarCapabilityDiscoveryAdapter {
            override val id = "test"
            override suspend fun discover(query: String, limit: Int) = listOf(
                AmarCapabilityCandidate("good", "Good", AmarCapabilityCategory.INDICATOR, "1", "https://github.com/good", "https://github.com/good/docs", "MIT", "https://opensource.org/license/mit", 1, "good"),
                AmarCapabilityCandidate("bad", "Bad", AmarCapabilityCategory.INDICATOR, "1", "https://github.com/bad", "https://github.com/bad/docs", null, null, 1, "bad")
            )
        })
        val report = AmarCapabilityRefreshPlanner(registry).refresh(listOf("indicators"))
        assertTrue(report.discovered.size == 2)
        assertTrue(report.admissible.single().id == "good")
        assertTrue(report.rejected.single().id == "bad")
    }
}
