# AMAR AI — Remaining Agent Roadmap

This roadmap is the authoritative stage tracker. A stage is CLOSED only after implementation, focused tests, full unit tests, build verification, CI, and explicit architecture/regression audit evidence.

**Binding project law:** `docs/governance/AMAR_PROJECT_CONSTITUTION.md` governs all closure decisions. No roadmap status may override missing evidence.

## Current status

- Stage 1 — Agent Foundation: CLOSED
- Stage 2 — Internal Intelligence / Workforce: CLOSED
- Stage 3 — Memory & Evidence: CLOSED
- Stage 4 — Simulation / Risk / Crisis / Audit: CLOSED
- Stage 5 — Hardening & Strategy: CLOSED
- Stage 6 — Amar Intelligence Expansion: CLOSED
- Stage 7 — Execution Governance: CLOSED — current verification evidence recorded below
- Stage 8 — Central Command Plane: CLOSED
- Stage 9 — Parallel Workforce / Performance / Reliability: CLOSED
- Stage 10 — Final 100% Audit and Release Gate: OPEN / IN PROGRESS
- Stage 11 — AMAR INTELLIGENCE 99: IN PROGRESS

### Stage 11 item status

- 1. Intelligence Core: IN PROGRESS — final verification pending
- 2. Verification Layer: IN PROGRESS — final verification pending
- 3. Advanced Memory: IN PROGRESS — final verification pending
- 4. Research Engine: IN PROGRESS — final verification pending
- 5. Decision Intelligence: PENDING
- 6. Self-Critique: PENDING
- 7. Benchmark & Quality: PENDING
- 8. Observability: PENDING
- 9. Final 99% Gate: PENDING
- 10. Multimodal Agent Interface & Workspace: PENDING — highest-priority interface package

## Stage 7 — Execution Governance — closure evidence

- Implementation verification commit: `08942187a2a154c421edef730521646dc6ca1621`
- CI run: `35174033338`
- Focused governance tests: PASS
- Full unit-test suite: PASS
- Android debug build: PASS
- Independent second-pass repository audit: PASS
- Closure record: `docs/governance/STAGE7_EXECUTION_GOVERNANCE.md`
- Closure status: **CLOSED**

## Stage 10 — Final 100% Audit and Release Gate

Scope:
- complete security audit
- dependency/license/provenance audit
- complete regression suite
- Android debug and release build verification
- release artifact verification
- final architecture consistency review
- documentation and open-source release hygiene
- zero unresolved critical/high defects within the Android release scope
- successful CI/release verification

The Stage 10 audit gate is implemented in `docs/governance/STAGE10_FINAL_AUDIT.md` and `.github/workflows/amar-stage-ten.yml`.

The protected MT5 EA baseline is explicitly outside the Android release artifact. It remains non-live and requires a separate controlled demo/runtime hardening gate before any execution is enabled. It is not an Android release blocker while that boundary remains enforced.

## Stage 11 — AMAR INTELLIGENCE 99

Purpose: raise Amar AI from a feature-oriented agent into a measurable, evidence-driven, self-checking intelligence platform. Stage 11 is active and must preserve the existing governance, safety, provenance, and regression contracts.

### Ordered execution sequence

1. Intelligence Core — IN PROGRESS / final verification pending
2. Verification Layer — IN PROGRESS / final verification pending
3. Advanced Memory — IN PROGRESS / final verification pending
4. Research Engine — IN PROGRESS / final verification pending
5. Decision Intelligence — PENDING
6. Self-Critique — PENDING
7. Benchmark & Quality — PENDING
8. Observability — PENDING
9. Final 99% Gate — PENDING
10. Multimodal Agent Interface & Workspace — PENDING — highest-priority interface package

### Non-negotiable Stage 11 rule

The 99% score is not a claim made by design. It must be earned through measurable benchmarks, evidence quality, regression results, security verification, architecture review, and CI evidence. No Stage 11 sub-stage is closed from design alone.

For every numbered item, the mandatory completion loop is:

**تحقيق → تنفيذ → فحص → اختبار → تأكيد → إثبات → تدقيق نهائي → جاهز → إغلاق**

No item may advance until that loop has produced current evidence. Previous CI success does not close later-modified implementation.

## Non-negotiable principles

- Build, do not destroy.
- Correct deeply; do not simplify away safety or evidence.
- Keep contracts and engines separated for auditability.
- Amar AI remains the central intelligence and policy coordinator inside the app.
- Open-source components require provenance and license evidence before reuse.
- Financial execution remains behind explicit user delegation, risk controls, security controls, ACK, read-back, reconciliation, and audit.
- The Android application remains the primary operating surface.
- The project closure law is defined in `docs/governance/AMAR_PROJECT_CONSTITUTION.md` and must be obeyed for every future stage and item.