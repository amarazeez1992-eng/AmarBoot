package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarModelRoutingAuditTest {
    @Test fun records_level_one_and_level_two() {
        val audit = AmarModelRoutingAudit()
        audit.record(1, "REASONING_PROVIDER", AmarModelComplexity.LOW, "AmarLocalReasoning", "SELECTED", "LOW complexity")
        audit.record(2, "ADAPTIVE_MODEL_ROUTING", AmarModelComplexity.HIGH, "model", "SELECTED", "CAPABILITY_RESOURCE_QUALITY_MATCH")
        val records = audit.records()
        assertEquals(2, records.size)
        assertTrue(records.any { it.level == 1 })
        assertTrue(records.any { it.level == 2 })
    }
}
