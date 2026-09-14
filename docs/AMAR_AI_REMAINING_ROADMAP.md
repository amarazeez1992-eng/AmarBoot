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
- Stage 11 — AMAR INTELLIGENCE 99: PLANNED / NEXT

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

Purpose: raise Amar AI from a feature-oriented agent into a measurable, evidence-driven, self-checking intelligence platform. Stage 11 is planned as the next major expansion and must preserve the existing governance, safety, provenance, and regression contracts.

### Ordered execution sequence

1. Intelligence Core
   - Perception Engine
   - Reasoning Engine
   - Confidence Engine

2. Verification Layer
   - Evidence Engine
   - Source Registry
   - Conflict Detection
   - Provenance Chain

3. Advanced Memory
   - Short/working/long-term memory
   - Evidence memory
   - Decision memory
   - Decision Replay

4. Research Engine
   - Deep Research Orchestrator
   - Parallel research
   - Source ranking and verification

5. Decision Intelligence
   - Scenario Engine
   - Simulation Lab
   - Risk intelligence
   - Explainability Engine

6. Self-Critique
   - Self-review
   - Error Intelligence
   - Root-cause correction
   - Permanent regression tests

7. Benchmark & Quality
   - Benchmark Lab
   - Accuracy and consistency metrics
   - Confidence calibration
   - Regression intelligence

8. Observability
   - AI health monitoring
   - Performance diagnostics
   - Capability/worker telemetry
   - Recovery visibility

9. Final 99% Gate
   - Full security audit
   - Full regression suite
   - Architecture audit
   - Performance/benchmark audit
   - Provenance/license audit
   - Zero unresolved critical/high defects

### Stage 11 architecture target

```text
AMAR AI
  -> CENTRAL BRAIN
     -> Perception
     -> Reasoning
     -> Memory
     -> Intelligence Core
        -> Research
        -> Verification
        -> Conflict
        -> Confidence
        -> Decision
        -> Risk
        -> Simulation
        -> Self-Critique
     -> Governance Gate
     -> Action / Audit
```

### Non-negotiable Stage 11 rule

The 99% score is not a claim made by design. It must be earned through measurable benchmarks, evidence quality, regression results, security verification, architecture review, and CI evidence. No Stage 11 sub-stage is closed from design alone.

## Non-negotiable principles

- Build, do not destroy.
- Correct deeply; do not simplify away safety or evidence.
- Keep contracts and engines separated for auditability.
- Amar AI remains the central intelligence and policy coordinator inside the app.
- Open-source components require provenance and license evidence before reuse.
- Financial execution remains behind explicit user delegation, risk controls, security controls, ACK, read-back, reconciliation, and audit.
- The Android application remains the primary operating surface.
- Stage 11 must follow: inspect → design → implement → re-inspect → focused test → full test → CI → audit → close.
