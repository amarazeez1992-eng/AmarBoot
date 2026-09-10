package com.personal.gridbot.amaros.broker

import com.personal.gridbot.amaros.chart.AmarCandle
import com.personal.gridbot.amaros.chart.AmarMarketDataProvider
import com.personal.gridbot.amaros.chart.AmarTimeframe
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** B29/B32: real MT5 market-data provider with explicit refresh and atomic cache replacement. */
class AmarMt5LiveMarketDataProvider(
    private val client: AmarMt5BridgeClient,
    private val count: Int = 160,
) : AmarMarketDataProvider {
    init { require(count in 10..500) { "عدد الشموع يجب أن يكون بين 10 و500" } }

    private val mutex = Mutex()
    private val cache = LinkedHashMap<String, List<AmarCandle>>()

    override fun candles(symbol: String, timeframe: AmarTimeframe): List<AmarCandle> =
        cache[cacheKey(symbol, timeframe)].orEmpty()

    suspend fun refresh(symbol: String, timeframe: AmarTimeframe): List<AmarCandle> {
        val series = client.candles(symbol, timeframe.shortLabel, count)
        require(series.ok) { "جسر MT5 لم يؤكد بيانات الشموع" }
        require(series.symbol == symbol) { "رمز الشموع لا يطابق الطلب" }
        require(series.items.zipWithNext().all { (a, b) -> a.timestampMs <= b.timestampMs }) { "ترتيب الشموع غير صالح" }
        val converted = series.items.map {
            AmarCandle(it.timestampMs, it.open, it.high, it.low, it.close, it.tickVolume.toDouble())
        }
        mutex.withLock { cache[cacheKey(symbol, timeframe)] = converted }
        return converted
    }

    suspend fun clear() = mutex.withLock { cache.clear() }

    private fun cacheKey(symbol: String, timeframe: AmarTimeframe) = "${symbol.trim()}:${timeframe.shortLabel}"
}
