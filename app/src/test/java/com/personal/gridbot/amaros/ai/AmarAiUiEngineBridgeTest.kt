package com.personal.gridbot.amaros.ai

import kotlin.test.Test
import kotlin.test.assertTrue

class AmarAiUiEngineBridgeTest {
    @Test
    fun bridgeUsesAmarLocalPathWithoutExternalProvider() = kotlinx.coroutines.runBlocking {
        val bridge = AmarAiUiEngineBridge()
        val result = bridge.ask("", "", "حلل السوق الآن")

        assertTrue(result.provider == "AMAR_LOCAL" || result.provider == "AMAR_LOCAL_FAIL_CLOSED")
        assertTrue(result.answer.contains("AMAR AI") || result.answer.contains("حالة الوكيل"))
        assertTrue(result.engineIds.contains("market"))
        assertTrue(result.evidence.any { it.startsWith("ENGINE_MARKET|") } || result.provider == "AMAR_LOCAL_FAIL_CLOSED")
    }

    @Test
    fun bridgeKeepsExecutionOutsideUiAuthority() = kotlinx.coroutines.runBlocking {
        val bridge = AmarAiUiEngineBridge()
        val result = bridge.ask("", "", "تحليل المخاطر")

        assertTrue(result.engineIds.contains("precision"))
        assertTrue(result.answer.contains("بدون تنفيذ") || result.answer.contains("تحليل") || result.provider == "AMAR_LOCAL_FAIL_CLOSED")
    }
}
