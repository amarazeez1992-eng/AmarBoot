package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.simulation.AmarMarketExecutionSimulation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarMarketExecutionSimulationTest {
    @Test
    fun professionalExecutionModelAppliesSlippageSpreadLatencyAndPartialFills() {
        val engine = AmarMarketExecutionSimulation()
        val result = engine.run(
            candles = listOf(
                AmarMarketExecutionSimulation.Candle(1_000L, 100.0, 101.0, 99.0, 100.5),
                AmarMarketExecutionSimulation.Candle(2_000L, 100.5, 102.0, 100.0, 101.0),
            ),
            orders = listOf(
                AmarMarketExecutionSimulation.Order(
                    id = "A1",
                    side = AmarMarketExecutionSimulation.Side.BUY,
                    quantity = 1.0,
                    triggerPrice = 100.0,
                    executionLatencyMs = 500L,
                )
            ),
            config = AmarMarketExecutionSimulation.Config(
                slippagePerUnit = 0.25,
                spreadPoints = listOf(
                    AmarMarketExecutionSimulation.SpreadPoint(0L, 0.20),
                    AmarMarketExecutionSimulation.SpreadPoint(1_500L, 0.40),
                ),
                partialFillPlan = AmarMarketExecutionSimulation.PartialFillPlan(listOf(0.25, 0.35, 0.40)),
            )
        )

        assertEquals(1.0, result.requestedQuantity, 0.000001)
        assertEquals(1.0, result.filledQuantity, 0.000001)
        assertEquals(3, result.fills.size)
        assertEquals(1_500L, result.fills.first().epochMs)
        assertEquals(100.45, result.fills.first().price, 0.000001)
        assertEquals(0.40, result.fills.first().spread, 0.000001)
        assertEquals(0.25, result.fills.first().slippage, 0.000001)
        assertEquals(1L, result.events.first().sequence)
        assertTrue(result.events.zipWithNext().all { it.first.sequence < it.second.sequence })
    }
}
