# AMAR AI — Direct Engine Binding

**Status:** APPROVED BY OWNER

## Requirement

AMAR AI commands must be produced from real application engines and measured runtime state, not from an intelligence Registry, catalog text, or prompt-only claims.

## Direct bindings

- **Market:** `AmarMarketStateStore` and, when installed, `AmarMt5RuntimeRegistry` read-only market data.
- **Grid:** `AmarGridPlanningEngine` calculates deterministic grid levels and lot progression.
- **Tracking:** `AmarMt5RuntimeRegistry` → read-only MT5 positions when a runtime is installed; otherwise fail-closed with `MT5_RUNTIME_NOT_INSTALLED`.
- **Risk:** `AmarTradingPrecisionEngine` provides the current research/risk gate.
- **Strategy / Validation:** `AmarStrategyValidationEngine` evaluates supplied measured R results; it never invents data.
- **Candle analysis:** MT5 runtime candle refresh is required before reporting candle direction or body percentage.

## AI authority

The Registry remains discovery/context only. It is not treated as proof that an engine executed an action.

Execution intents remain `PENDING_MT5` until the final execution stage.

## Final stage

Only after the Android application and its internal engines are complete do we connect:

`AMAR AI → Android command record → laptop → CMG command panel → Bridge → MT5 → ACK → fresh state → verification → reconciliation → audit`

The Android phase does not enable live broker execution and does not modify the approved MT5 EA.
