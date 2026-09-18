# Amar AI — Trading Research & Recommendation Constitutional Contract

## Status

APPROVED ARCHITECTURAL BASELINE — IMPLEMENTATION DEFERRED TO THE RESPONSIBLE LATER STAGES

This document extends Stage 11 / Item 3 planning without closing any Point 10–24 item and without changing the closed Point 1–9 contracts.

## Objective

Amar AI is intended to support trading research and produce auditable trade-analysis recommendations across market data, candles, orders/order-book information where legitimately available, indicators, documented analytical schools, and reproducible backtests.

The engineering target is comprehensive trading coverage and maximum verifiable reliability. The system MUST NOT claim that any recommendation or strategy is 100% profitable or guaranteed to win. A recommendation is only as strong as the verified data, evidence, execution assumptions, risk constraints, and validation available at decision time.

## Canonical research flow

User request:
"ابحث لي عن صفقة"

must eventually resolve through one auditable pipeline:

1. Request interpretation and instrument/timeframe scope.
2. Market-data availability and timestamp/provenance verification.
3. Candle/price-structure analysis.
4. Volume/order-flow/order-book analysis when the selected data source actually provides it.
5. Indicator computation from canonical market data.
6. Multi-timeframe/context analysis.
7. Documented analytical-school/strategy research.
8. Candidate setup generation.
9. Evidence and source verification.
10. Conflict detection.
11. Historical/backtest validation using only information available at each historical decision time.
12. Out-of-sample / walk-forward validation where the strategy protocol requires it.
13. Execution-model and cost validation.
14. Risk and invalidation analysis.
15. Recommendation assembly.
16. Explicit uncertainty, missing-data, conflict, and assumption disclosure.
17. Final decision-boundary check before a recommendation is emitted.

No single indicator, school, source, model, or backtest may silently become the universal authority.

## Expert mode vs Fast mode

The future user-selectable modes are orchestration policies, not separate methodologies.

### Fast mode

Optimized for bounded latency using already-authorized, verified data sources and a defined subset of analysis.

It MUST NOT bypass:
- provenance checks;
- future-data protection;
- evidence status;
- conflict detection;
- risk boundaries;
- recommendation traceability.

### Expert mode

Runs the broader research protocol, including deeper source comparison, additional analytical methods, broader indicator/strategy evaluation, and stronger validation where data and time permit.

Expert mode MUST NOT gain permission to bypass constitutional safety or evidence boundaries.

The exact mode contract, latency budget, and eligible research components must be defined later from repository evidence rather than invented now.

## Market-data coverage boundary

The architecture must be able to represent, when supported by the connected provider:

- OHLC/price candles;
- bid/ask and spread;
- tick/quote data;
- volume or provider-specific volume semantics;
- order book / depth;
- trades/order-flow data;
- sessions and market calendars;
- symbol metadata;
- corporate/contract adjustments where relevant;
- economic/news/event data;
- historical datasets;
- live/realtime streams.

Provider capability MUST be explicit. Missing order-book or volume data must never be silently fabricated or inferred as if it were directly observed.

## Indicator and analytical-school registry

Indicators and analytical methods must be represented through a registry/adapter contract with:

- canonical name and version;
- implementation/source identity;
- input requirements;
- timeframe requirements;
- parameterization;
- deterministic output;
- warm-up requirements;
- known data dependencies;
- provenance;
- test coverage;
- backtest compatibility;
- limitations.

The registry may contain technical indicators and documented analytical methodologies, but membership in the registry does not imply that a method is profitable.

TradingView scripts, public libraries, websites, books, research papers, broker documentation, and other external material MUST be handled according to licensing, API, terms-of-use, provenance, and source-authority constraints. The agent must not scrape or reproduce protected content merely because it is useful.

## Backtest integrity contract

Every backtest used in a recommendation must carry an experiment identity and remain traceable to:

- strategy/method version;
- dataset fingerprint;
- data source and provenance;
- historical interval;
- decision cutoff;
- timeframe(s);
- train/validation/OOS protocol;
- walk-forward protocol where applicable;
- spread/slippage/commission/fee assumptions;
- latency/execution assumptions;
- position sizing/risk assumptions;
- sample size;
- leakage checks;
- stress scenarios;
- reproducibility identity;
- result status.

The engine MUST reject or downgrade the experiment as non-decisive when required controls are absent.

Backtests are experiment evidence, not proof of future profitability. Hypothetical/simulated performance must remain explicitly labeled. CFTC materials specifically identify hindsight, liquidity, execution, and other differences between simulated and actual trading as material limitations.

## Recommendation contract

A future trade recommendation must be structured, not just a buy/sell sentence. It should be able to expose:

- instrument;
- direction;
- entry condition/zone;
- invalidation/stop condition;
- target(s);
- timeframe;
- setup rationale;
- supporting evidence references;
- conflicting evidence;
- market-data timestamp;
- data-source identity;
- backtest/validation identity when used;
- execution/cost assumptions;
- risk constraints;
- missing information;
- recommendation status;
- uncertainty/confidence output from the authoritative downstream confidence architecture.

The recommendation layer MUST NOT convert evidence quality into a probability of profit by itself.

## Fail-closed rules

No recommendation may be emitted as actionable when required market data is invalid, future-dated, provenance-incomplete, materially conflicted, or otherwise fails the applicable evidence contract.

A research result may be returned as informational when the architecture explicitly supports that state, but the system must clearly distinguish informational research from an actionable recommendation.

No trade execution authority is created by this contract.

## Required failure taxonomy

Future implementation and regression suites must cover at minimum:

- bad source;
- stale source;
- future data;
- malformed data;
- missing data;
- duplicated/derived duplicate evidence;
- conflicting sources;
- indicator calculation failure;
- unsupported provider capability;
- look-ahead leakage;
- selection/cherry-picking bias;
- unrealistic spread/slippage/fees;
- execution mismatch;
- insufficient sample;
- non-reproducible backtest;
- strategy overfitting;
- recommendation with missing invalidation/risk information;
- disagreement between analytical methods;
- provider outage or partial data.

Each failure must have one architectural owner and a deterministic boundary response.

## Ownership after Item 3

Evidence Engine owns evidence validity and evidence-state contracts.

Research/Quant layers own strategy research, backtesting, experiment identity, and validation.

Decision Intelligence owns synthesis of verified evidence into a decision candidate.

Risk Guard owns trading-risk constraints and authorization boundaries.

Execution infrastructure, if later enabled, owns broker/order execution and must remain outside evidence/research authority.

No layer may duplicate another layer's canonical responsibility.

## Constitutional compatibility

This contract does not alter Point 10's binary certification model and does not authorize invented weights or percentages.

NIST's AI Risk Management Framework treats validity, reliability, robustness, testing/evaluation, and lifecycle monitoring as trustworthiness concerns. It does not establish a universal trading-success percentage.
