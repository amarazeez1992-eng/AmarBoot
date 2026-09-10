package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.bots.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarB34BotRuntimeTest {
    @Test fun reconciliationDetectsMatchAndDrift() {
        val identity = AmarBotIdentity()
        val config = AmarBot1RuntimeConfig()
        val desired = AmarBot1DesiredState(identity, AmarBotRuntimeState.RUNNING, config)
        val actual = AmarBot1ActualState(identity, AmarBotRuntimeState.RUNNING, config)
        assertEquals(AmarBotSyncState.MATCHED, reconcileBot1(desired, actual))
        assertEquals(AmarBotSyncState.DRIFT, reconcileBot1(desired, actual.copy(runtimeState = AmarBotRuntimeState.OFF)))
    }

    @Test fun strategyCatalogHasTenIndependentSlots() {
        val slots = AmarBot1StrategyCatalog.defaults()
        assertEquals(10, slots.size)
        assertEquals("STRATEGY_01", slots.first().strategyId)
        assertEquals("STRATEGY_10", slots.last().strategyId)
        assertEquals(10, slots.map { it.strategyId }.toSet().size)
    }

    @Test fun lifecycleTransitionsAreClosedAndTerminal() {
        assertTrue(AmarBot1CommandLifecycleRules.canMove(AmarBot1CommandStatus.CREATED, AmarBot1CommandStatus.VALIDATED))
        assertTrue(AmarBot1CommandLifecycleRules.canMove(AmarBot1CommandStatus.ACKNOWLEDGED, AmarBot1CommandStatus.VERIFIED))
        assertTrue(AmarBot1CommandLifecycleRules.isTerminal(AmarBot1CommandStatus.VERIFIED))
        assertTrue(!AmarBot1CommandLifecycleRules.canMove(AmarBot1CommandStatus.VERIFIED, AmarBot1CommandStatus.EXECUTING))
    }
}
