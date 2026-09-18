# Stage 11 — Item 3 — Point 10: Evidence Quality Score

## Constitutional audit record

Status: VERIFICATION PENDING — ROOT CORRECTION APPLIED

### Scope

Produce one deterministic evidence-quality certification result from facts already established by the upstream Evidence Engine points.

### Canonical upstream contract

Point 10 now has an explicit upstream-state contract: `AmarEvidenceQualityUpstreamState`.

The contract contains one authoritative verification state for each Point 1–9 boundary:

1. Evidence Intake
2. Source Quality Analysis
3. Authority Scoring
4. Freshness Engine
5. Source Independence
6. Duplicate Evidence Detection
7. Fingerprint Integrity
8. Evidence Tampering Detection
9. Evidence Uniqueness

Point 10 must consume these established states. It must not recompute them, infer them from raw fields, or substitute a local heuristic.

A missing upstream state is not treated as success. The constitutional integration step must connect the actual Point 1–9 outputs to this contract before Point 10 can close.

### Canonical boundary

The dedicated `AmarEvidenceQualityScoreEngine` remains the certification gate.

Numeric authority/freshness values are audit attributes. They are not converted into a second percentage methodology. A valid upstream value below 1.0 is not automatically an unverified value; the owning upstream contract determines whether that value is verified.

The current compatibility `AmarEvidenceQualityEngine` remains an advisory/legacy adapter and is not sufficient evidence for constitutional Point 10 closure until its inputs are sourced from the canonical Point 1–9 state contract.

### Deterministic rule

There are no heuristic weights, estimated percentages, averaging, or partial scores in Point 10.

For a single evidence item:
- **1.0 (100% / VERIFIED)** only when every required upstream state is fully verified and all Point 10 numeric audit attributes are finite and within their defined range.
- **0.0 (NOT VERIFIED)** when any required state or numeric validity gate fails.

For an aggregate:
- **1.0 (100% / VERIFIED)** only when the input set is non-empty and every item is fully verified.
- **0.0 (NOT VERIFIED)** for an empty set or when any item fails a required gate.

### Invalid and future-value behavior

Point 10 is fail-closed.

- Non-finite numeric attributes cannot pass.
- Numeric attributes outside their defined bounded range cannot pass.
- An upstream freshness state of FUTURE cannot pass.
- Point 10 does not clamp, normalize, average, or repair upstream values.
- Validation and normalization remain the responsibility of the owning upstream boundary.

### Trading-agent architectural requirement

The trading recommendation capability must be downstream of evidence certification, not embedded in Point 10.

A future “find me a trade” request must be able to traverse:
market/data intake → evidence verification → multi-timeframe market analysis → applicable trading-school lenses → indicator/price-action/order-flow analysis → candidate generation → deterministic backtest/forward-test controls → risk analysis → conflict/uncertainty handling → final recommendation.

This is a capability architecture, not a promise of profitable or error-free trades. No stage may convert a backtest into a guarantee of future returns.

### Backtest trust boundary

TradingView documents that strategies simulate orders and that backtest/forward-test results are hypothetical; it also documents that non-standard charts can produce unrealistic strategy results and that out-of-sample testing helps address overfitting. These controls must be treated as evidence requirements, not as proof of future profitability.

External regulatory guidance likewise identifies hindsight, liquidity, and execution differences as limitations of simulated or hypothetical results. Therefore the agent must label simulated results explicitly and preserve dataset, execution-cost, and validation assumptions.

### Regression coverage

The dedicated Point 10 regression suite must prove:
- fully verified states produce exactly 1.0
- each individual verification-state failure produces exactly 0.0
- invalid/non-finite numeric attributes fail closed
- future evidence cannot pass
- aggregate certification requires every item to pass
- empty aggregation is NOT VERIFIED
- repeated execution with identical inputs is deterministic
- the result remains strictly bounded to the two canonical outcomes

The upstream-state contract regression suite must additionally prove:
- every Point 1–9 state is mandatory
- every individual upstream failure blocks the aggregate verification state
- identical upstream state is deterministic

### Weighting policy

No new Point 10 weights are permitted.

There is no universal institutional percentage allocation that can be truthfully presented as a global standard for evidence quality. NIST emphasizes validity, reliability, robustness, provenance, testing/evaluation, and monitoring rather than prescribing one universal percentage allocation.

### Constitutional gate

This point remains **NOT CLOSED** until:
1. the actual Point 1–9 outputs are connected to `AmarEvidenceQualityUpstreamState`;
2. the dedicated boundary tests pass;
3. the relevant regression suite passes;
4. the full unit suite passes;
5. Debug and Release builds pass;
6. CI evidence is recorded;
7. security/boundary checks pass;
8. documentation and artifacts are re-inspected; and
9. the constitutional verification gate approves closure.
