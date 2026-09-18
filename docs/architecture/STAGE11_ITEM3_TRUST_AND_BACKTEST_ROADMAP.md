# Stage 11 / Item 3 — Trust, Failure, and Backtest Architecture Roadmap

## Status

PRE-POINT-10 ENGINEERING PREPARATION — NOT A CLOSURE RECORD

This document does not close any constitutional point. It records the engineering map that must be respected while Points 10–24 are closed.

## Objective

Preserve the existing architecture and behavior while preventing the following failure chain:

Bad source -> false evidence quality -> false warning/acceptance -> unsupported reasoning -> wrong decision -> misleading backtest -> unsafe promotion.

The system must fail closed at every boundary where uncertainty, invalid provenance, future data, conflict, or non-reproducible results could otherwise become a trusted input.

## Canonical flow

1. Source / data intake
2. Source quality and authority
3. Freshness / temporal validity
4. Source independence
5. Duplicate / semantic duplicate detection
6. Fingerprint integrity
7. Tampering / provenance integrity
8. Evidence uniqueness
9. Evidence quality certification
10. Evidence status
11. Evidence ranking
12. Evidence explanation
13. Conflict awareness
14. Claim ↔ evidence verification
15. Confidence calibration input
16. Invalid / future evidence protection
17. Deterministic policy
18. Fail-closed boundary
19. Canonical evidence authority
20. Security / execution boundary
21. Regression / architecture / constitutional gates

The existing Point 1–9 implementations remain the owners of their individual evidence properties. Point 10 must consume their verified states rather than reimplementing their methodology.

## Corrected Point 10 boundary

Point 10 has two different concepts that must never be conflated:

- Numeric evidence attributes: authority score and freshness score.
- Verification states: whether the upstream contract has actually verified those attributes.

Point 10 certifies the verification states. It does not reinterpret a lower-but-valid upstream score as an invented partial percentage.

Therefore:

- VERIFIED requires all required upstream verification states plus independence and uniqueness to pass.
- NOT_VERIFIED is the only alternative.
- Point 10 output remains binary.
- No averaging, weighting, normalization, or heuristic percentage is introduced.

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
- future Item/Stage responsible for deterministic strategy validation

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

The repository already contains leakage checks, dataset fingerprints, walk-forward windows, stress testing, and mutation gates. Future work must connect these controls into one auditable promotion boundary instead of creating parallel backtest engines.

## Important existing architectural risk

AmarEvidenceQualityEngine is retained for compatibility with earlier consumers. It is not allowed to become a second constitutional Point 10 methodology.

Its numeric fields remain advisory/upstream facts. AmarEvidenceQualityScoreEngine is the canonical Point 10 certification boundary.

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

Closure requires the project's constitutional protocol:
inspect → discover → root cause → architect → implement → unit test → integration test → regression → build → behavior verification → re-inspection → security/boundary check → CI evidence → documentation → constitutional decision.

## External engineering reference

NIST's AI Risk Management Framework treats validity, reliability, robustness, provenance, testing/evaluation, and ongoing monitoring as trustworthiness concerns rather than as a single universal percentage formula. This supports the project's decision to avoid invented universal weights while strengthening verification and auditability.

Backtest results are also inherently limited when hypothetical or simulated; regulatory material from the CFTC and SEC describes hindsight, liquidity/execution assumptions, and differences between simulated and actual results as material limitations. The project therefore treats backtests as experiment evidence with explicit assumptions, not as guarantees.

## Mandatory Point 10 integration hardening

Before Point 10 can be constitutionally closed, the canonical path must be re-inspected for contract wiring, not only local correctness. The Point 10 gate must consume the established outputs of Points 1–9 through one auditable adapter/assembly boundary, without reimplementing their algorithms. The closure audit must prove that Point 6 duplicate detection, Point 7 fingerprint integrity, Point 8 tamper detection, and Point 9 canonical uniqueness are not bypassed by the compatibility path in AmarEvidenceQualityEngine.

The legacy compatibility report may remain available for existing consumers, but it must not be the authoritative certification input when the canonical Point 10 path is available. Any aggregate score produced by compatibility code is an advisory/legacy signal and must not be allowed to turn mixed evidence into a constitutional VERIFIED result.

This is a wiring/integration gate, not a new evidence-quality formula.

## Trading-domain completeness mandate

The project goal is to make Amar AI a trading-specialized research and decision-support agent, not to promise a 100% win rate or guaranteed profit. The trading mandate is therefore defined as completeness of domain coverage and verification, not certainty of market outcome.

A future trade-search request must be able to route through the appropriate research mode (for example, a fast or expert mode selected by the user), gather current and historical market evidence, inspect relevant indicators/strategies/schools, reproduce calculations where possible, run controlled backtests/forward tests when data and tooling permit, compare conflicting evidence, and return a traceable trade hypothesis with entry/invalidity/target/risk assumptions and evidence provenance. It must never convert a backtest into a promise of future profit.

TradingView documents that its strategies can be backtested and forward-tested and that community scripts include indicators, strategies, and libraries; these are useful research targets, subject to access, licensing, source-quality, and reproducibility controls. MetaTrader 5 documents multi-symbol strategy testing and multiple tick-generation modes, which should inform future MT5-compatible research/testing adapters rather than being duplicated blindly.

The agent must preserve a strict distinction among market evidence, analysis, backtest evidence, trade hypothesis/recommendation, and execution. No component may claim that a simulated/backtested result is an actual trading result. CFTC guidance identifies hindsight, liquidity, spread/execution, fees, and other limitations of hypothetical/simulated results and states that no trading system can guarantee profits.

## Additional control adopted for Points 10–24

Evidence-to-Decision Traceability is now an explicit closure requirement: every downstream trade recommendation must be traceable to the evidence set, timestamps/data cutoff, analytical methods, backtest experiment identity (if used), conflicts, uncertainty state, and risk controls that produced it. Missing provenance or an unverifiable dependency must fail closed rather than being silently filled by model inference.

This strengthens the existing architecture without adding a scoring weight or changing the closed methodology of Points 1–9.
