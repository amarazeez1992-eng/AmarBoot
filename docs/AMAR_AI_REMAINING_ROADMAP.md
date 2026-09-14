# AMAR AI — Remaining Agent Roadmap

This roadmap governs the remaining Agent work. A stage is CLOSED only after implementation, focused tests, full unit tests, build verification, and explicit audit evidence. No later stage is considered complete while the previous stage is failing.

## Current baseline

Stages 1–5 of the Agent foundation are now CLOSED: budget/policy safety, multi-role deliberation, evidence hardening, memory, unified evidence/freshness, deterministic simulation/risk/crisis/audit, and the first deterministic strategy compiler/research workforce.

## Remaining 30% — six gated stages

### Stage 6 — Open-Source Market Intelligence and Indicator Engines (70–75%)
- provider-neutral source registry
- open-source indicator adapters
- technical, statistical and market-structure engines
- source licensing/provenance records
- parallel analysis with deterministic aggregation
- CLOSE GATE: source provenance, indicator correctness, performance and full CI

**Implementation baseline started:** provider-neutral source registry, deterministic built-in SMA/EMA/RSI/ATR adapter, statistical return analysis, market-structure analysis, bounded deterministic aggregation, provenance/license documentation, and focused/full-test workflow are now present. Stage 6 remains **OPEN** until CI, build, performance evidence and final audit are verified.

### Stage 7 — Execution Governance and User Delegation (75–85%)
- central capability/permission model
- explicit user delegation and revocation
- execution proposal -> validation -> risk -> security -> approval -> gateway
- idempotency, ACK, read-back, reconciliation and emergency lock
- live execution remains opt-in and fail-closed
- laptop remains secondary; mobile app remains the primary operating surface
- CLOSE GATE: demo-only execution verification, failure injection and full CI

### Stage 8 — Central Command Plane and Application Authority (85–90%)
- Amar AI becomes the application command authority
- subordinate control-room modules consume Agent decisions rather than owning policy
- capability routing, audit visibility and emergency controls
- bounded device permissions; no unrestricted device authority
- CLOSE GATE: permission matrix, revocation tests, lifecycle tests and full CI

### Stage 9 — Performance, Parallel Workforce and Reliability (90–95%)
- parallel independent research/analysis workers
- caching and bounded memory
- latency budgets and cancellation
- deterministic result aggregation
- offline/partial-connectivity behavior where possible
- CLOSE GATE: load, timeout, concurrency, recovery and full CI

### Stage 10 — Final 100% Audit and Release Gate (95–100%)
- complete security audit
- dependency/license/provenance audit
- complete regression suite
- Android release build and artifact verification
- final architecture consistency review
- documentation and open-source release hygiene
- CLOSE GATE: zero unresolved critical/high defects and successful CI/release verification

## Design principles

- Build, do not destroy.
- Correct deeply; do not simplify away safety or evidence.
- Keep contracts and engines in separate files for auditability.
- The Agent is the central intelligence and policy coordinator inside the app.
- External models are not required as another agent.
- Open-source components are admitted only with license/provenance checks.
- Financial execution is always behind explicit user delegation, risk controls, security controls and an auditable gateway.
- The laptop is secondary; the Android application is the primary operating surface.
