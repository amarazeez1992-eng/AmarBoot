package com.personal.gridbot.amaros.bots

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * User-selected trading instrument. This is execution context, not strategy logic.
 * The broker symbol is kept explicit because MT5 brokers may use suffixes/prefixes.
 */
data class AmarTradingSymbol(
    val id: String,
    val label: String,
    val brokerSymbol: String
)

object AmarTradingSymbolCatalog {
    val XAUUSD = AmarTradingSymbol("XAUUSD", "الذهب • XAUUSD", "XAUUSD")
    val BTCUSD = AmarTradingSymbol("BTCUSD", "البيتكوين • BTCUSD", "BTCUSD")
    val CUSTOM = AmarTradingSymbol("CUSTOM", "رمز مخصص", "")

    val defaults = listOf(XAUUSD, BTCUSD, CUSTOM)
}

object AmarTradingSymbolContext {
    private const val PREFS = "amar_trading_context"
    private const val KEY_ID = "symbol_id"
    private const val KEY_BROKER_SYMBOL = "broker_symbol"

    var selected: AmarTradingSymbol by mutableStateOf(AmarTradingSymbolCatalog.XAUUSD)
        private set

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_ID, AmarTradingSymbolCatalog.XAUUSD.id).orEmpty()
        val savedBrokerSymbol = prefs.getString(KEY_BROKER_SYMBOL, null)
        val base = AmarTradingSymbolCatalog.defaults.firstOrNull { it.id == id }
            ?: AmarTradingSymbolCatalog.XAUUSD
        selected = if (base.id == AmarTradingSymbolCatalog.CUSTOM.id) {
            base.copy(brokerSymbol = savedBrokerSymbol.orEmpty())
        } else {
            base.copy(brokerSymbol = savedBrokerSymbol?.ifBlank { base.brokerSymbol } ?: base.brokerSymbol)
        }
    }

    fun select(context: Context, symbol: AmarTradingSymbol) {
        selected = symbol
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ID, symbol.id)
            .putString(KEY_BROKER_SYMBOL, symbol.brokerSymbol)
            .apply()
    }
}
