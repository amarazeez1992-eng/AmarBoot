package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.broker.AmarBot1RemoteState
import com.personal.gridbot.amaros.broker.AmarBot1VerifiedRuntime
import com.personal.gridbot.amaros.bots.AmarBot1DesiredState
import com.personal.gridbot.amaros.bots.AmarBot1RuntimeConfig
import com.personal.gridbot.amaros.bots.AmarBotIdentity
import com.personal.gridbot.amaros.bots.AmarBotRuntimeState
import com.personal.gridbot.amaros.bots.AmarBotSyncState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarBot1VerifiedRuntimeTest {
    private val desired = AmarBot1DesiredState(
        identity = AmarBotIdentity(),
        runtimeState = AmarBotRuntimeState.RUNNING,
        config = AmarBot1RuntimeConfig()
    )

    private fun liveState() = AmarBot1RemoteState(
        available = true,
        fresh = true,
        botId = "BOT_1",
        magic = 20260908L,
        strategyId = "STRATEGY_01",
        strategyVersion = "2.00",
        runtimeState = "RUNNING",
        targetSymbol = "XAUUSD",
        chartSymbol = "XAUUSD",
        isTrading = true,
        buyEnabled = true,
        sellEnabled = true,
        lotStart = 0.01,
        gridStep = 30.0,
        maxOrders = 10,
        martingale = 2.0,
        basketTp = 50.0,
        basketSl = -30.0,
        trailing = 0,
        marketReady = true,
        heartbeatMs = System.currentTimeMillis(),
    )

    @Test fun exactReadBackIsVerified() {
        val runtime = AmarBot1VerifiedRuntime()
        assertEquals(AmarBotSyncState.MATCHED, runtime.reconcile(desired, liveState()))
        assertTrue(runtime.isVerified(desired, liveState()))
    }

    @Test fun staleHeartbeatFailsClosed() {
        val state = liveState().copy(fresh = false)
        assertEquals(AmarBotSyncState.UNKNOWN, AmarBot1VerifiedRuntime().reconcile(desired, state))
    }

    @Test fun staleMarketFailsClosed() {
        val state = liveState().copy(marketReady = false)
        assertEquals(AmarBotSyncState.UNKNOWN, AmarBot1VerifiedRuntime().reconcile(desired, state))
    }

    @Test fun configurationDriftIsDetected() {
        val state = liveState().copy(lotStart = 0.02)
        assertEquals(AmarBotSyncState.DRIFT, AmarBot1VerifiedRuntime().reconcile(desired, state))
    }

    @Test fun strategyIdentityDriftFailsClosed() {
        val state = liveState().copy(strategyVersion = "9.99")
        assertEquals(AmarBotSyncState.ERROR, AmarBot1VerifiedRuntime().reconcile(desired, state))
    }
}
