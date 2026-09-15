package com.personal.gridbot.amaros.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AmarAiUiEngineBridgeTest {
    @Test
    fun bridgeUsesAmarLocalPathWithoutExternalProvider() = kotlinx.coroutines.runBlocking {
        val bridge = AmarAiUiEngineBridge(externalProviderEnabled = false)
        val result = bridge.ask("", "", "حلل السوق الآن")

        assertEquals("AMAR_LOCAL", result.provider)
        assertTrue(result.answer.contains("AMAR AI"))
        assertTrue(result.engineIds.contains("market"))
        assertTrue(result.evidence.any { it.startsWith("ENGINE_MARKET|") })
    }

    @Test
    fun bridgeKeepsExecutionOutsideUiAuthority() = kotlinx.coroutines.runBlocking {
        val bridge = AmarAiUiEngineBridge(externalProviderEnabled = false)
        val result = bridge.ask("", "", "تحليل المخاطر")

        assertTrue(result.engineIds.contains("precision"))
        assertTrue(result.answer.contains("بدون تنفيذ") || result.answer.contains("تحليل"))
    }
}
