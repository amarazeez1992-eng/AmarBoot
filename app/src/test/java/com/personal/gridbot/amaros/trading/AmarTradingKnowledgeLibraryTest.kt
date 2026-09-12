package com.personal.gridbot.amaros.trading

import com.personal.gridbot.amaros.intelligence.trading.AmarTradingKnowledgeLibrary
import org.junit.Assert.*
import org.junit.Test

class AmarTradingKnowledgeLibraryTest {
    @Test fun libraryHasBroadTradingDomains() {
        assertTrue(AmarTradingKnowledgeLibrary.domains.size >= 15)
        assertTrue(AmarTradingKnowledgeLibrary.domains.any { it.name == "Validation" })
        assertTrue(AmarTradingKnowledgeLibrary.domains.any { it.name == "Risk & Exits" })
    }

    @Test fun searchFindsRelevantDomains() {
        val hits = AmarTradingKnowledgeLibrary.search("liquidity order block")
        assertTrue(hits.any { it.name == "SMC / ICT" })
    }

    @Test fun governanceProtectsProprietaryBoundaries() {
        assertTrue(AmarTradingKnowledgeLibrary.governance().contains("Protected"))
    }
}
