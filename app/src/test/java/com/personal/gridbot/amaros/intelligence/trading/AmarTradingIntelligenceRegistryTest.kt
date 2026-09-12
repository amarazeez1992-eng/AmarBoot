package com.personal.gridbot.amaros.intelligence.trading

import org.junit.Assert.assertTrue
import org.junit.Test

class AmarTradingIntelligenceRegistryTest {
    @Test
    fun registryHasBroadGlobalPrimaryCoverage() {
        assertTrue(AmarTradingIntelligenceRegistry.officialSources.size >= 40)
        assertTrue(AmarTradingIntelligenceRegistry.officialSources.any { it.name == "ECB" })
        assertTrue(AmarTradingIntelligenceRegistry.officialSources.any { it.name == "BLS" })
        assertTrue(AmarTradingIntelligenceRegistry.officialSources.any { it.name == "FRED" })
        assertTrue(AmarTradingIntelligenceRegistry.officialSources.any { it.name == "Nasdaq" })
        assertTrue(AmarTradingIntelligenceRegistry.officialSources.any { it.name == "QuantConnect LEAN" })
    }

    @Test
    fun governanceRequiresIndependentEvidenceAndMeasuredValidation() {
        val governance = AmarTradingIntelligenceRegistry.governance()
        assertTrue(governance.contains("independent channels"))
        assertTrue(governance.contains("profitability probability"))
        assertTrue(governance.contains("OOS validation"))
        assertTrue(AmarTradingIntelligenceRegistry.confidencePolicy().contains("VERIFIED"))
    }
}
