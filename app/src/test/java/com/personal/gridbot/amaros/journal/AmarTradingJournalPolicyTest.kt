package com.personal.gridbot.amaros.journal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarTradingJournalPolicyTest {
    @Test
    fun summaryCalculatesCoreTradeMetrics() {
        val trades = listOf(
            AmarTradingJournalPolicy.TradeRecord("1", "Grid", 20.0, 2.0, 10),
            AmarTradingJournalPolicy.TradeRecord("2", "Grid", -10.0, 1.0, 5),
            AmarTradingJournalPolicy.TradeRecord("3", "Manual", 30.0, 3.0, 15),
        )

        assertTrue(AmarTradingJournalPolicy.validate(trades).isEmpty())
        val summary = AmarTradingJournalPolicy.summarize(trades)
        assertEquals(3, summary.trades)
        assertEquals(2, summary.wins)
        assertEquals(1, summary.losses)
        assertEquals(2.0 / 3.0, summary.winRate, 0.0001)
        assertEquals(40.0, summary.netProfitLoss, 0.0001)
        assertEquals(40.0 / 3.0, summary.averageProfitLoss, 0.0001)
        assertEquals(5.0, summary.profitFactor, 0.0001)
        assertEquals(2.0, summary.averageRiskReward, 0.0001)
    }

    @Test
    fun emptyJournalIsSafe() {
        val summary = AmarTradingJournalPolicy.summarize(emptyList())
        assertEquals(0, summary.trades)
        assertEquals(0.0, summary.winRate, 0.0001)
        assertEquals(0.0, summary.netProfitLoss, 0.0001)
        assertTrue(summary.profitFactor.isInfinite())
    }

    @Test
    fun invalidTradeIsRejected() {
        val trade = AmarTradingJournalPolicy.TradeRecord("", "", Double.NaN, -1.0, -1)
        val errors = AmarTradingJournalPolicy.validate(listOf(trade))
        assertTrue(errors.isNotEmpty())
    }

    @Test
    fun duplicateTradeIdsAreRejected() {
        val trades = listOf(
            AmarTradingJournalPolicy.TradeRecord("1", "Grid", 10.0, 1.0, 5),
            AmarTradingJournalPolicy.TradeRecord("1", "Manual", -5.0, 1.0, 3),
        )
        val errors = AmarTradingJournalPolicy.validate(trades)
        assertTrue("TRADE_1_ID_DUPLICATE" in errors)
    }

    @Test
    fun summaryRejectsProfitAggregationOverflow() {
        val trades = listOf(
            AmarTradingJournalPolicy.TradeRecord("1", "Grid", Double.MAX_VALUE, 1.0, 1),
            AmarTradingJournalPolicy.TradeRecord("2", "Grid", Double.MAX_VALUE, 1.0, 1),
        )
        assertTrue(AmarTradingJournalPolicy.validate(trades).isEmpty())
        assertTrue(runCatching { AmarTradingJournalPolicy.summarize(trades) }.isFailure)
    }

    @Test
    fun summaryRejectsLossAggregationOverflow() {
        val trades = listOf(
            AmarTradingJournalPolicy.TradeRecord("1", "Grid", -Double.MAX_VALUE, 1.0, 1),
            AmarTradingJournalPolicy.TradeRecord("2", "Grid", -Double.MAX_VALUE, 1.0, 1),
        )
        assertTrue(AmarTradingJournalPolicy.validate(trades).isEmpty())
        assertTrue(runCatching { AmarTradingJournalPolicy.summarize(trades) }.isFailure)
    }

    @Test
    fun summaryRejectsRiskRewardAggregationOverflow() {
        val trades = listOf(
            AmarTradingJournalPolicy.TradeRecord("1", "Grid", 1.0, Double.MAX_VALUE, 1),
            AmarTradingJournalPolicy.TradeRecord("2", "Grid", 1.0, Double.MAX_VALUE, 1),
        )
        assertTrue(AmarTradingJournalPolicy.validate(trades).isEmpty())
        assertTrue(runCatching { AmarTradingJournalPolicy.summarize(trades) }.isFailure)
    }
}
