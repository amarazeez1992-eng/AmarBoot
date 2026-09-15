# Stage 11 Item 5 — Clean Rebuild Scope

Status: CLOSED

This item was rebuilt from the Stage 11 Items 1–4 baseline commit `0ab1fa2bbf20d6475f2511497c0e85e92bf5e325`.

## Design authority

- Existing `DecisionEngine` is the B7 decision authority for this item.
- Existing `AmarQuantTradingMath` remains the quantitative primitive authority.
- No Item 5 second decision engine is introduced.
- No broker/execution dependency is introduced.

## Decision contract

- Qualitative market context remains the primary directional input.
- Quantitative risk is optional and evidence-driven.
- Only valid, finite quantitative metrics contribute to risk.
- Volatility requires at least four closing prices; historical VaR requires at least five losses.
- Insufficient samples are treated as unavailable evidence, not zero risk.
- Missing/invalid metrics are omitted rather than fabricated.
- Partial invalidity does not suppress independent valid metrics.
- Quantitative risk can only reduce score magnitude and confidence.
- Every proposal remains `executable = false`.

## Closure evidence

1. Focused Item 5 regression suite passed.
2. Full unit suite passed.
3. Debug build passed.
4. Architecture and execution-safety checks passed.
5. Fresh CI passed on the exact merged commit `748eec82f6b6baaca545739992d88b3873b72968`.
6. Final audit confirmed no legacy Item 5 engine or temporary verification artifacts remain.
7. Current-main Stage 5 transient test failure was rerun successfully and produced no production-code change.

## Final status

Item 5 is formally CLOSED based on the complete closure loop and current evidence.
