# Amar AI — Trading Recommendation Architecture Baseline

## Status

ARCHITECTURAL BASELINE — NOT A CONSTITUTIONAL CLOSURE RECORD

This document establishes the target architecture for future stages that turn the verified Evidence Engine into a trading-research and recommendation capability. It does not reopen closed Evidence Engine points and does not authorize live trade execution.

## Objective

When the user asks for a trade search, the agent must be able to assemble and verify the information required for a defined market question rather than rely on a single indicator, source, school, or backtest.

The target is **100% capability/verification coverage of the defined workflow**, not a claim of 100% profitable trades. No engineering design can truthfully guarantee future market profit.

## Canonical request flow

User request
→ request interpretation and missing-parameter detection
→ market/instrument/timeframe scope
→ current and historical market-data intake
→ source/evidence verification
→ multi-school analytical research
→ indicator/statistical analysis
→ market-structure and order-flow analysis where data exists
→ candidate setup generation
→ deterministic backtest/validation
→ leakage/hindsight/execution-cost checks
→ conflict detection
→ risk and invalidation analysis
→ recommendation synthesis
→ explicit uncertainty and evidence trace
→ user-facing trade plan or NO_TRADE

## Fast mode vs Expert mode

The architecture must support two later execution profiles without creating two methodologies:

### Fast mode

- bounded source set
- bounded analytical scope
- deterministic time/data budget
- rapid candidate screening
- no bypass of evidence, conflict, freshness, leakage, or risk gates

### Expert mode

- broader source discovery
- multiple independent analytical schools
- deeper indicator/statistical analysis
- broader historical validation
- stronger cross-checking and conflict analysis
- expanded stress and scenario testing

Both modes must use the same canonical evidence, validation, risk, and security boundaries. Fast mode may reduce breadth, but it may not weaken safety or falsify confidence.

## Research coverage

The research orchestrator should be capable of routing to relevant sources and tools for:

- OHLCV/candlestick data
- bid/ask and spread information where available
- volume and market microstructure data where available
- order-flow/footprint data where available
- economic and macro data
- official/regulatory data
- news and event data
- TradingView-compatible indicator definitions or documented equivalents
- academic and quantitative research
- open-source implementations, with provenance and license checks
- documented trading methodologies/schools

The system must distinguish **available data** from **assumed or unavailable data**. It must never fabricate order flow, volume, spread, execution, or indicator values.

## Analytical-school routing

The existing trading-school taxonomy is a set of analytical lenses, not a universal truth hierarchy.

A request may activate relevant lenses including:

- price action/candlesticks
- market structure/support/resistance
- supply/demand
- trend/momentum/breakout
- mean reversion
- VWAP/volume profile/market profile
- order flow/market microstructure
- Wyckoff/Elliott/harmonic/classical chart analysis
- quantitative/statistical methods
- machine learning where justified
- macro/fundamental/news/event analysis
- cross-asset and multi-timeframe analysis

Each activated lens must expose its assumptions, required data, invalidation conditions, evidence, and known failure modes. No school receives an undocumented priority or invented weight.

## Indicator and library policy

The agent may research and compare indicators and documented implementations, but an indicator is evidence/input, not a guarantee.

For each indicator or library used in a recommendation, the future implementation should preserve:

- exact definition/version
- parameters
- source/provenance
- timeframe
- data dependencies
- warm-up requirements
- repaint/look-ahead behavior
- calculation differences across platforms
- licensing/usage constraints where applicable

## Backtest and validation contract

A recommendation supported by a backtest must be traceable to:

- strategy/version identity
- exact dataset and fingerprint
- observation window
- decision cutoff
- chronological ordering
- train/validation/OOS boundaries
- walk-forward protocol where applicable
- spread/commission/slippage/latency assumptions
- liquidity/execution assumptions
- stress scenarios
- sample size and trade distribution
- leakage/look-ahead checks
- reproducibility identity

A single in-sample result is never sufficient to establish future profitability.

Hypothetical/simulated results must remain explicitly labeled as such. CFTC material notes that simulated results can differ from actual trading because of execution/liquidity assumptions and hindsight limitations.

## Recommendation contract

A future recommendation object should contain, at minimum:

- instrument
- timeframe(s)
- market timestamp / decision cutoff
- direction or NO_TRADE
- entry condition/range
- invalidation condition
- stop-loss basis
- target basis
- risk constraints
- supporting evidence references
- conflicting evidence references
- analytical lenses used
- backtest/validation identity
- data limitations
- execution assumptions
- uncertainty state
- reason for rejection when NO_TRADE

The recommendation layer must never silently convert evidence quality into a probability of profit.

## NO_TRADE is a first-class result

The agent must be able to return NO_TRADE when:

- evidence is insufficient or invalid
- data required by the requested method is unavailable
- evidence is future-dated or temporally invalid
- material evidence conflicts remain unresolved
- backtest validation is invalid or non-reproducible
- execution assumptions are materially unknown
- risk/invalidation cannot be defined
- the requested claim cannot be supported

NO_TRADE is a valid analytical outcome, not an error state.

## Security and execution boundary

Research and recommendation layers may analyze, calculate, backtest, explain, and prepare a trade plan.

They must not:

- execute trades
- alter broker accounts
- bypass permissions
- execute untrusted code
- modify sensitive files outside authorized tooling
- treat research output as execution authorization

Any future live-execution capability must be a separate, explicitly authorized, audited boundary with its own controls.

## Relationship to Stage 11 / Item 3

Stage 11 / Item 3 remains the evidence trust foundation.

Points 10–24 must strengthen:

- deterministic certification
- status and ranking
- explanation
- conflict awareness
- claim/evidence verification
- confidence-calibration inputs
- future/invalid evidence protection
- fail-closed behavior
- canonical authority
- security boundaries
- regression and architecture integrity

They must not absorb the trading recommendation engine or duplicate the responsibilities of later decision, risk, quant-research, or execution stages.

## Constitutional design principle

The agent should become broad in capability while remaining strict at trust boundaries:

**search broadly → verify evidence → analyze through appropriate lenses → validate reproducibly → expose conflicts → define invalidation/risk → recommend only when the evidence contract permits → otherwise NO_TRADE.**

This baseline is intentionally compatible with later stages and does not declare any future capability complete.
