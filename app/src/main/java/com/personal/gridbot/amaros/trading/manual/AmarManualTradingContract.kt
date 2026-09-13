package com.personal.gridbot.amaros.trading.manual

/** Android-side manual trading intent. It is a governed command model, not broker execution. */
data class AmarManualTradeIntent(
    val symbol: String,
    val side: Side,
    val orderType: OrderType,
    val volume: Double,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null
) {
    enum class Side { BUY, SELL }
    enum class OrderType { MARKET, PENDING }
}

data class AmarManualTradeValidation(
    val accepted: Boolean,
    val errors: List<String> = emptyList()
)

object AmarManualTradeValidator {
    fun validate(intent: AmarManualTradeIntent): AmarManualTradeValidation {
        val errors = buildList {
            if (intent.symbol.isBlank()) add("SYMBOL_REQUIRED")
            if (!intent.volume.isFinite() || intent.volume <= 0.0) add("VOLUME_INVALID")
            if (intent.stopLoss != null && (!intent.stopLoss.isFinite() || intent.stopLoss <= 0.0)) {
                add("STOP_LOSS_INVALID")
            }
            if (intent.takeProfit != null && (!intent.takeProfit.isFinite() || intent.takeProfit <= 0.0)) {
                add("TAKE_PROFIT_INVALID")
            }
        }
        return AmarManualTradeValidation(errors.isEmpty(), errors)
    }
}
