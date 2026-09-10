package com.personal.gridbot.amaros.simulation

/** Advanced deterministic execution simulation; it never connects to a broker. */
class AmarMarketExecutionSimulation {
    data class Candle(val epochMs: Long, val open: Double, val high: Double, val low: Double, val close: Double) {
        init {
            require(epochMs >= 0)
            require(open.isFinite() && high.isFinite() && low.isFinite() && close.isFinite())
            require(high >= maxOf(open, close, low) && low <= minOf(open, close, high))
        }
    }
    data class SpreadPoint(val epochMs: Long, val spread: Double) {
        init { require(epochMs >= 0); require(spread.isFinite() && spread >= 0.0) }
    }
    data class Order(val id: String, val side: Side, val quantity: Double, val triggerPrice: Double, val executionLatencyMs: Long = 0L) {
        init { require(id.isNotBlank()); require(quantity.isFinite() && quantity > 0.0); require(triggerPrice.isFinite() && triggerPrice > 0.0); require(executionLatencyMs >= 0L) }
    }
    data class PartialFillPlan(val fractions: List<Double> = listOf(1.0)) {
        init { require(fractions.isNotEmpty()); require(fractions.all { it.isFinite() && it > 0.0 }); require(kotlin.math.abs(fractions.sum() - 1.0) < 0.000001) }
    }
    data class Config(val slippagePerUnit: Double = 0.0, val spreadPoints: List<SpreadPoint> = emptyList(), val partialFillPlan: PartialFillPlan = PartialFillPlan()) {
        init { require(slippagePerUnit.isFinite() && slippagePerUnit >= 0.0); require(spreadPoints.zipWithNext().all { it.first.epochMs <= it.second.epochMs }) }
    }
    enum class Side { BUY, SELL }
    enum class EventType { TRIGGER, EXECUTION, PARTIAL_FILL, COMPLETION }
    data class Event(val epochMs: Long, val sequence: Long, val orderId: String, val type: EventType, val quantity: Double = 0.0, val price: Double? = null)
    data class Fill(val orderId: String, val sequence: Long, val epochMs: Long, val price: Double, val quantity: Double, val spread: Double, val slippage: Double)
    data class Result(val fills: List<Fill>, val events: List<Event>, val requestedQuantity: Double, val filledQuantity: Double)

    fun run(candles: List<Candle>, orders: List<Order>, config: Config): Result {
        require(candles.zipWithNext().all { it.first.epochMs <= it.second.epochMs })
        require(orders.map { it.id }.distinct().size == orders.size) { "معرفات الأوامر يجب أن تكون فريدة" }
        val events = mutableListOf<Event>(); val fills = mutableListOf<Fill>(); var sequence = 0L
        fun nextSequence() = ++sequence
        for (order in orders) {
            val trigger = candles.firstOrNull { candle -> when (order.side) { Side.BUY -> candle.low <= order.triggerPrice; Side.SELL -> candle.high >= order.triggerPrice } } ?: continue
            events += Event(trigger.epochMs, nextSequence(), order.id, EventType.TRIGGER, order.quantity, order.triggerPrice)
            val executionTime = trigger.epochMs + order.executionLatencyMs
            events += Event(executionTime, nextSequence(), order.id, EventType.EXECUTION, order.quantity, order.triggerPrice)
            config.partialFillPlan.fractions.forEachIndexed { index, fraction ->
                val quantity = order.quantity * fraction
                val spread = spreadAt(config.spreadPoints, executionTime)
                val slippage = config.slippagePerUnit
                val price = when (order.side) { Side.BUY -> order.triggerPrice + spread / 2.0 + slippage; Side.SELL -> order.triggerPrice - spread / 2.0 - slippage }
                require(price.isFinite() && price > 0.0) { "نتج سعر تنفيذ غير صالح" }
                val eventSequence = nextSequence()
                events += Event(executionTime, eventSequence, order.id, if (index == config.partialFillPlan.fractions.lastIndex) EventType.COMPLETION else EventType.PARTIAL_FILL, quantity, price)
                fills += Fill(order.id, eventSequence, executionTime, price, quantity, spread, slippage)
            }
        }
        return Result(fills.sortedWith(compareBy<Fill> { it.epochMs }.thenBy { it.sequence }), events.sortedWith(compareBy<Event> { it.epochMs }.thenBy { it.sequence }), orders.sumOf { it.quantity }, fills.sumOf { it.quantity })
    }

    private fun spreadAt(points: List<SpreadPoint>, epochMs: Long): Double = points.filter { it.epochMs <= epochMs }.maxByOrNull { it.epochMs }?.spread ?: points.minByOrNull { kotlin.math.abs(it.epochMs - epochMs) }?.spread ?: 0.0
}
