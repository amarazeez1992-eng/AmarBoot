package com.personal.gridbot.amaros.agent

/** Provider-neutral indicator adapter. Implementations are deterministic and read-only. */
interface AmarIndicatorAdapter {
    val provenance: AmarSourceProvenance
    fun calculate(bars: List<AmarMarketBar>, request: AmarIndicatorRequest): AmarIndicatorResult
}

class AmarBuiltInIndicatorAdapter : AmarIndicatorAdapter {
    override val provenance = AmarSourceProvenance(
        sourceId = "amar-built-in-indicators",
        name = "AMAR AI Built-in Indicator Engine",
        version = "1.0",
        homepage = "https://github.com/amarazeez1992-eng/AmarBoot",
        sourceType = AmarSourceType.INDICATOR_ENGINE,
        license = AmarSourceLicense("Apache-2.0", "https://www.apache.org/licenses/LICENSE-2.0")
    )

    override fun calculate(bars: List<AmarMarketBar>, request: AmarIndicatorRequest): AmarIndicatorResult {
        require(bars.zipWithNext().all { it.first.timestampEpochMs < it.second.timestampEpochMs })
        val values = when (request.kind) {
            AmarIndicatorKind.SMA -> sma(bars, request.period)
            AmarIndicatorKind.EMA -> ema(bars, request.period)
            AmarIndicatorKind.RSI -> rsi(bars, request.period)
            AmarIndicatorKind.ATR -> atr(bars, request.period)
        }
        return AmarIndicatorResult(request.kind, request.period, values, provenance)
    }

    private fun sma(bars: List<AmarMarketBar>, period: Int): List<AmarIndicatorPoint> {
        if (bars.size < period) return emptyList()
        var sum = 0.0
        return buildList {
            for (i in bars.indices) {
                sum += bars[i].close
                if (i >= period) sum -= bars[i - period].close
                if (i >= period - 1) add(AmarIndicatorPoint(bars[i].timestampEpochMs, sum / period))
            }
        }
    }

    private fun ema(bars: List<AmarMarketBar>, period: Int): List<AmarIndicatorPoint> {
        if (bars.size < period) return emptyList()
        val alpha = 2.0 / (period + 1.0)
        var ema = bars.take(period).sumOf { it.close } / period
        return buildList {
            add(AmarIndicatorPoint(bars[period - 1].timestampEpochMs, ema))
            for (i in period until bars.size) {
                ema = (bars[i].close - ema) * alpha + ema
                add(AmarIndicatorPoint(bars[i].timestampEpochMs, ema))
            }
        }
    }

    private fun rsi(bars: List<AmarMarketBar>, period: Int): List<AmarIndicatorPoint> {
        if (bars.size <= period) return emptyList()
        var gains = 0.0
        var losses = 0.0
        for (i in 1..period) {
            val delta = bars[i].close - bars[i - 1].close
            if (delta >= 0.0) gains += delta else losses -= delta
        }
        var avgGain = gains / period
        var avgLoss = losses / period
        return buildList {
            fun value(): Double = when {
                avgLoss == 0.0 && avgGain == 0.0 -> 50.0
                avgLoss == 0.0 -> 100.0
                else -> 100.0 - (100.0 / (1.0 + avgGain / avgLoss))
            }
            add(AmarIndicatorPoint(bars[period].timestampEpochMs, value()))
            for (i in period + 1 until bars.size) {
                val delta = bars[i].close - bars[i - 1].close
                val gain = maxOf(delta, 0.0)
                val loss = maxOf(-delta, 0.0)
                avgGain = ((avgGain * (period - 1)) + gain) / period
                avgLoss = ((avgLoss * (period - 1)) + loss) / period
                add(AmarIndicatorPoint(bars[i].timestampEpochMs, value()))
            }
        }
    }

    private fun atr(bars: List<AmarMarketBar>, period: Int): List<AmarIndicatorPoint> {
        if (bars.size <= period) return emptyList()
        val tr = DoubleArray(bars.size)
        tr[0] = bars[0].high - bars[0].low
        for (i in 1 until bars.size) {
            tr[i] = maxOf(
                bars[i].high - bars[i].low,
                kotlin.math.abs(bars[i].high - bars[i - 1].close),
                kotlin.math.abs(bars[i].low - bars[i - 1].close)
            )
        }
        var atr = tr.take(period).average()
        return buildList {
            add(AmarIndicatorPoint(bars[period - 1].timestampEpochMs, atr))
            for (i in period until bars.size) {
                atr = ((atr * (period - 1)) + tr[i]) / period
                add(AmarIndicatorPoint(bars[i].timestampEpochMs, atr))
            }
        }
    }
}
