package com.personal.gridbot.amaros.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiToolRegistryTest {
    @Test fun readOnlyToolIsRegisteredAndNeverExecutionCapable() {
        val spec = AmarAiToolRegistry.resolve("engine_market")
        assertNotNull(spec)
        assertEquals(AmarAiToolRegistry.Authority.READ_ONLY, spec?.authority)
        assertFalse(AmarAiToolRegistry.isExecutionCapable("engine_market"))
    }

    @Test fun draftToolRequiresGovernedDraftAuthority() {
        val spec = AmarAiToolRegistry.resolve("strategy_save")
        assertNotNull(spec)
        assertEquals(AmarAiToolRegistry.Authority.DRAFT_ONLY, spec?.authority)
        assertTrue(spec?.requiresContext == true)
        assertFalse(AmarAiToolRegistry.isExecutionCapable("strategy_save"))
    }

    @Test fun unknownToolIsRejectedByRegistry() {
        assertFalse(AmarAiToolRegistry.isKnown("place_market_order"))
        assertEquals(null, AmarAiToolRegistry.resolve("place_market_order"))
    }

    @Test fun brokerExecutionToolCannotBecomeKnownByNamingConvention() {
        assertFalse(AmarAiToolRegistry.isExecutionCapable("execute_order"))
        assertFalse(AmarAiToolRegistry.isKnown("execute_order"))
        assertFalse(AmarAiToolRegistry.isKnown("broker_execute"))
    }
}
