package com.personal.gridbot.amaros.grid

/** Independent grid hardening boundary. It validates planning inputs without executing trades. */
object AmarGridPolicy {
    enum class DirectionMode { BUY, SELL, BOTH }

    data class Config(
        val symbol: String,
        val anchorPrice: Double,
        val levelsPerSide: Int,
        val distance: Double,
        val baseQuantity: Double,
        val quantityStep: Double = 0.0,
        val quantityMultiplier: Double = 1.0,
        val direction: DirectionMode = DirectionMode.BOTH,
        val basketTakeProfit: Double = 0.0,
        val basketStopLoss: Double = 0.0,
    )

    fun validate(config: Config): List<String> {
        val errors = mutableListOf<String>()
        if (config.symbol.isBlank()) errors += "SYMBOL_REQUIRED"
        if (!config.anchorPrice.isFinite() || config.anchorPrice <= 0.0) errors += "ANCHOR_PRICE_INVALID"
        if (config.levelsPerSide <= 0) errors += "LEVEL_COUNT_INVALID"
        if (!config.distance.isFinite() || config.distance <= 0.0) errors += "GRID_DISTANCE_INVALID"
        if (!config.baseQuantity.isFinite() || config.baseQuantity <= 0.0) errors += "BASE_QUANTITY_INVALID"
        if (!config.quantityStep.isFinite() || config.quantityStep < 0.0) errors += "QUANTITY_STEP_INVALID"
        if (!config.quantityMultiplier.isFinite() || config.quantityMultiplier < 1.0) errors += "QUANTITY_MULTIPLIER_INVALID"
        if (!config.basketTakeProfit.isFinite() || config.basketTakeProfit < 0.0) errors += "BASKET_TP_INVALID"
        if (!config.basketStopLoss.isFinite() || config.basketStopLoss < 0.0) errors += "BASKET_SL_INVALID"
        if (config.levelsPerSide > 0 && config.distance > 0.0 && config.anchorPrice.isFinite()) {
            val farthest = config.anchorPrice + config.distance * config.levelsPerSide
            if (!farthest.isFinite()) errors += "GRID_RANGE_OVERFLOW"
        }
        return errors
    }

    fun quantities(config: Config): List<Double> {
        require(validate(config).isEmpty())
        return (0 until config.levelsPerSide).map { index ->
            val stepped = config.baseQuantity + config.quantityStep * index
            val quantity = stepped * config.quantityMultiplier.pow(index)
            require(quantity.isFinite() && quantity > 0.0)
            quantity
        }
    }

    private fun Double.pow(exponent: Int): Double {
        var result = 1.0
        repeat(exponent) { result *= this }
        return result
    }
}
