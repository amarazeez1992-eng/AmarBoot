# Stage 11 / Item 3 — Trust, Failure, and Backtest Architecture Roadmap

## Status

PRE-POINT-10 ENGINEERING PREPARATION — NOT A CLOSURE RECORD

This document does not close any constitutional point. It records the engineering map that must be respected while Points 10–24 are closed.

## Objective

Preserve the existing architecture and behavior while preventing the following failure chain:

Bad source -> false evidence quality -> false warning/acceptance -> unsupported reasoning -> wrong decision -> misleading backtest -> unsafe promotion.

The system must fail closed at every boundary where uncertainty, invalid provenance, future data, conflict, or non-reproducible results could otherwise become a trusted input.

## New Point 10 integration contract

`AmarEvidenceQualityUpstreamState` is now the explicit constitutional input contract for Point 10.

It carries one boolean verification state for each of Points 1–9. Point 10 consumes the states; it does not reimplement their algorithms.

The integration rule is strict:
- Point 1 output must populate Point 1 state.
- Point 2 output must populate Point 2 state.
- Point 3 output must populate Point 3 state.
- Point 4 output must populate Point 4 state.
- Point 5 output must populate Point 5 state.
- Point 6 output must populate Point 6 state.
- Point 7 output must populate Point 7 state.
- Point 8 output must populate Point 8 state.
- Point 9 output must populate Point 9 state.

No default-true values, inferred success, or compatibility-derived substitutes are permitted.

## Failure class map

### 1. Bad source

Owner:
- Point 2 Source Quality
- Point 3 Authority Scoring
- Point 5 Source Independence

Detection:
- missing source
- malformed/unusable source
- UNKNOWN authority
- insufficient independent sources
- provenance gaps

Boundary:
- invalid source cannot become trusted evidence.

### 2. False warning / false acceptance

Owner:
- Point 11 Status Classification
- Point 13 Explanation
- Point 15 Claim ↔ Evidence Verification
- Point 19 Fail-Closed Evidence Boundary

Detection:
- deterministic status precedence
- explanation must reference actual evidence state
- unsupported claims cannot pass
- uncertainty must remain explicit

Boundary:
- no warning may be emitted from a condition not supported by the canonical evidence state.

### 3. Conflict / contradictory evidence

Owner:
- Point 14 Conflict Awareness
- Point 19 Fail-Closed Boundary

Detection:
- preserve both sides
- identify the conflicting evidence
- do not silently resolve the conflict inside Evidence Engine

Boundary:
- conflict blocks downstream trust unless an explicit later policy permits continuation.

### 4. Wrong decision

Owner:
- Point 15 verification output
- Point 16 confidence calibration input
- Decision Intelligence / Risk Guard after Item 3

Evidence Engine does not decide a trade.

Boundary:
- Evidence Engine supplies verified facts and states only.
- Decision layers must not treat evidence quality as profitability probability.

### 5. Misleading backtest

Owner:
- Quant Research / Backtest layer
- existing AmarQuantResearchPlatform
- future strategy-validation/promotion boundary

Required controls:
- chronological ordering
- decision cutoff enforcement
- future-data rejection
- dataset fingerprinting
- train / validation / OOS separation
- walk-forward evaluation
- realistic spread/slippage/fees/latency assumptions
- stress testing
- sufficient sample evidence
- reproducible experiment identity
- explicit hypothetical/simulated labeling
- no promotion from a single in-sample result

TradingView's current documentation confirms that strategy scripts simulate trades through a broker emulator and that strategy reports are hypothetical; it also documents that non-standard charts can distort simulated execution and that OOS testing is used to reduce overfitting risk. The architecture therefore treats TradingView-style results as experiment evidence with explicit assumptions, not guaranteed future performance.

## Trading recommendation capability

The requested future behavior is accepted as an architectural product requirement:

When the user asks for a trade search, the agent should be able to research the relevant market rather than answer from a single indicator. The future pipeline should support:
- current market/data snapshot and timestamp
- candle/price-action structure
- multi-timeframe analysis
- indicators and indicator limitations
- market structure, support/resistance, supply/demand, volume/order-flow where data exists
- trading-school lenses including trend, momentum, mean reversion, breakout, price action, market structure, Wyckoff, Elliott, Gann, quantitative/statistical, macro/fundamental, sentiment, volatility, and other documented lenses represented by the existing taxonomy
- source/provenance verification
- candidate trade construction
- historical simulation/backtest
- forward-test or paper validation where supported
- spread/slippage/fees/latency assumptions
- out-of-sample and walk-forward evaluation
- stress/regime analysis
- risk and invalidation conditions
- explicit conflict/uncertainty disclosure
- final recommendation only after the evidence/risk gates allow it

TradingView's documentation shows that its ecosystem includes indicators, strategies, libraries, community scripts, and Pine Script v6; strategies can simulate market/limit/stop/stop-limit orders and produce strategy reports. This supports treating TradingView as one research source/tooling ecosystem, not as the sole authority.

## Important existing architectural risk

`AmarEvidenceQualityEngine` is retained for compatibility with earlier consumers. It is not allowed to become a second constitutional Point 10 methodology.

Its numeric fields remain advisory/upstream facts. `AmarEvidenceQualityScoreEngine` is the canonical Point 10 certification boundary.

