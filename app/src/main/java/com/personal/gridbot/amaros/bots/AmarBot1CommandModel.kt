package com.personal.gridbot.amaros.bots

/** B33: complete BOT 1 command vocabulary. Transport remains fail-closed. */
enum class AmarBot1CommandType {
    START,
    STOP,
    REBUILD,
    CLOSE_ALL,
    SET_BUY_ENABLED,
    SET_SELL_ENABLED,
    UPDATE_SETTINGS,
}

data class AmarBot1Settings(
    val lotStart: Double,
    val gridStep: Double,
    val maxOrders: Int,
    val martingale: Double,
    val basketTp: Double,
    val basketSl: Double,
    val trailing: Double,
    val buyEnabled: Boolean,
    val sellEnabled: Boolean,
) {
    init {
        require(lotStart.isFinite() && lotStart > 0.0)
        require(gridStep.isFinite() && gridStep > 0.0)
        require(maxOrders > 0)
        require(martingale.isFinite() && martingale > 0.0)
        require(basketTp.isFinite())
        require(basketSl.isFinite())
        require(trailing.isFinite() && trailing >= 0.0)
    }
}

data class AmarBot1ControlCommand(
    val type: AmarBot1CommandType,
    val settings: AmarBot1Settings? = null,
    val enabled: Boolean? = null,
) {
    init {
        if (type == AmarBot1CommandType.UPDATE_SETTINGS) requireNotNull(settings)
        if (type == AmarBot1CommandType.SET_BUY_ENABLED || type == AmarBot1CommandType.SET_SELL_ENABLED) requireNotNull(enabled)
    }
}
