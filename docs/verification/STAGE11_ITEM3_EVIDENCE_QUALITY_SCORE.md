# Stage 11 — Item 3 — Point 10: Evidence Quality Score

## Constitutional audit record

Status: VERIFICATION PENDING — ROOT CORRECTION APPLIED

### Scope
Produce one deterministic, bounded evidence-quality score from dimensions already established upstream.

### Canonical boundary
Point 10 only aggregates:
- authority score
- freshness score
- source independence state
- evidence uniqueness state

It does not collect evidence, detect semantic duplicates, verify fingerprints, detect tampering, or verify claims.

### Root-cause architecture
The scoring formula was previously embedded inside the general Stage Two hardening model. It has been extracted into the dedicated AmarEvidenceQualityScoreEngine, and the existing quality item delegates to that single implementation. This removes duplicate scoring logic without changing the upstream evidence boundaries.

### Deterministic formula
authority × 0.45 + freshness × 0.20 + independence × 0.20 + uniqueness × 0.15

All dimensions are clamped to [0,1]; the final score is clamped to [0,1].

### Regression coverage
- perfect dimensions produce 1.0
- weak dimensions remain bounded and deterministic
- invalid ranges are clamped
- empty aggregation returns 0.0
- aggregate output remains bounded

### Root correction record

The first Point 10 verification run exposed a test-contract mismatch: the perfect-dimensions assertion did not match the intended bounded scoring formula. The scorer and regression contract were aligned so perfect dimensions yield 1.0, while invalid dimensions are clamped before weighting. A dedicated Point 10 gate was also added to the Stage Eleven workflow so Item 3 cannot bypass this boundary.

### Constitutional gate
This point remains NOT CLOSED until the exact Point 10 state passes its dedicated boundary test, full unit suite, Debug build, and the resulting CI evidence is directly verified.
