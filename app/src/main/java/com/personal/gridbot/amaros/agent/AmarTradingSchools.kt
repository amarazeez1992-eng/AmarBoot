package com.personal.gridbot.amaros.agent

/**
 * Trading-school taxonomy. These are analytical lenses, not claims that one
 * school is universally correct. The Director can activate several lenses in
 * parallel and require evidence for their conclusions.
 */
enum class AmarTradingSchool {
    TREND_FOLLOWING,
    MOMENTUM,
    MEAN_REVERSION,
    BREAKOUT,
    PRICE_ACTION,
    MARKET_STRUCTURE,
    SUPPORT_RESISTANCE,
    SUPPLY_DEMAND,
    AUCTION_MARKET_THEORY,
    MARKET_PROFILE,
    VOLUME_PROFILE,
    ORDER_FLOW,
    FOOTPRINT,
    VWAP,
    CANDLESTICK,
    HARMONIC,
    ELLIOTT_WAVE,
    WYCKOFF,
    GANN,
    CLASSICAL_CHARTS,
    STATISTICAL_ARBITRAGE,
    PAIRS_TRADING,
    COINTEGRATION,
    FACTOR_INVESTING,
    QUANTITATIVE,
    SYSTEMATIC,
    MACHINE_LEARNING,
    DEEP_LEARNING,
    REINFORCEMENT_LEARNING,
    OPTIONS,
    VOLATILITY,
    MACRO,
    FUNDAMENTAL,
    SENTIMENT,
    EVENT_DRIVEN,
    NEWS_DRIVEN,
    SEASONALITY,
    CARRY,
    MOMENTUM_FACTOR,
    RISK_PARITY,
    PORTFOLIO_OPTIMIZATION,
    PAIRS_AND_BASKETS,
    ON_CHAIN,
    CROSS_ASSET,
    MULTI_TIMEFRAME,
    MARKET_MICROSTRUCTURE
}

data class AmarSchoolLens(
    val school: AmarTradingSchool,
    val questions: List<String>,
    val preferredData: Set<AmarResearchChannel>,
    val preferredCapabilityCategories: Set<AmarCapabilityCategory>
)

object AmarTradingSchoolCatalog {
    val all: List<AmarSchoolLens> = AmarTradingSchool.entries.map { school ->
        AmarSchoolLens(
            school = school,
            questions = listOf(
                "What does this school predict?",
                "What market conditions invalidate it?",
                "What independent evidence supports it?",
                "What are the known failure modes?",
                "Can the hypothesis be expressed as deterministic rules?",
                "Can it be simulated without look-ahead or survivorship bias?"
            ),
            preferredData = when (school) {
                AmarTradingSchool.MACRO, AmarTradingSchool.FUNDAMENTAL -> setOf(AmarResearchChannel.MACROECONOMIC, AmarResearchChannel.OFFICIAL_REGULATORY, AmarResearchChannel.CENTRAL_BANK)
                AmarTradingSchool.OPTIONS, AmarTradingSchool.VOLATILITY -> setOf(AmarResearchChannel.OPTIONS_DERIVATIVES, AmarResearchChannel.EXCHANGE, AmarResearchChannel.MARKET_DATA)
                AmarTradingSchool.ON_CHAIN -> setOf(AmarResearchChannel.ON_CHAIN, AmarResearchChannel.ALTERNATIVE_DATA)
                AmarTradingSchool.MACHINE_LEARNING, AmarTradingSchool.DEEP_LEARNING -> setOf(AmarResearchChannel.ACADEMIC, AmarResearchChannel.PREPRINT, AmarResearchChannel.OPEN_SOURCE_CODE)
                else -> setOf(AmarResearchChannel.MARKET_DATA, AmarResearchChannel.QUANT_RESEARCH, AmarResearchChannel.OPEN_SOURCE_CODE)
            },
            preferredCapabilityCategories = when (school) {
                AmarTradingSchool.MACHINE_LEARNING, AmarTradingSchool.DEEP_LEARNING, AmarTradingSchool.REINFORCEMENT_LEARNING -> setOf(AmarCapabilityCategory.MACHINE_LEARNING, AmarCapabilityCategory.STATISTICS_ENGINE)
                AmarTradingSchool.OPTIONS, AmarTradingSchool.VOLATILITY -> setOf(AmarCapabilityCategory.OPTIONS_ENGINE, AmarCapabilityCategory.STATISTICS_ENGINE)
                else -> setOf(AmarCapabilityCategory.INDICATOR, AmarCapabilityCategory.MARKET_STRUCTURE, AmarCapabilityCategory.STATISTICS_ENGINE)
            }
        )
    }
}
