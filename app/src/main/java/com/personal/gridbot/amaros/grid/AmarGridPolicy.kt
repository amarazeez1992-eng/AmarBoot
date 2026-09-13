package com.personal.gridbot.amaros.grid

import com.personal.gridbot.amaros.runtime.AmarGridPlanningEngine

/**
 * Governed grid configuration boundary. Actual level generation is delegated to the
 * existing shared grid planner so Android and AI cannot maintain two different grid algorithms.
 */
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
        val errors = validateBasic(config)
        if (errors.isNotEmpty()) return errors

        return try {
            plan(config)
            emptyList()
        } catch (_: IllegalArgumentException) {
            listOf("GRID_PLAN_INVALID")
        }
    }

    fun plan(config: Config): List<AmarGridPlanningEngine.Level> {
        require(validateBasic(config).isEmpty())
        return AmarGridPlanningEngine.build(
            referencePrice = config.anchorPrice,
            step = config.distance,
            maxOrders = config.levelsPerSide,
            baseLot = config.baseQuantity,
            multiplier = config.quantityMultiplier,
            buyEnabled = config.direction != DirectionMode.SELL,
            sellEnabled = config.direction != DirectionMode.BUY,
            quantityStep = config.quantityStep,
        )
    }

    /** Returns the lot progression itself, independent of BUY/SELL interleaving in the plan. */
    fun quantities(config: Config): List<Double> {
        require(validateBasic(config).isEmpty())
        return try {
            (1..config.levelsPerSide).map { index -> quantityAt(config, index) }
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("GRID_PLAN_INVALID")
        }
    }

    private fun quantityAt(config: Config, index: Int): Double {
        val quantity = if (config.quantityStep > 0.0) {
            config.baseQuantity + config.quantityStep * (index - 1).toDouble()
        } else {
            config.baseQuantity * Math.pow(config.quantityMultiplier, (index - 1).toDouble())
        }
        require(quantity.isFinite() && quantity > 0.0)
        return quantity
    }

    private fun validateBasic(config: Config): List<String> = buildList {
        if (config.symbol.isBlank()) add("SYMBOL_REQUIRED")
        if (!config.anchorPrice.isFinite() || config.anchorPrice <= 0.0) add("ANCHOR_PRICE_INVALID")
        if (config.levelsPerSide <= 0) add("LEVEL_COUNT_INVALID")
        if (!config.distance.isFinite() || config.distance <= 0.0) add("GRID_DISTANCE_INVALID")
        if (!config.baseQuantity.isFinite() || config.baseQuantity <= 0.0) add("BASE_QUANTITY_INVALID")
        if (!config.quantityStep.isFinite() || config.quantityStep < 0.0) add("QUANTITY_STEP_INVALID")
        if (!config.quantityMultiplier.isFinite() || config.quantityMultiplier < 1.0) add("QUANTITY_MULTIPLIER_INVALID")
        if (config.quantityStep > 0.0 && config.quantityMultiplier != 1.0) add("QUANTITY_RULE_CONFLICT")
        if (!config.basketTakeProfit.isFinite() || config.basketTakeProfit < 0.0) add("BASKET_TP_INVALID")
        if (!config.basketStopLoss.isFinite() || config.basketStopLoss < 0.0) add("BASKET_SL_INVALID")
    }
}
