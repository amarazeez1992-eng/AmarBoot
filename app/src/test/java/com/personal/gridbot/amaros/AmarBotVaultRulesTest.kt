package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.bots.AmarBot1RuntimeConfig
import com.personal.gridbot.amaros.bots.AmarSavedBot
import com.personal.gridbot.amaros.bots.AmarSavedStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarBotVaultRulesTest {
    @Test fun botNormalization_keepsOnlyTenStrategySlots() {
        val strategies = (1..12).map { AmarSavedStrategy(it, "S$it", AmarBot1RuntimeConfig()) }
        val normalized = AmarSavedBot(2, "", strategies).normalized()
        assertEquals("بوت 2", normalized.name)
        assertEquals(10, normalized.strategies.size)
        assertEquals(listOf(1,2,3,4,5,6,7,8,9,10), normalized.strategies.map { it.number })
    }

    @Test fun botNormalization_removesDuplicateStrategyNumbers() {
        val a = AmarSavedStrategy(1, "A", AmarBot1RuntimeConfig())
        val b = AmarSavedStrategy(1, "B", AmarBot1RuntimeConfig())
        val normalized = AmarSavedBot(1, "BOT", listOf(a, b)).normalized()
        assertEquals(1, normalized.strategies.size)
        assertEquals("A", normalized.strategies.first().name)
    }

    @Test fun profileMetadata_roundTripsThroughJson() {
        val original = AmarSavedStrategy(4, "Gold", AmarBot1RuntimeConfig(), "محافظ", "بعد الإغلاق", "يدوي", "ملاحظة")
        val restored = AmarSavedStrategy.fromJson(original.toJson())
        assertEquals(original.number, restored.number)
        assertEquals(original.name, restored.name)
        assertEquals(original.riskProfile, restored.riskProfile)
        assertEquals(original.rebuildRule, restored.rebuildRule)
        assertEquals(original.entryRule, restored.entryRule)
        assertEquals(original.metadata, restored.metadata)
        assertTrue(restored.profile.lot > 0.0)
    }
}
