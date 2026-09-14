package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarStageFiveTest {
    private val compiler = AmarStageFiveCompiler()

    @Test
    fun compile_is_deterministic_and_approved_when_all_gates_are_satisfied() {
        val request = AmarStrategyCompileRequest(
            strategyId = "gold-breakout",
            naturalLanguageIdea = "Buy XAUUSD on M5 when price crosses above resistance, stop loss 1%, take profit 2%",
            symbol = "XAUUSD",
            timeframe = "M5",
            sourceReferences = listOf("source:b", "source:a", "source:a")
        )
        val first = compiler.compile(request)
        val second = compiler.compile(request)
        val spec = first.specification
        assertTrue(first.approved)
        assertTrue(first.conflicts.isEmpty())
        assertNotNull(spec)
        assertEquals(first, second)
        assertEquals(64, spec!!.fingerprint.length)
        assertEquals(listOf("source:a", "source:b"), spec.provenance)
        assertEquals(6, first.opinions.size)
    }

    @Test
    fun compiler_blocks_execution_requests_inside_strategy_compilation() {
        val result = compiler.compile(AmarStrategyCompileRequest("unsafe-request", "Buy XAUUSD when price crosses above resistance, stop loss 1%, execute trade now", "XAUUSD", "M5"))
        assertFalse(result.approved)
        assertTrue(result.reasons.any { it.contains("adversarial_reviewer_rejected") })
        assertTrue(result.conflicts.isNotEmpty())
    }

    @Test
    fun compiler_fails_closed_when_direction_or_entry_is_missing() {
        val result = compiler.compile(AmarStrategyCompileRequest("incomplete", "use a careful strategy", "XAUUSD", "M5"))
        assertFalse(result.approved)
        assertTrue(result.specification == null)
        assertTrue(result.reasons.contains("strategy_specification_incomplete"))
    }

    @Test
    fun risk_role_rejects_missing_risk_rule_and_conflict_is_reported() {
        val result = compiler.compile(AmarStrategyCompileRequest("no-risk", "Buy XAUUSD on M5 when price crosses above resistance", "XAUUSD", "M5"))
        assertFalse(result.approved)
        assertFalse(result.opinions.single { it.role == AmarStrategyRole.RISK_REVIEWER }.approved)
        assertTrue(result.conflicts.any { AmarStrategyRole.RISK_REVIEWER in it.roles })
    }
}
