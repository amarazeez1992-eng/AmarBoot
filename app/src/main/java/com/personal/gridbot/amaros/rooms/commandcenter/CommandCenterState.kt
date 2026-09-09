package com.personal.gridbot.amaros.rooms.commandcenter

/**
 * بيانات العرض التجريبي لمركز القيادة.
 * لا تحتوي على أي منطق تداول أو اتصال MT5.
 *
 * التصميم متعمد أن يكون Data-Driven حتى يمكن تغيير القيم والأقسام لاحقاً
 * دون إعادة بناء محرك التداول.
 */
data class CommandCenterState(
    val account: AccountSnapshot = AccountSnapshot(),
    val market: MarketSnapshot = MarketSnapshot(),
    val trading: TradingSnapshot = TradingSnapshot(),
    val risk: RiskSnapshot = RiskSnapshot(),
    val alerts: List<AlertItem> = emptyList(),
    val sections: CommandCenterSections = CommandCenterSections()
)

data class AccountSnapshot(
    val balance: Double = 1000.00,
    val equity: Double = 1012.84,
    val margin: Double = 120.00,
    val freeMargin: Double = 892.84,
    val floatingProfit: Double = 12.84,
    val dailyProfit: Double = 18.40,
    val totalProfit: Double = 127.40
)

data class MarketSnapshot(
    val symbol: String = "XAUUSD",
    val price: Double = 0.0,
    val direction: String = "شراء",
    val strength: Int = 82,
    val session: String = "الجلسة الحالية",
    val status: String = "تجريبي"
)

data class TradingSnapshot(
    val botName: String = "AMAR GRID",
    val botStatus: String = "جاهز",
    val positions: Int = 6,
    val pendingOrders: Int = 18,
    val winningTrades: Int = 4,
    val losingTrades: Int = 2
)

data class RiskSnapshot(
    val riskPercent: Double = 5.0,
    val drawdownPercent: Double = 1.28,
    val protection: String = "مفعّلة",
    val emergencyStop: Boolean = false
)

data class AlertItem(
    val title: String,
    val detail: String,
    val level: AlertLevel = AlertLevel.INFO
)

enum class AlertLevel { INFO, WARNING, CRITICAL }

data class CommandCenterSections(
    val showAccount: Boolean = true,
    val showMarket: Boolean = true,
    val showTrading: Boolean = true,
    val showRisk: Boolean = true,
    val showAlerts: Boolean = true
)

val PreviewCommandCenterState = CommandCenterState(
    alerts = listOf(
        AlertItem("النظام في الوضع التجريبي", "لا يوجد اتصال تداول حقيقي", AlertLevel.INFO),
        AlertItem("الحماية مفعّلة", "مراقبة المخاطر تعمل على بيانات المحاكاة", AlertLevel.INFO)
    )
)
