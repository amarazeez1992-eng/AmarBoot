package com.personal.gridbot.amaros.grid

/** B23: deterministic AMAR GRID planner; execution remains outside the domain layer. */
class AmarGridEngine {
    enum class Direction { BUY, SELL }

    data class GridConfig(
        val symbol: String,
        val anchorPrice: Double,
        val levels: Int,
        val distance: Double,
        val quantity: Double,
        val direction: Direction,
        val stopLossDistance: Double = 0.0,
        val takeProfitDistance: Double = 0.0,
    )

    data class GridLevel(
        val index: Int,
        val price: Double,
        val direction: Direction,
        val quantity: Double,
        val stopLoss: Double?,
        val takeProfit: Double?,
    )

    data class GridPlan(val symbol: String, val anchorPrice: Double, val levels: List<GridLevel>)

    fun build(config: GridConfig): GridPlan {
        require(config.symbol.isNotBlank())
        require(config.anchorPrice.isFinite() && config.anchorPrice > 0.0)
        require(config.levels > 0)
        require(config.distance.isFinite() && config.distance > 0.0)
        require(config.quantity.isFinite() && config.quantity > 0.0)
        require(config.stopLossDistance.isFinite() && config.stopLossDistance >= 0.0)
        require(config.takeProfitDistance.isFinite() && config.takeProfitDistance >= 0.0)
        val levels = (1..config.levels).map { index ->
            val price = when (config.direction) {
                Direction.BUY -> config.anchorPrice - config.distance * index
                Direction.SELL -> config.anchorPrice + config.distance * index
            }
            require(price.isFinite() && price > 0.0) { "نتج مستوى سعري غير صالح" }
            val sl = if (config.stopLossDistance > 0.0) when (config.direction) {
                Direction.BUY -> price - config.stopLossDistance
                Direction.SELL -> price + config.stopLossDistance
            } else null
            val tp = if (config.takeProfitDistance > 0.0) when (config.direction) {
                Direction.BUY -> price + config.takeProfitDistance
                Direction.SELL -> price - config.takeProfitDistance
            } else null
            require(sl == null || sl.isFinite() && sl > 0.0)
            require(tp == null || tp.isFinite() && tp > 0.0)
            GridLevel(index, price, config.direction, config.quantity, sl, tp)
        }
        return GridPlan(config.symbol, config.anchorPrice, levels)
    }
}
