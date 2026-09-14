package com.personal.gridbot.amaros.agent

/** Stage 6: provider-neutral market intelligence, indicator provenance and deterministic analysis. */
enum class AmarSourceType {
    OFFICIAL_API,
    MARKET_DATA,
    NEWS,
    SOCIAL,
    COMMUNITY,
    RESEARCH,
    DOCUMENT,
    WEB,
    INDICATOR_ENGINE,
    STATISTICAL_ENGINE,
    MARKET_STRUCTURE,
    KNOWLEDGE,
    UNKNOWN
}

data class AmarSourceLicense(
    val spdxId: String,
    val licenseUrl: String,
    val attributionRequired: Boolean = false
) {
    init {
        require(spdxId.isNotBlank())
        require(licenseUrl.startsWith("https://"))
    }
}

data class AmarSourceProvenance(
    val sourceId: String,
    val name: String,
    val version: String,
    val homepage: String,
    val sourceType: AmarSourceType,
    val license: AmarSourceLicense,
    val repository: String? = null
) {
    init {
        require(sourceId.isNotBlank())
        require(name.isNotBlank())
        require(version.isNotBlank())
        require(homepage.startsWith("https://"))
        repository?.let { require(it.startsWith("https://")) }
    }
}

data class AmarMarketBar(
    val timestampEpochMs: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 0.0
) {
    init {
        require(timestampEpochMs >= 0L)
        require(open.isFinite() && high.isFinite() && low.isFinite() && close.isFinite())
        require(open > 0.0 && high > 0.0 && low > 0.0 && close > 0.0)
        require(high >= maxOf(open, close, low))
        require(low <= minOf(open, close, high))
        require(volume.isFinite() && volume >= 0.0)
    }
}

/** Broad indicator namespace. A provider may implement any subset; the hub has no four-indicator ceiling. */
enum class AmarIndicatorKind {
    SMA, EMA, WMA, DEMA, TEMA, TRIMA, KAMA, MAMA, T3, HMA, ZLEMA, VWMA, RMA, MIDPOINT, MIDPRICE,
    BBANDS, KC, DONCHIAN, SAR, SUPERTREND,
    RSI, MACD, STOCH, STOCHRSI, CCI, ADX, ADXR, AO, ROC, MOM, MFI, CMO, DPO, TRIX, TSI, ULTOSC,
    VORTEX, WILLR, AROON, AROONOSC, PPO, APO, BOP, KDJ,
    ATR, NATR, TRANGE, ADR, MASSI, CVI, RVI,
    OBV, VWAP, PVT, CMF, AD, ADOSC, EFI, PVO, NVI, PVI, RVOL, MARKETFI,
    STDDEV, VAR, BETA, CORREL, LINEARREG, LINEARREG_SLOPE, LINEARREG_ANGLE, LINEARREG_INTERCEPT,
    TSF, PERCENTILE, PERCENTRANK,
    MAX, MIN, SUM, MAXINDEX, MININDEX, MINMAX, MINMAXINDEX,
    HT_DCPERIOD, HT_DCPHASE, HT_PHASOR, HT_SINE, HT_TRENDMODE,
    HA, AVGPRICE, MEDPRICE, TYPPRICE, WCLPRICE,
    FRACTAL, CDL2CROWS, CDL3BLACKCROWS, CDL3INSIDE, CDL3LINESTRIKE, CDL3OUTSIDE,
    CDL3STARSINSOUTH, CDL3WHITESOLDIERS, CDLABANDONEDBABY, CDLADVANCEBLOCK, CDLBELTHOLD,
    CDLBREAKAWAY, CDLCLOSINGMARUBOZU, CDLCONCEALBABYSWALLOW, CDLCOUNTERATTACK
}

data class AmarIndicatorRequest(
    val kind: AmarIndicatorKind,
    val period: Int
) {
    init { require(period in 1..10_000) }
}

data class AmarIndicatorPoint(val timestampEpochMs: Long, val value: Double) {
    init { require(timestampEpochMs >= 0L); require(value.isFinite()) }
}

data class AmarIndicatorResult(
    val kind: AmarIndicatorKind,
    val period: Int,
    val points: List<AmarIndicatorPoint>,
    val source: AmarSourceProvenance
)

data class AmarStatisticalSnapshot(
    val sampleSize: Int,
    val meanReturn: Double,
    val volatility: Double,
    val zScore: Double,
    val positiveReturnFraction: Double
) {
    init {
        require(sampleSize >= 0)
        require(meanReturn.isFinite() && volatility.isFinite() && zScore.isFinite())
        require(positiveReturnFraction.isFinite() && positiveReturnFraction in 0.0..1.0)
    }
}

enum class AmarStructureSignal { HIGHER_HIGH, HIGHER_LOW, LOWER_HIGH, LOWER_LOW, BREAKOUT_UP, BREAKOUT_DOWN, RANGE }

data class AmarMarketStructureSnapshot(
    val signal: AmarStructureSignal,
    val swingHigh: Double,
    val swingLow: Double,
    val lookback: Int
) {
    init {
        require(swingHigh.isFinite() && swingHigh > 0.0)
        require(swingLow.isFinite() && swingLow > 0.0)
        require(lookback >= 2)
    }
}

data class AmarMarketIntelligenceReport(
    val bars: Int,
    val indicators: List<AmarIndicatorResult>,
    val statistics: AmarStatisticalSnapshot,
    val structure: AmarMarketStructureSnapshot,
    val score: Double,
    val confidence: Double,
    val reasons: List<String>
) {
    init {
        require(bars >= 0)
        require(score.isFinite() && score in -1.0..1.0)
        require(confidence.isFinite() && confidence in 0.0..1.0)
        require(reasons.isNotEmpty())
    }
}
