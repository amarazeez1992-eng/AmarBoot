package com.personal.gridbot.amaros.bots

/** B36: BOT 1 command vocabulary with explicit execution-symbol context. */
enum class AmarBot1CommandType {
    START,
    STOP,
    REBUILD,
    CLOSE_ALL,
    SET_BUY_ENABLED,
    SET_SELL_ENABLED,
    UPDATE_SETTINGS,
    EMERGENCY_LOCK,
    CLEAR_EMERGENCY_LOCK,
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
    val targetSymbol: String? = null,
) {
    init {
        if (type == AmarBot1CommandType.UPDATE_SETTINGS) requireNotNull(settings)
        if (type == AmarBot1CommandType.SET_BUY_ENABLED || type == AmarBot1CommandType.SET_SELL_ENABLED) requireNotNull(enabled)
        if (type == AmarBot1CommandType.EMERGENCY_LOCK) require(enabled != false)
        targetSymbol?.let {
            require(it.isNotBlank())
            require(!it.contains('\n') && !it.contains('\r'))
        }
    }
}
