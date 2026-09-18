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
