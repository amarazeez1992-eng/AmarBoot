# Stage 11 — Item 3 — Point 10: Evidence Quality Score

## Constitutional audit record

Status: VERIFICATION PENDING — ROOT CORRECTION APPLIED

### Scope

Produce one deterministic evidence-quality certification result from facts already established by the upstream Evidence Engine points.

### Canonical boundary

Point 10 only certifies the already-established dimensions:
- authority score and its upstream verification state
- freshness score and its upstream verification state
- source independence state
- evidence uniqueness state

It does not collect evidence, detect semantic duplicates, verify fingerprints, detect tampering, verify claims, rank evidence, or resolve conflicts.

### Root-cause architecture

The former heuristic Point 10 scoring path has been replaced by the dedicated AmarEvidenceQualityScoreEngine.

The dedicated engine is the canonical Point 10 gate. Upstream dimensions remain owned by their respective earlier Evidence Engine points; Point 10 consumes their verified states and does not redefine their methodology.

Numeric values are audit attributes. They are not converted into a second percentage methodology. A valid upstream value below 1.0 is not automatically an unverified value; the owning upstream contract determines whether that value is verified. Point 10 consumes that verification state.

### Deterministic rule

There are no heuristic weights, estimated percentages, averaging, or partial scores in Point 10.

For a single evidence item:
- **1.0 (100% / VERIFIED)** only when every required Point 10 gate is fully verified.
- **0.0 (NOT VERIFIED)** when any required gate is not fully verified.

For an aggregate:
- **1.0 (100% / VERIFIED)** only when the input set is non-empty and every item is fully verified.
- **0.0 (NOT VERIFIED)** for an empty set or when any item fails a required gate.

The canonical Point 10 result therefore has no invented intermediate percentage.

### Invalid and future-value behavior

Point 10 is fail-closed.

- Non-finite numeric attributes cannot pass.
- Numeric attributes outside their defined bounded range cannot pass.
- An upstream freshness state of FUTURE cannot pass even if a raw freshness number would otherwise look fresh.
- Point 10 does not clamp, normalize, average, or otherwise repair upstream evidence-quality values.
- Validation and normalization remain the responsibility of the owning upstream boundary.

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

### Weighting policy

No new Point 10 weights are permitted.

There is no universal institutional percentage allocation that can be truthfully presented as a global standard for evidence quality. NIST's AI Risk Management Framework emphasizes validity, reliability, robustness, provenance, testing/evaluation, and monitoring rather than prescribing one universal percentage allocation. Those principles may inform future methodology decisions in the owning upstream points, but they do not justify inventing weights inside Point 10.

### Constitutional gate

This point remains **NOT CLOSED** until the exact Point 10 implementation state passes its dedicated boundary tests, relevant regression suite, full unit suite, Debug build, CI verification, and evidence recording, followed by constitutional re-inspection.
