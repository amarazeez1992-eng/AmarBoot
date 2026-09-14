package com.personal.gridbot.amaros.agent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AmarStageFourTest {
    @Test
    fun simulation_closes_on_reversal_and_calculates_pnl() {
        val candles = listOf(
            candle(1, 100.0), candle(2, 105.0), candle(3, 103.0)
        )
        val signals = listOf(
            AmarStrategySignal(1, AmarSignalDirection.LONG),
            AmarStrategySignal(2, AmarSignalDirection.LONG),
            AmarStrategySignal(3, AmarSignalDirection.SHORT)
        )
        val result = AmarStageFourSimulationEngine().run(candles, signals, AmarSimulationConfig(1000.0, 1.0))
        assertEquals(1, result.trades.size)
        assertEquals(3.0, result.trades.single().netPnl)
        assertEquals(1003.0, result.finalEquity)
    }

    @Test
    fun simulation_rejects_empty_data() {
        val result = AmarStageFourSimulationEngine().run(emptyList(), emptyList(), AmarSimulationConfig(100.0, 1.0))
        assertFalse(result.completed)
        assertTrue("no_candles" in result.issues)
    }

    @Test
    fun risk_gate_blocks_daily_loss_and_open_position_limits() {
        val engine = AmarStageFourRiskEngine(AmarRiskLimits(maxDailyLossFraction = 0.05, maxOpenPositions = 1))
        val decision = engine.evaluate(AmarRiskSnapshot(930.0, 1000.0, 1, -60.0, 1000.0), 10.0)
        assertFalse(decision.approved)
        assertTrue("daily_loss_limit_breached" in decision.reasons)
        assertTrue("max_open_positions_reached" in decision.reasons)
    }

    @Test
    fun risk_gate_blocks_excessive_trade_risk() {
        val engine = AmarStageFourRiskEngine(AmarRiskLimits(maxRiskPerTradeFraction = 0.02))
        val decision = engine.evaluate(AmarRiskSnapshot(1000.0, 1000.0, 0, 0.0, 1000.0), 25.0)
        assertFalse(decision.approved)
        assertTrue("per_trade_risk_limit_breached" in decision.reasons)
    }

    @Test
    fun crisis_engine_detects_spread_gap_range_and_stale_data() {
        val previous = candle(1, 100.0)
        val current = AmarMarketCandle(2_000L, 110.0, 115.0, 100.0, 105.0)
        val quote = AmarMarketQuote("XAUUSD", 100.0, 101.0, 2_000L)
        val state = AmarStageFourCrisisEngine(AmarCrisisLimits(maxSpreadFraction = 0.001, maxBarRangeFraction = 0.01, maxGapFraction = 0.01, maxDataAgeMs = 100L)).inspect(previous, current, quote, 2_500L)
        assertTrue(state.active)
        assertTrue("spread_abnormal" in state.reasons)
        assertTrue("bar_range_abnormal" in state.reasons)
        assertTrue("price_gap_abnormal" in state.reasons)
        assertTrue("market_data_stale" in state.reasons)
    }

    @Test
    fun audit_ledger_is_hash_chained_and_detects_tampering() {
        val ledger = AmarStageFourAuditLedger()
        ledger.append(1L, "DIRECTOR", "SIMULATE", "APPROVED", "validated")
        ledger.append(2L, "RISK_GUARD", "RISK_CHECK", "BLOCKED", "drawdown")
        assertTrue(ledger.verifyIntegrity())
        val event = ledger.snapshot().last()
        val tampered = event.copy(reason = "changed")
        assertFalse(verifyEventAgainstChain(tampered, ledger.snapshot().dropLast(1).last().hash))
    }

    private fun verifyEventAgainstChain(event: AmarAuditEvent, previous: String): Boolean {
        val material = listOf(event.sequence, event.timestampEpochMs, event.actor, event.action, event.decision, event.reason, previous).joinToString("|")
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest(material.toByteArray()).joinToString("") { "%02x".format(it) }
        return event.hash == digest
    }

    private fun candle(time: Long, close: Double) = AmarMarketCandle(time, close, close, close, close)
}
