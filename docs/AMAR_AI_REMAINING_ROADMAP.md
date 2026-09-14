# AMAR AI — Remaining Agent Roadmap

This roadmap governs the remaining Agent work. A stage is CLOSED only after implementation, focused tests, full unit tests, build verification, and explicit audit evidence. No later stage is considered complete while the previous stage is failing.

## Current baseline

Stages 1–5 of the Agent foundation are now CLOSED: budget/policy safety, multi-role deliberation, evidence hardening, memory, unified evidence/freshness, deterministic simulation/risk/crisis/audit, and the first deterministic strategy compiler/research workforce.

## Remaining 30% — six gated stages

### Stage 6 — Open-Source Market Intelligence and Indicator Engines (70–75%)
- no fixed four-indicator ceiling: broad indicator namespace and capability catalog
- pluggable indicator-engine hub for any admitted technical-analysis implementation
- TA-Lib-class coverage target across overlap, momentum, volatility, volume, statistics, cycle, price-transform and candlestick-pattern families
- TradingView/Pine/community-script discovery as a research source, subject to the source's actual publication/licensing terms
- provider-neutral open-source research connectors for public web indexes, Git repositories, package registries, documentation and academic sources
- provenance + SPDX/license evidence required before code or algorithms become reusable dependencies
- technical, statistical, market-structure, pattern and quantitative engines
- parallel analysis with deterministic aggregation
- resource budgets are execution safeguards, not intelligence/indicator restrictions
- CLOSE GATE: broad capability coverage evidence, formula correctness, source provenance, performance, full CI, build verification and final audit

**Expanded implementation baseline:** Stage 6 now has a broad indicator namespace/catalog, an extensible indicator-engine hub with no hard four-indicator ceiling, and an open-source research gateway that can query multiple public connectors and fail closed when repository/license evidence is missing. The local built-in adapter remains intentionally small; advanced coverage is admitted through independently verified adapters rather than pretending unsupported formulas are implemented. Stage 6 remains **OPEN** until broad coverage, CI, build, performance and audit evidence are verified.

### Stage 7 — Execution Governance and User Delegation (75–85%)
- central capability/permission model
- explicit user delegation and revocation
- execution proposal -> validation -> risk -> security -> approval -> gateway
- idempotency, ACK, read-back, reconciliation and emergency lock
- live execution remains opt-in and fail-closed
- laptop remains secondary; mobile app remains the primary operating surface
- CLOSE GATE: demo-only execution verification, failure injection and full CI

### Stage 8 — Central Command Plane and Application Authority (85–90%) — CLOSED
- Amar AI becomes the application command authority
- subordinate control-room modules consume Agent decisions rather than owning policy
- capability routing, audit visibility and emergency controls
- bounded device permissions; no unrestricted device authority
- CLOSE GATE: permission matrix, revocation tests, lifecycle tests and full CI

**Closure evidence:** Stage 8 implementation, focused tests, full unit-test suite, debug build, CI workflow, regression guard, and architecture audit were verified successfully. The verified CI run is `34862221490` (job `104037088671`) on commit `db55367b2bdf735eb42b2eeffd584433a5288039`. The Stage 8 governance record is `docs/governance/STAGE8_CENTRAL_COMMAND_PLANE.md`.

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
- The Agent may research broadly across public sources; source admission remains evidence- and license-gated.
- Financial execution is always behind explicit user delegation, risk controls, security controls and an auditable gateway.
- The laptop is secondary; the Android application is the primary operating surface.
