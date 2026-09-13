package com.personal.gridbot.amaros.trading.manual

/** Android-side manual trading intent. It is a governed command model, not broker execution. */
data class AmarManualTradeIntent(
    val symbol: String,
    val side: Side,
    val orderType: OrderType,
    val volume: Double,
    val entryPrice: Double? = null,
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
            when {
                intent.orderType == AmarManualTradeIntent.OrderType.PENDING && intent.entryPrice == null ->
                    add("ENTRY_PRICE_REQUIRED")
                intent.orderType == AmarManualTradeIntent.OrderType.MARKET && intent.entryPrice != null ->
                    add("ENTRY_PRICE_NOT_ALLOWED_FOR_MARKET")
                intent.entryPrice != null && (!intent.entryPrice.isFinite() || intent.entryPrice <= 0.0) ->
                    add("ENTRY_PRICE_INVALID")
            }
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
