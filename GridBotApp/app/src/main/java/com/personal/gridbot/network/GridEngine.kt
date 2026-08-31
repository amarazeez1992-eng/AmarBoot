package com.personal.gridbot.network

import com.personal.gridbot.data.Bot
import com.personal.gridbot.data.BotDao
import com.personal.gridbot.data.BotLog

/**
 * منطق التنفيذ الفعلي للبوت الشبكي - نسخة Kotlin من نفس فكرة GridTradeBot.mq5
 * لكن تنفّذه عبر MetaApi بدل تشغيله داخل MetaTrader مباشرة.
 */
class GridEngine(private val dao: BotDao) {

    suspend fun runCycle(bot: Bot) {
        try {
            val pipSize = 0.0001 // عدّلها لأزواج الـ JPY (0.01) حسب الزوج
            val spacing = bot.gridSpacingPips * pipSize

            val account = MetaApiClient.service.getAccountInformation(
                MetaApiClient.authToken, MetaApiClient.accountId
            )
            val basePrice = account.balance ?: 0.0 // ملاحظة: استبدلها بسعر السوق الفعلي (quote) وليس الرصيد
            val lot = calcLotSize(bot)

            for (i in 1..bot.gridCount) {
                val buyPrice = basePrice - i * spacing
                val sellPrice = basePrice + i * spacing

                val buyTrade = TradeRequest(
                    actionType = "ORDER_TYPE_BUY_LIMIT",
                    symbol = bot.pair,
                    volume = lot,
                    openPrice = buyPrice,
                    stopLoss = buyPrice * (1 - bot.stopLossPct / 100.0),
                    takeProfit = buyPrice * (1 + bot.takeProfitPct / 100.0)
                )
                val sellTrade = TradeRequest(
                    actionType = "ORDER_TYPE_SELL_LIMIT",
                    symbol = bot.pair,
                    volume = lot,
                    openPrice = sellPrice,
                    stopLoss = sellPrice * (1 + bot.stopLossPct / 100.0),
                    takeProfit = sellPrice * (1 - bot.takeProfitPct / 100.0)
                )

                val buyResult = MetaApiClient.service.executeTrade(
                    MetaApiClient.authToken, MetaApiClient.accountId, buyTrade
                )
                dao.insertLog(BotLog(botId = bot.id, type = "ORDER", message = "Buy L$i -> ${buyResult.stringCode} ${buyResult.orderId}"))

                val sellResult = MetaApiClient.service.executeTrade(
                    MetaApiClient.authToken, MetaApiClient.accountId, sellTrade
                )
                dao.insertLog(BotLog(botId = bot.id, type = "ORDER", message = "Sell L$i -> ${sellResult.stringCode} ${sellResult.orderId}"))
            }
        } catch (e: Exception) {
            dao.insertLog(BotLog(botId = bot.id, type = "ERROR", message = e.message ?: "Unknown error"))
        }
    }

    private fun calcLotSize(bot: Bot): Double {
        val perGridCapital = bot.investment / bot.gridCount.coerceAtLeast(1)
        val lot = (perGridCapital / 1000.0)
        return String.format("%.2f", lot.coerceAtLeast(0.01)).toDouble()
    }
}
