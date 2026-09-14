package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarStageSixExpansionTest {
    @Test
    fun indicator_namespace_is_broad_and_not_limited_to_four() {
        assertTrue(AmarIndicatorKind.entries.size > 80)
        assertTrue(AmarIndicatorUniverse.capabilities.size > 80)
        assertTrue(AmarIndicatorUniverse.capabilities.any { it.name == "MACD" })
        assertTrue(AmarIndicatorUniverse.capabilities.any { it.name == "SUPERTREND" })
        assertTrue(AmarIndicatorUniverse.capabilities.any { it.name == "VWAP" })
        assertTrue(AmarIndicatorUniverse.capabilities.any { it.name == "CDL3BLACKCROWS" })
    }

    @Test
    fun hub_can_host_multiple_independent_indicator_engines() {
        val registry = AmarSourceRegistry()
        val hub = AmarIndicatorEngineHub(registry)
        val source = AmarBuiltInIndicatorAdapter().provenance
        hub.register(AmarBuiltInIndicatorAdapter())
        assertEquals(listOf(source.sourceId), hub.sourceIds())
    }

    @Test
    fun open_source_gateway_fails_closed_without_license_and_repository_evidence() {
        val gateway = AmarOpenSourceResearchGateway()
        val candidate = AmarOpenSourceCandidate(
            sourceId = "candidate",
            title = "Candidate",
            uri = "https://example.org/project",
            repository = null,
            version = "1.0",
            licenseSpdxId = null,
            licenseUri = null,
            publisher = "Example",
            description = "candidate"
        )
        try {
            gateway.admit(candidate)
            throw AssertionError("unverified open-source candidate must fail closed")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }
}
