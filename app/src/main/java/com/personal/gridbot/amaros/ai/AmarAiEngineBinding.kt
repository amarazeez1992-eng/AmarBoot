package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import com.personal.gridbot.amaros.broker.AmarMt5RuntimeRegistry
import com.personal.gridbot.amaros.chart.AmarTimeframe
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingPrecisionEngine
import com.personal.gridbot.amaros.runtime.AmarGridPlanningEngine
import com.personal.gridbot.amaros.ai.AmarStrategyValidationEngine

/** Direct AI -> real application engines boundary. Read-only for broker/MT5 until final laptop/CMG/Bridge/MT5 phase. */
object AmarAiEngineBinding {
    fun market(): String {
        val s = AmarMarketStateStore.snapshot
        return "ENGINE_MARKET|symbol=${s.symbol}|tf=${s.timeframe}|bid=${s.bid}|ask=${s.ask}|spread=${s.spread}|direction=${s.direction}|strength=${s.strength}|quality=${s.quality}|source=${s.source}"
    }

    fun grid(referencePrice: Double, step: Double, orders: Int, baseLot: Double, multiplier: Double, buy: Boolean, sell: Boolean): String {
        val levels = AmarGridPlanningEngine.build(referencePrice, step, orders, baseLot, multiplier, buy, sell)
        return "ENGINE_GRID|levels=${levels.size}|" + levels.joinToString("|") { "${it.index}:${it.side}:${it.price}:${it.volume}" }
    }

    fun validate(rValues: List<Double>): String {
        if (rValues.size < 2) return "ENGINE_VALIDATION|ERROR=need_2_R_values"
        val r = AmarStrategyValidationEngine.analyze(rValues)
        return "ENGINE_VALIDATION|n=${r.sampleSize}|winRate=${"%.2f".format(r.winRatePct)}|PF=${"%.3f".format(r.profitFactor)}|expectancyR=${"%.4f".format(r.expectancyR)}|maxDD=${"%.3f".format(r.maxDrawdownR)}|SQN=${"%.3f".format(r.sqn)}|verified=${r.verified}"
    }

    fun riskGate(): String {
        val g = AmarTradingPrecisionEngine.defaultResearchGate()
        val r = AmarTradingPrecisionEngine.evaluate(
            AmarTradingPrecisionEngine.Input(
                g.scorePct / 100.0, g.confidencePct / 100.0, g.scorePct / 100.0,
                .25, .40, .20, .35, g.uncertaintyPct / 100.0
            )
        )
        return "ENGINE_RISK|score=${"%.1f".format(r.scorePct)}|confidence=${"%.1f".format(r.confidencePct)}|uncertainty=${"%.1f".format(r.uncertaintyPct)}|gate=${r.gate}|reasons=${r.reasons.joinToString(" || ")}"
    }

    suspend fun tracking(symbol: String? = null): String {
        val runtime = AmarMt5RuntimeRegistry.current()
            ?: return "ENGINE_TRACKING|status=MT5_RUNTIME_NOT_INSTALLED|failClosed=true"
        val positions = runtime.client.positions(symbol)
        require(positions.ok) { "جسر MT5 لم يؤكد بيانات التتبع" }
        return "ENGINE_TRACKING|status=READ_ONLY_RUNTIME|positions=${positions.items.size}|" +
            positions.items.joinToString("|") { "${it.ticket}:${it.symbol}:${it.volume}:${it.priceCurrent}:${it.profit}:${it.magic}" }
    }

    suspend fun candle(symbol: String, timeframe: AmarTimeframe): String {
        val runtime = AmarMt5RuntimeRegistry.current()
            ?: return "ENGINE_CANDLE|status=MT5_RUNTIME_NOT_INSTALLED|failClosed=true"
        val candles = runtime.marketData.refresh(symbol, timeframe)
        val c = candles.lastOrNull() ?: return "ENGINE_CANDLE|ERROR=no_candle_data"
        val direction = when {
            c.close > c.open -> "BULLISH"
            c.close < c.open -> "BEARISH"
            else -> "DOJI"
        }
        val range = c.high - c.low
        val bodyPct = if (range > 0.0) kotlin.math.abs(c.close - c.open) / range * 100.0 else 0.0
        return "ENGINE_CANDLE|symbol=$symbol|tf=${timeframe.shortLabel}|open=${c.open}|high=${c.high}|low=${c.low}|close=${c.close}|direction=$direction|bodyPct=${"%.2f".format(bodyPct)}|source=MT5_RUNTIME"
    }
}
