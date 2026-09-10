package com.personal.gridbot.amaros.broker

/** Pure B30 analysis of real MT5 candles. It never changes bot settings or execution state. */
data class AmarMarketDirection(
    val labelAr: String,
    val percentage: Int,
)

object AmarMt5MarketAnalysis {
    fun direction(candles: List<AmarMt5Candle>): AmarMarketDirection {
        if (candles.size < 10) return AmarMarketDirection("محايد", 50)

        val ordered = candles.sortedBy { it.timestampMs }.takeLast(30)
        val returns = ordered.zipWithNext().mapNotNull { (a, b) ->
            if (a.close > 0.0 && b.close > 0.0) (b.close - a.close) / a.close else null
        }
        if (returns.isEmpty()) return AmarMarketDirection("محايد", 50)

        val average = returns.average()
        val positiveRatio = returns.count { it > 0.0 }.toDouble() / returns.size
        val negativeRatio = returns.count { it < 0.0 }.toDouble() / returns.size
        val score = (50.0 + average * 5000.0 + (positiveRatio - negativeRatio) * 25.0)
            .coerceIn(0.0, 100.0)
            .toInt()

        return when {
            score >= 60 -> AmarMarketDirection("صاعد", score)
            score <= 40 -> AmarMarketDirection("هابط", 100 - score)
            else -> AmarMarketDirection("محايد", score)
        }
    }

    fun summary(directions: List<AmarMarketDirection>): Triple<Int, Int, Int> {
        if (directions.isEmpty()) return Triple(0, 0, 0)
        val buy = directions.filter { it.labelAr == "صاعد" }.map { it.percentage }.average().toIntOrNullSafe()
        val sell = directions.filter { it.labelAr == "هابط" }.map { it.percentage }.average().toIntOrNullSafe()
        val neutral = directions.filter { it.labelAr == "محايد" }.map { 100 - it.percentage }.average().toIntOrNullSafe()
        return Triple(buy, neutral, sell)
    }

    private fun Double.toIntOrNullSafe(): Int = if (isFinite()) coerceIn(0.0, 100.0).toInt() else 0
}
