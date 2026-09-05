package com.personal.gridbot.ui.gridcontrol

val PreviewGridControlState = GridControlState(
    botName = "AMAR GRID",
    symbol = "XAUUSD",
    selectedTimeframe = "M5",

    isTrading = true,
    buyEnabled = true,
    sellEnabled = true,
    gridEnabled = true,

    positions = 6,
    winningTrades = 4,
    losingTrades = 2,
    pendingOrders = 18,

    balance = 1000.0,
    equity = 1012.84,
    margin = 120.0,
    freeMargin = 892.84,

    floatingProfit = 12.84,
    dailyProfit = 18.40,
    totalProfit = 127.40,

    marketDirection = "BUY",
    marketStrength = 82,

    lotStart = 0.02,
    gridStep = 40,
    maxOrders = 30,
    martingale = 2.0,

    basketTp = 100.0,
    basketSl = -100.0,

    individualTp = 1.5,
    individualSl = -1.5,

    enhancementEnabled = false,
    trailingEnabled = true,
    autoRebuildEnabled = true,
    newEntriesEnabled = true,
    manageManualPositions = true,

    accountAdded = true,
    accountBroker = "JustMarkets",
    accountName = "AMAR DEMO",
    accountNumber = "12345678",

    isDemoMode = true
)
