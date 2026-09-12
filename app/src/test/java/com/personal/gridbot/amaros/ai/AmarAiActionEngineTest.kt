package com.personal.gridbot.amaros.ai

import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiActionEngineTest {
    @Test fun settingsNavigationIsRecognized() {
        val result = AmarAiActionEngine.route("اذهب إلى الإعدادات")
        assertTrue(result.handled)
        assertTrue(result.response.contains("الإعدادات"))
    }

    @Test fun botLotCommandIsGoverned() {
        val result = AmarAiActionEngine.route("ارفع لوت البوت 2 إلى 0.03")
        assertTrue(result.handled)
        assertTrue(result.response.contains("PENDING_MT5"))
    }

    @Test fun incompleteLotRequestDoesNotInventValue() {
        val result = AmarAiActionEngine.route("ارفع مستوى اللوت")
        assertTrue(result.handled)
        assertTrue(result.response.contains("أعطني القيمة المطلوبة"))
    }
}
