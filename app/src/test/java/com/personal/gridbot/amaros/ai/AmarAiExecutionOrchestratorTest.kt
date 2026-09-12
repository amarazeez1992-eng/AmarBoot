package com.personal.gridbot.amaros.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiExecutionOrchestratorTest {
    @Test fun marketOrderRequiresExplicitSymbolSideAndVolume() {
        assertFalse(AmarAiExecutionOrchestrator.validate(AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.OPEN_MARKET, symbol = "XAUUSD", side = "BUY")).accepted)
        assertTrue(AmarAiExecutionOrchestrator.validate(AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.OPEN_MARKET, symbol = "XAUUSD", side = "BUY", volume = .01)).accepted)
    }

    @Test fun canonicalCommandIsBridgeReadyAndNeverClaimsExecution() {
        val command = AmarAiExecutionOrchestrator.canonical(AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.SET_STOP_LOSS, amountUsd = 30.0))
        assertTrue(command.contains("AMAR_EXECUTION_INTENT_V1"))
        assertTrue(command.contains("PENDING_MT5"))
        assertFalse(command.contains("EXECUTED"))
    }

    @Test fun naturalLanguageTradingCommandsBecomeGovernedIntents() {
        assertTrue(AmarAiActionEngine.route("افتح شراء XAUUSD لوت 0.01").handled)
        assertTrue(AmarAiActionEngine.route("عند الخسارة 30 دولار أغلق").handled)
        assertTrue(AmarAiActionEngine.route("عند الربح 2 دولار نفذ الأمر التالي").handled)
        assertTrue(AmarAiActionEngine.route("شغل الشبكة").handled)
        assertTrue(AmarAiActionEngine.route("شغل التتبع").handled)
    }
}
