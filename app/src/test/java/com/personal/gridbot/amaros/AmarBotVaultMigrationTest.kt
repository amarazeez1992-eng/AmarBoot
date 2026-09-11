package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.bots.AmarSavedStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarBotVaultMigrationTest {
    @Test
    fun malformedStrategyDefaultsToSafeRuntimeValues() {
        val strategy = AmarSavedStrategy.fromJsonString(
            """{"number":0,"name":"","lot":0,"step":0,"max":0,"multiplier":0,"tp":0,"sl":0,"trailing":-1,"buy":false,"sell":false}"""
        )

        assertTrue(strategy.number in 1..10)
        assertEquals(0.01, strategy.profile.lot, 0.0000001)
        assertEquals(30.0, strategy.profile.gridStep, 0.0000001)
        assertEquals(1, strategy.profile.maxOrders)
        assertEquals(2.0, strategy.profile.multiplier, 0.0000001)
        assertTrue(strategy.profile.trailing >= 0.0)
    }
}
