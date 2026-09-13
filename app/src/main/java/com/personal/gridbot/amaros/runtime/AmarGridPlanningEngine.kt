package com.personal.gridbot.amaros.runtime

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
        sellEnabled: Boolean,
        quantityStep: Double = 0.0,
    ): List<Level> {
        require(referencePrice.isFinite() && referencePrice > 0.0)
        require(step.isFinite() && step > 0.0)
        require(maxOrders > 0)
        require(baseLot.isFinite() && baseLot > 0.0)
        require(multiplier.isFinite() && multiplier >= 1.0)
        require(quantityStep.isFinite() && quantityStep >= 0.0)
        require(quantityStep == 0.0 || multiplier == 1.0)

        return buildList {
            for (i in 1..maxOrders) {
                val priceOffset = step * i.toDouble()
                val price = if (buyEnabled) referencePrice - priceOffset else referencePrice + priceOffset
                val buyPrice = referencePrice - priceOffset
                val sellPrice = referencePrice + priceOffset
                require(buyPrice.isFinite() && buyPrice > 0.0)
                require(sellPrice.isFinite() && sellPrice > 0.0)

                val volume = if (quantityStep > 0.0) {
                    baseLot + quantityStep * (i - 1).toDouble()
                } else {
                    baseLot * Math.pow(multiplier, (i - 1).toDouble())
                }
                require(volume.isFinite() && volume > 0.0)

                if (buyEnabled) add(Level(i, Side.BUY, price, volume))
                if (sellEnabled) add(Level(i, Side.SELL, sellPrice, volume))
            }
        }
    }
}
