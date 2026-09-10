package com.personal.gridbot.amaros.simulation

/**
 * محرك محاكاة تنفيذ متقدم مستقل عن التداول الحقيقي.
 * يدعم الانزلاق، زمن الاستجابة، التنفيذ الجزئي، اختلاف السبريد، وتسلسل الأحداث.
 * لا يتصل بأي وسيط ولا ينفذ أوامر حقيقية.
 */
class AmarMarketExecutionSimulation {
    data class Candle(
        val epochMs: Long,
        val open: Double,
        val high: Double,
        val low: Double,
        val close: Double,
    )

    data class SpreadPoint(val epochMs: Long, val spread: Double)

    data class Order(
        val id: String,
        val side: Side,
        val quantity: Double,
        val triggerPrice: Double,
        val executionLatencyMs: Long = 0L,
    )

    data class PartialFillPlan(val fractions: List<Double> = listOf(1.0)) {
        init {
            require(fractions.isNotEmpty())
            require(fractions.all { it > 0.0 })
            require(kotlin.math.abs(fractions.sum() - 1.0) < 0.000001)
        }
    }

    data class Config(
        val slippagePerUnit: Double = 0.0,
        val spreadPoints: List<SpreadPoint> = emptyList(),
        val partialFillPlan: PartialFillPlan = PartialFillPlan(),
    ) {
        init {
            require(slippagePerUnit >= 0.0)
            require(spreadPoints.all { it.spread >= 0.0 })
        }
    }

    enum class Side { BUY, SELL }
    enum class EventType { TRIGGER, EXECUTION, PARTIAL_FILL, COMPLETION }

    data class Event(
        val epochMs: Long,
        val sequence: Long,
        val orderId: String,
        val type: EventType,
        val quantity: Double = 0.0,
        val price: Double? = null,
    )

    data class Fill(
        val orderId: String,
        val sequence: Long,
        val epochMs: Long,
        val price: Double,
        val quantity: Double,
        val spread: Double,
        val slippage: Double,
    )

    data class Result(
        val fills: List<Fill>,
        val events: List<Event>,
        val requestedQuantity: Double,
        val filledQuantity: Double,
    )

    fun run(candles: List<Candle>, orders: List<Order>, config: Config): Result {
        require(candles.zipWithNext().all { it.first.epochMs <= it.second.epochMs })
        require(orders.all { it.quantity > 0.0 })
        val events = mutableListOf<Event>()
        val fills = mutableListOf<Fill>()
        var sequence = 0L

        fun nextSequence(): Long = ++sequence

        for (order in orders) {
            val trigger = candles.firstOrNull { candle ->
                when (order.side) {
                    Side.BUY -> candle.low <= order.triggerPrice
                    Side.SELL -> candle.high >= order.triggerPrice
                }
            } ?: continue

            events += Event(
                epochMs = trigger.epochMs,
                sequence = nextSequence(),
                orderId = order.id,
                type = EventType.TRIGGER,
                quantity = order.quantity,
                price = order.triggerPrice,
            )

            val executionTime = trigger.epochMs + order.executionLatencyMs
            events += Event(
                epochMs = executionTime,
                sequence = nextSequence(),
                orderId = order.id,
                type = EventType.EXECUTION,
                quantity = order.quantity,
                price = order.triggerPrice,
            )

            var filled = 0.0
            config.partialFillPlan.fractions.forEachIndexed { index, fraction ->
                val quantity = order.quantity * fraction
                val spread = spreadAt(config.spreadPoints, executionTime)
                val slippage = config.slippagePerUnit
                val halfSpread = spread / 2.0
                val price = when (order.side) {
                    Side.BUY -> order.triggerPrice + halfSpread + slippage
                    Side.SELL -> order.triggerPrice - halfSpread - slippage
                }
                filled += quantity
                val type = if (index == config.partialFillPlan.fractions.lastIndex) {
                    EventType.COMPLETION
                } else {
                    EventType.PARTIAL_FILL
                }
                val eventSequence = nextSequence()
                events += Event(
                    epochMs = executionTime,
                    sequence = eventSequence,
                    orderId = order.id,
                    type = type,
                    quantity = quantity,
                    price = price,
                )
                fills += Fill(
                    orderId = order.id,
                    sequence = eventSequence,
                    epochMs = executionTime,
                    price = price,
                    quantity = quantity,
                    spread = spread,
                    slippage = slippage,
                )
            }
        }

        return Result(
            fills = fills.sortedWith(compareBy<Fill> { it.epochMs }.thenBy { it.sequence }),
            events = events.sortedWith(compareBy<Event> { it.epochMs }.thenBy { it.sequence }),
            requestedQuantity = orders.sumOf { it.quantity },
            filledQuantity = filledQuantity(fills),
        )
    }

    private fun spreadAt(points: List<SpreadPoint>, epochMs: Long): Double =
        points.filter { it.epochMs <= epochMs }.maxByOrNull { it.epochMs }?.spread
            ?: points.minByOrNull { kotlin.math.abs(it.epochMs - epochMs) }?.spread
            ?: 0.0

    private fun filledQuantity(fills: List<Fill>): Double = fills.sumOf { it.quantity }
}
