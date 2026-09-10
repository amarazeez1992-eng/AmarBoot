package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.api.DemoAmarTradingApi
import com.personal.gridbot.amaros.api.AmarGridRequest
import com.personal.gridbot.amaros.broker.AmarBrokerCommand
import com.personal.gridbot.amaros.broker.BlockedLiveBrokerAdapter
import com.personal.gridbot.amaros.broker.Side as BrokerSide
import com.personal.gridbot.amaros.grid.AmarGridEngine
import com.personal.gridbot.amaros.simulation.AmarSimulationEngine
import com.personal.gridbot.amaros.testing.AmarTestingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarB21B25Test {
    @Test fun testingEngine_producesDeterministicReport() {
        var now = 100L
        val report = AmarTestingEngine().run(listOf(
            AmarTestingEngine.TestCase("اختبار", { 7 }, { it == 7 })
        )) { now++ }
        assertTrue(report.passed)
        assertEquals(1, report.passedCount)
        assertEquals(0, report.failedCount)
    }

    @Test fun simulationEngine_neverUsesLiveMode() {
        val engine = AmarSimulationEngine()
        val result = engine.run(1000.0, listOf(AmarSimulationEngine.Candle(1, 10.0, 11.0, 9.0, 10.5)), listOf(
            AmarSimulationEngine.Order("1", "XAUUSD", AmarSimulationEngine.Side.BUY, 1.0, 9.5)
        ))
        assertEquals(com.personal.gridbot.amaros.core.AmarOperatingMode.SIMULATION, result.mode)
        assertEquals(1001.0, result.endingBalance, 0.0001)
    }

    @Test fun gridEngine_buildsStableLevels() {
        val plan = AmarGridEngine().build(AmarGridEngine.GridConfig("XAUUSD", 100.0, 3, 5.0, 0.01, AmarGridEngine.Direction.BUY))
        assertEquals(listOf(95.0, 90.0, 85.0), plan.levels.map { it.price })
    }

    @Test fun api_isDemoOnly() {
        val api = DemoAmarTradingApi()
        val status = api.status()
        assertFalse(status.connected)
        assertFalse(status.executionEnabled)
        assertTrue(api.planGrid(AmarGridRequest("XAUUSD", 100.0, 2, 5.0, 0.01, AmarGridEngine.Direction.SELL)).accepted)
    }

    @Test fun brokerAdapter_failClosed() {
        val result = BlockedLiveBrokerAdapter().submit(AmarBrokerCommand("r1", "XAUUSD", BrokerSide.BUY, 0.01))
        assertFalse(result.accepted)
        assertFalse(result.executed)
    }
}
