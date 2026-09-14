package com.personal.gridbot.amaros.agent

/**
 * Indicator engine hub: there is intentionally no fixed four-indicator ceiling.
 * Any independently licensed adapter can be registered and queried by capability.
 */
class AmarIndicatorEngineHub(
    private val sourceRegistry: AmarSourceRegistry = AmarSourceRegistry()
) {
    private val adapters = linkedMapOf<String, AmarIndicatorAdapter>()

    fun register(adapter: AmarIndicatorAdapter) {
        sourceRegistry.register(adapter.provenance)
        require(adapters[adapter.provenance.sourceId] == null) {
            "Indicator adapter already registered: ${adapter.provenance.sourceId}"
        }
        adapters[adapter.provenance.sourceId] = adapter
    }

    fun registerAll(newAdapters: Iterable<AmarIndicatorAdapter>) = newAdapters.forEach(::register)

    fun allAdapters(): List<AmarIndicatorAdapter> = adapters.values.toList()

    fun sourceIds(): List<String> = adapters.keys.toList()

    fun calculate(
        bars: List<AmarMarketBar>,
        request: AmarIndicatorRequest,
        sourceId: String? = null
    ): AmarIndicatorResult {
        val candidates = if (sourceId == null) adapters.values else listOfNotNull(adapters[sourceId])
        require(candidates.isNotEmpty()) { "No indicator engine is registered" }
        val failures = mutableListOf<Throwable>()
        for (adapter in candidates) {
            try {
                return adapter.calculate(bars, request)
            } catch (failure: Throwable) {
                failures += failure
            }
        }
        throw IllegalStateException("No registered indicator engine can calculate ${request.kind}", failures.firstOrNull())
    }
}

/** Capability declaration used to discover broad indicator families without coupling to one vendor. */
data class AmarIndicatorCapability(
    val family: String,
    val name: String,
    val aliases: Set<String> = emptySet(),
    val requiresVolume: Boolean = false,
    val requiresOpenInterest: Boolean = false
)

object AmarIndicatorUniverse {
    /** Catalog of common technical-analysis families; implementations may come from any admitted adapter. */
    val capabilities: List<AmarIndicatorCapability> = listOf(
        "Overlap" to "SMA", "Overlap" to "EMA", "Overlap" to "WMA", "Overlap" to "DEMA",
        "Overlap" to "TEMA", "Overlap" to "TRIMA", "Overlap" to "KAMA", "Overlap" to "MAMA",
        "Overlap" to "T3", "Overlap" to "HMA", "Overlap" to "ZLEMA", "Overlap" to "VWMA",
        "Overlap" to "RMA", "Overlap" to "MIDPOINT", "Overlap" to "MIDPRICE", "Overlap" to "BBANDS",
        "Overlap" to "KC", "Overlap" to "DONCHIAN", "Overlap" to "SAR", "Overlap" to "SUPERTREND",
        "Momentum" to "RSI", "Momentum" to "MACD", "Momentum" to "STOCH", "Momentum" to "STOCHRSI",
        "Momentum" to "CCI", "Momentum" to "ADX", "Momentum" to "ADXR", "Momentum" to "AO",
        "Momentum" to "ROC", "Momentum" to "MOM", "Momentum" to "MFI", "Momentum" to "CMO",
        "Momentum" to "DPO", "Momentum" to "TRIX", "Momentum" to "TSI", "Momentum" to "ULTOSC",
        "Momentum" to "VORTEX", "Momentum" to "WILLR", "Momentum" to "AROON", "Momentum" to "AROONOSC",
        "Momentum" to "PPO", "Momentum" to "APO", "Momentum" to "BOP", "Momentum" to "KDJ",
        "Volatility" to "ATR", "Volatility" to "NATR", "Volatility" to "TRANGE", "Volatility" to "ADR",
        "Volatility" to "MASSI", "Volatility" to "CVI", "Volatility" to "RVI",
        "Volume" to "OBV", "Volume" to "VWAP", "Volume" to "PVT", "Volume" to "CMF",
        "Volume" to "AD", "Volume" to "ADOSC", "Volume" to "EFI", "Volume" to "PVO",
        "Volume" to "NVI", "Volume" to "PVI", "Volume" to "RVOL", "Volume" to "MARKETFI",
        "Statistics" to "STDDEV", "Statistics" to "VAR", "Statistics" to "BETA", "Statistics" to "CORREL",
        "Statistics" to "LINEARREG", "Statistics" to "LINEARREG_SLOPE", "Statistics" to "LINEARREG_ANGLE",
        "Statistics" to "LINEARREG_INTERCEPT", "Statistics" to "TSF", "Statistics" to "PERCENTILE",
        "Statistics" to "PERCENTRANK", "Math" to "MAX", "Math" to "MIN", "Math" to "SUM",
        "Math" to "MAXINDEX", "Math" to "MININDEX", "Math" to "MINMAX", "Math" to "MINMAXINDEX",
        "Cycle" to "HT_DCPERIOD", "Cycle" to "HT_DCPHASE", "Cycle" to "HT_PHASOR",
        "Cycle" to "HT_SINE", "Cycle" to "HT_TRENDMODE", "Price" to "HA", "Price" to "AVGPRICE",
        "Price" to "MEDPRICE", "Price" to "TYPPRICE", "Price" to "WCLPRICE", "Pattern" to "FRACTAL",
        "Pattern" to "CDL2CROWS", "Pattern" to "CDL3BLACKCROWS", "Pattern" to "CDL3INSIDE",
        "Pattern" to "CDL3LINESTRIKE", "Pattern" to "CDL3OUTSIDE", "Pattern" to "CDL3STARSINSOUTH",
        "Pattern" to "CDL3WHITESOLDIERS", "Pattern" to "CDLABANDONEDBABY", "Pattern" to "CDLADVANCEBLOCK",
        "Pattern" to "CDLBELTHOLD", "Pattern" to "CDLBREAKAWAY", "Pattern" to "CDLCLOSINGMARUBOZU",
        "Pattern" to "CDLCONCEALBABYSWALLOW", "Pattern" to "CDLCOUNTERATTACK"
    ).map { (family, name) -> AmarIndicatorCapability(family, name) }
}
