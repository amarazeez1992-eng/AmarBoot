# AMAR AI — Remaining Agent Roadmap

This roadmap is the authoritative stage tracker. A stage is CLOSED only after implementation, focused tests, full unit tests, build verification, CI, and explicit architecture/regression audit evidence.

## Current status

- Stage 1 — Agent Foundation: CLOSED
- Stage 2 — Internal Intelligence / Workforce: CLOSED
- Stage 3 — Memory & Evidence: CLOSED
- Stage 4 — Simulation / Risk / Crisis / Audit: CLOSED
- Stage 5 — Hardening & Strategy: CLOSED
- Stage 6 — Amar Intelligence Expansion: CLOSED
- Stage 7 — Execution Governance: CLOSED
- Stage 8 — Central Command Plane: CLOSED
- Stage 9 — Parallel Workforce / Performance / Reliability: CLOSED
- Stage 10 — Final 100% Audit and Release Gate: OPEN / IN PROGRESS

## Stage 10 — Final 100% Audit and Release Gate

Scope:
- complete security audit
- dependency/license/provenance audit
- complete regression suite
- Android debug and release build verification
- release artifact verification
- final architecture consistency review
- documentation and open-source release hygiene
- zero unresolved critical/high defects
- successful CI/release verification

The Stage 10 audit gate is now implemented in `docs/governance/STAGE10_FINAL_AUDIT.md` and `.github/workflows/amar-stage-ten.yml`.

Current known blocker: the protected MT5 EA baseline remains a documented HIGH-risk artifact and is not approved for live trading. Stage 10 cannot be CLOSED while that release blocker remains unresolved under the zero-critical/high-defect rule.

## Non-negotiable principles

- Build, do not destroy.
- Correct deeply; do not simplify away safety or evidence.
- Keep contracts and engines separated for auditability.
- Amar AI remains the central intelligence and policy coordinator inside the app.
- Open-source components require provenance and license evidence before reuse.
- Financial execution remains behind explicit user delegation, risk controls, security controls, ACK, read-back, reconciliation, and audit.
- The Android application remains the primary operating surface.
