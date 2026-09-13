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

    fun quantities(
        count: Int,
        baseLot: Double,
        multiplier: Double,
        quantityStep: Double = 0.0,
    ): List<Double> {
        require(count > 0)
        require(baseLot.isFinite() && baseLot > 0.0)
        require(multiplier.isFinite() && multiplier >= 1.0)
        require(quantityStep.isFinite() && quantityStep >= 0.0)
        require(quantityStep == 0.0 || multiplier == 1.0)

        return buildList {
            for (i in 1..count) {
                val volume = calculateVolume(i, baseLot, multiplier, quantityStep)
                require(volume.isFinite() && volume > 0.0)
                add(volume)
            }
        }
    }

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
                val buyPrice = referencePrice - priceOffset
                val sellPrice = referencePrice + priceOffset
                require(buyPrice.isFinite() && buyPrice > 0.0)
                require(sellPrice.isFinite() && sellPrice > 0.0)

                val volume = calculateVolume(i, baseLot, multiplier, quantityStep)
                require(volume.isFinite() && volume > 0.0)

                if (buyEnabled) add(Level(i, Side.BUY, buyPrice, volume))
                if (sellEnabled) add(Level(i, Side.SELL, sellPrice, volume))
            }
        }
    }

    private fun calculateVolume(
        index: Int,
        baseLot: Double,
        multiplier: Double,
        quantityStep: Double,
    ): Double = if (quantityStep > 0.0) {
        baseLot + quantityStep * (index - 1).toDouble()
    } else {
        baseLot * Math.pow(multiplier, (index - 1).toDouble())
    }
}
