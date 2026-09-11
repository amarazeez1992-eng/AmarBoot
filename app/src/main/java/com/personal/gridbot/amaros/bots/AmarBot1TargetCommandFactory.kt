package com.personal.gridbot.amaros.bots

/**
 * Builds broker-neutral BOT 1 intents with the currently selected execution symbol.
 * Transport and runtime verification remain outside the UI layer.
 */
object AmarBot1TargetCommandFactory {
    fun rebuild(): AmarBot1ControlCommand =
        AmarBot1ControlCommand(
            type = AmarBot1CommandType.REBUILD,
            targetSymbol = requireTargetSymbol(),
        )

    fun start(): AmarBot1ControlCommand =
        AmarBot1ControlCommand(
            type = AmarBot1CommandType.START,
            targetSymbol = requireTargetSymbol(),
        )

    fun stop(): AmarBot1ControlCommand =
        AmarBot1ControlCommand(
            type = AmarBot1CommandType.STOP,
            targetSymbol = requireTargetSymbol(),
        )

    fun closeAll(): AmarBot1ControlCommand =
        AmarBot1ControlCommand(
            type = AmarBot1CommandType.CLOSE_ALL,
            targetSymbol = requireTargetSymbol(),
        )

    fun update(settings: AmarBot1Settings): AmarBot1ControlCommand =
        AmarBot1ControlCommand(
            type = AmarBot1CommandType.UPDATE_SETTINGS,
            settings = settings,
            targetSymbol = requireTargetSymbol(),
        )

    private fun requireTargetSymbol(): String =
        AmarTradingSymbolContext.selected.brokerSymbol.trim().also {
            require(it.isNotEmpty()) { "Trading symbol must be selected before execution" }
        }
}
