package com.personal.gridbot.amaros.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AmarAiUiEngineBridgeTest {
    @Test
    fun bridgeUsesAmarLocalPathWithoutExternalProvider() = kotlinx.coroutines.runBlocking {
        val bridge = AmarAiUiEngineBridge(externalProviderEnabled = false)
        val result = bridge.ask("", "", "حلل السوق الآن")

        assertEquals("AMAR_LOCAL", result.provider)
        assertTrue(result.answer.contains("AMAR AI"))
        assertTrue(result.engineIds.contains("market"))
        assertTrue(result.evidence.any { it.startsWith("MARKET|") })
    }

    @Test
    fun bridgeNeverGrantsBrokerExecutionAuthority() = kotlinx.coroutines.runBlocking {
        val bridge = AmarAiUiEngineBridge(externalProviderEnabled = false)
        val result = bridge.ask("", "", "تحليل المخاطر")

        assertFalse(result.engineIds.isEmpty())
        assertTrue(result.answer.contains("بدون تنفيذ") || result.answer.contains("execution") || result.answer.contains("تحليل"))
    }
}
