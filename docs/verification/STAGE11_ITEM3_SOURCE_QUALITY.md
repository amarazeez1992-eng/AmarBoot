# Stage 11 — Item 3 — Point 2: Source Quality Analysis

**Status: CONSTITUTIONALLY CLOSED**

## Scope

Evaluate whether already-intaken evidence has the minimum source, evidence, and authority information required for downstream processing.

## Deterministic contract

Evidence is usable only when:
- source URI is present;
- evidence payload is present;
- authority is not UNKNOWN.

This point does not collect, rank, route, verify claims, or execute actions.

## Verification

Focused regression: `AmarSourceQualityAnalyzerTest`.
Repeated assessment is deterministic.
