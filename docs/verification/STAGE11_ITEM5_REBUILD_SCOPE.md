# Stage 11 Item 5 — Clean Rebuild Scope

Status: IN PROGRESS

This item was rebuilt from the Stage 11 Items 1–4 baseline commit `0ab1fa2bbf20d6475f2511497c0e85e92bf5e325`.

## Design authority

- Existing `DecisionEngine` remains the single decision authority.
- Existing `AmarQuantTradingMath` remains the quantitative primitive authority.
- No second decision engine is introduced.
- No broker/execution dependency is introduced.

## Decision contract

- Qualitative market context remains the primary directional input.
- Quantitative risk is optional and evidence-driven.
- Only valid, finite quantitative metrics contribute to risk.
- Missing/invalid metrics are omitted rather than fabricated.
- Quantitative risk can only reduce score magnitude and confidence.
- Every proposal remains `executable = false`.

## Closure evidence required

1. Focused Item 5 regression suite passes.
2. Full unit suite passes.
3. Debug build passes.
4. Architecture and execution-safety checks pass.
5. Fresh CI runs on the exact current commit.
6. Final audit confirms no legacy Item 5 engine or temporary verification artifacts remain.

This document is the single Item 5 rebuild scope; no duplicate audit-marker documents are required.
