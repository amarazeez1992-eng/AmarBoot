package com.personal.gridbot.ui.gridcontrol

data class GridControlState(
    val botName: String = "AMAR GRID",
    val symbol: String = "XAUUSD",
    val selectedTimeframe: String = "M5",

    val isTrading: Boolean = false,
    val buyEnabled: Boolean = true,
    val sellEnabled: Boolean = true,
    val gridEnabled: Boolean = true,

    val positions: Int = 0,
    val winningTrades: Int = 0,
    val losingTrades: Int = 0,
    val pendingOrders: Int = 0,

    val balance: Double = 1000.0,
    val equity: Double = 1000.0,
    val margin: Double = 0.0,
    val freeMargin: Double = 1000.0,

    val floatingProfit: Double = 0.0,
    val dailyProfit: Double = 0.0,
    val totalProfit: Double = 0.0,

    val marketDirection: String = "BUY",
    val marketStrength: Int = 82,

    val lotStart: Double = 0.02,
    val gridStep: Int = 40,
    val maxOrders: Int = 30,
    val martingale: Double = 2.0,

    val basketTp: Double = 100.0,
    val basketSl: Double = -100.0,

    val individualTp: Double = 1.5,
    val individualSl: Double = -1.5,

    val enhancementEnabled: Boolean = false,
    val trailingEnabled: Boolean = false,
    val autoRebuildEnabled: Boolean = true,
    val newEntriesEnabled: Boolean = true,
    val manageManualPositions: Boolean = false,

    val accountAdded: Boolean = false,
    val accountBroker: String = "JustMarkets",
    val accountName: String = "AMAR DEMO",
    val accountNumber: String = "12345678",

    val isDemoMode: Boolean = true
)
