package com.personal.gridbot.amaros.bots

import org.junit.Assert.assertEquals
import org.junit.Test

class AmarBot1TargetCommandFactoryTest {
    @Test
    fun rebuildCarriesSelectedSymbol() {
        val command = AmarBot1TargetCommandFactory.rebuild()
        assertEquals(AmarTradingSymbolContext.selected.brokerSymbol, command.targetSymbol)
        assertEquals(AmarBot1CommandType.REBUILD, command.type)
    }

    @Test
    fun startCarriesSelectedSymbol() {
        val command = AmarBot1TargetCommandFactory.start()
        assertEquals("XAUUSD", command.targetSymbol)
        assertEquals(AmarBot1CommandType.START, command.type)
    }
}
