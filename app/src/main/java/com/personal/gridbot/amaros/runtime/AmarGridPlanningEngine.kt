package com.personal.gridbot.amaros.runtime

import kotlin.math.max

/** Pure, deterministic grid planner. It calculates real levels from strategy data and never sends orders. */
object AmarGridPlanningEngine {
    data class Level(
        val index: Int,
        val side: Side,
        val price: Double,
        val volume: Double
    )

    enum class Side { BUY, SELL }

    fun build(
        referencePrice: Double,
        step: Double,
        maxOrders: Int,
        baseLot: Double,
        multiplier: Double,
        buyEnabled: Boolean,
        sellEnabled: Boolean
    ): List<Level> {
        require(referencePrice.isFinite() && referencePrice > 0.0)
        require(step.isFinite() && step > 0.0)
        require(baseLot.isFinite() && baseLot > 0.0)
        require(multiplier.isFinite() && multiplier > 0.0)
        val count = max(1, maxOrders)
        return buildList {
            for (i in 1..count) {
                val volume = baseLot * Math.pow(multiplier, (i - 1).toDouble())
                if (buyEnabled) add(Level(i, Side.BUY, referencePrice - step * i, volume))
                if (sellEnabled) add(Level(i, Side.SELL, referencePrice + step * i, volume))
            }
        }
    }
}