Future changes must not silently reintroduce:
- heuristic Point 10 weights
- aggregate averaging as certification
- model-dependent scoring
- future evidence acceptance
- duplicate logic for already-closed points
- a second decision engine
- execution authority inside intelligence/evidence layers

## Backtest trust boundary

Backtest output is evidence about a defined experiment, not proof of future profitability.

The result record should remain traceable to:
- strategy/version
- dataset fingerprint
- data interval
- execution assumptions
- cost assumptions
- validation protocol
- OOS windows
- stress scenarios
- sample size
- leakage result
- reproducibility identity

Hypothetical/simulated results must remain explicitly identified and must not be represented as actual trading performance.

## Trading-recommendation capability baseline

The requested future capability to research and produce a trade recommendation is recorded as a separate architectural baseline in `docs/architecture/AMAR_TRADING_RECOMMENDATION_ARCHITECTURE_BASELINE.md`.

The baseline defines the future path for fast and expert research profiles, broad market-data and analytical-source routing, indicator and trading-school research, order-flow where data exists, deterministic candidate validation, backtest leakage/hindsight/execution-cost controls, explicit conflicts and invalidation, risk/uncertainty reporting, and a first-class `NO_TRADE` result.

It deliberately does **not** make a 100% profit guarantee. The constitutional target is 100% coverage and verification of the defined workflow, with no fabricated evidence or hidden assumptions. Simulated/backtested performance remains experimental evidence rather than proof of future profitability.

This baseline must remain downstream of the canonical Evidence Engine boundaries and must not duplicate Points 10–24 responsibilities or create a parallel decision/execution path.

## Point 10 → 24 execution map

- Point 10: certify evidence quality from upstream verified states.
- Point 11: deterministic status classification.
- Point 12: deterministic evidence ranking.
- Point 13: auditable evidence explanation.
- Point 14: conflict detection without silent resolution.
- Point 15: claim/evidence verification.
- Point 16: structured confidence-calibration input.
- Point 17: invalid/future evidence protection.
- Point 18: deterministic evidence policy.
- Point 19: fail-closed evidence boundary.
- Point 20: canonical evidence authority.
- Point 21: security/execution boundary.
- Point 22: focused regression over Points 1–21.
- Point 23: architecture integrity gate.
- Point 24: final constitutional verification gate.

## Closure rule

No Point 10–24 point is closed because the code looks correct.

Closure requires:
inspect → discover → root cause → architect → implement → unit test → integration test → regression → build → behavior verification → re-inspection → security/boundary check → CI evidence → documentation → constitutional decision.

## External engineering references

NIST's AI Risk Management Framework treats validity, reliability, robustness, provenance, testing/evaluation, and ongoing monitoring as trustworthiness concerns rather than as a single universal percentage formula.

Backtest results are inherently limited when hypothetical or simulated; CFTC and SEC materials describe hindsight, liquidity/execution assumptions, and differences between simulated and actual results as material limitations.


## Adopted trading-agent completeness controls for Points 10–24

The following controls are adopted as architecture requirements now, while their concrete implementation belongs to the responsible later point/stage and must not be backfilled into Point 10:

### A. Market-data evidence contract
A trade-search request must distinguish, at minimum:
- instrument/symbol
- market/session and timezone
- timeframe
- candle/bar source and timestamp
- bid/ask or executable-price context when available
- spread/fee/commission assumptions
- order-flow/order-book data when actually available
- indicator inputs and calculation version
- data cutoff used for the decision

Missing or unverifiable market-data provenance must not be silently replaced by model inference.

### B. Strategy / indicator provenance
Every indicator, strategy, school, script, library, or external methodology used by a trade-search run must remain attributable to its source and version where available. The agent may compare multiple methodologies, but must not imply that community scripts, educational material, or proprietary claims are equivalent in authority.

### C. Backtest promotion gate
A backtest may support a trade hypothesis only when its experiment identity, dataset fingerprint, cutoff, execution/cost assumptions, leakage result, validation/OOS protocol, and stress results are available. A single favorable in-sample result is insufficient for promotion.

### D. Recommendation trace
A final trade hypothesis must be traceable through:
market data -> evidence -> analysis/method -> conflict state -> backtest/forward-test evidence (if used) -> risk constraints -> recommendation.
If a link in that chain is missing or unverifiable, the downstream recommendation must be marked accordingly or blocked by the owning fail-closed policy.

### E. Research-mode separation
The future Fast and Expert research modes may use different breadth/depth budgets, but they must consume the same canonical evidence, verification, provenance, and fail-closed contracts. Fast mode is not permitted to bypass safety/verification gates.

### F. Execution separation
Trade recommendation and trade execution remain different authorities. The research/intelligence/evidence layers may produce a trade hypothesis and its conditions, but cannot silently acquire broker execution authority.

### G. Market-regime and non-stationarity awareness
Backtest and strategy-validation layers must record the market regime/context represented by their data and avoid treating historical stationarity as an assumption of future behavior. Regime coverage is an evaluation dimension, not an invented confidence weight.

### H. Negative-result preservation
Failed searches, rejected evidence, conflicting sources, failed backtests, and invalidated hypotheses must remain auditable inputs. The system must not discard negative evidence merely because it conflicts with a desired trade direction.

These controls increase domain completeness without introducing a universal profitability score or claiming a 100% win rate. They are compatible with NIST's emphasis on validity, reliability, robustness, explicit test methodology, uncertainty, and ongoing evaluation.
