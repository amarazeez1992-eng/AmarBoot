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
- Stage 11 — AMAR INTELLIGENCE 99: IN PROGRESS

### Stage 11 item status

- 1. Intelligence Core: CLOSED
- 2. Verification Layer: CLOSED
- 3. Advanced Memory: CLOSED
- 4. Research Engine: CLOSED
- 5. Decision Intelligence: PENDING
- 6. Self-Critique: PENDING
- 7. Benchmark & Quality: PENDING
- 8. Observability: PENDING
- 9. Final 99% Gate: PENDING
- 10. Multimodal Agent Interface & Workspace: PENDING — highest-priority interface package

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

1. Intelligence Core — CLOSED
   - Perception Engine
   - Reasoning Engine
   - Confidence Engine

2. Verification Layer — CLOSED
   - Evidence Engine
   - Source Registry
   - Conflict Detection
   - Provenance Chain

3. Advanced Memory — CLOSED
   - Short/working/long-term memory
   - Evidence memory
   - Decision memory
   - Decision Replay

4. Research Engine — CLOSED
   - Deep Research Orchestrator
   - Parallel research
   - Source ranking and verification

5. Decision Intelligence — PENDING
   - Scenario Engine
   - Simulation Lab
   - Risk intelligence
   - Explainability Engine

6. Self-Critique — PENDING
   - Self-review
   - Error Intelligence
   - Root-cause correction
   - Permanent regression tests

7. Benchmark & Quality — PENDING
   - Benchmark Lab
   - Accuracy and consistency metrics
   - Confidence calibration
   - Regression intelligence

8. Observability — PENDING
   - AI health monitoring
   - Performance diagnostics
   - Capability/worker telemetry
   - Recovery visibility

9. Final 99% Gate — PENDING
   - Full security audit
   - Full regression suite
   - Architecture audit
   - Performance/benchmark audit
   - Provenance/license audit
   - Zero unresolved critical/high defects

10. Multimodal Agent Interface & Workspace — PENDING — highest-priority interface package
   - Unified multimodal session orchestrator: text, voice, URL, code, files, and artifacts enter one consistent agent context without duplicating state
   - Microphone input with explicit permission, speech-to-text, streaming partial transcripts, interruption/barge-in, turn detection, and recovery from recognition failures
   - Natural two-way voice conversation with interruption/turn-taking support, cancellation, latency budgets, and graceful text fallback
   - Text and voice conversation share the same agent context and memory policy; modality changes must not silently lose context
   - External URL intake with safe fetch, redirect/timeout/size limits, content-type validation, parsing, analysis, source/evidence tracking, snapshot/provenance identity, and clear failure reasons
   - Source-code intake for common programming languages with language detection, encoding detection, parser/static-analyzer adapters where supported, generic-text fallback, explanation, defect detection, dependency/context inspection, and report generation
   - File intake pipeline with content-based type detection, archive inspection, safe extraction limits, text/document/image analysis, metadata inspection, and format-specific analyzers
   - APK analysis through a sandboxed/read-only inspection path with manifest/resource/code/signature/package metadata analysis; never implicitly install or execute untrusted binaries
   - Persistent artifact workspace with stable artifact IDs, content hashes, explicit names, versions, timestamps, provenance, source references, analysis status, and retrieval metadata
   - Artifact lineage: every generated report or derived artifact records exactly which source/version produced it and which analysis configuration was used
   - Reproducible analysis: same immutable artifact + same analyzer/version/configuration must produce a traceable, comparable result
   - Safe workspace lifecycle: quarantine, active-analysis, verified, archived, and deleted states with explicit transitions
   - GitHub save workflow: when explicitly instructed, create/update a named file or artifact in the authorized repository; verify repository/branch/path/overwrite intent; preserve commit identity and provenance; never silently overwrite or execute
   - Execution is a separate governed action: save does not imply run, install, deploy, merge, publish, or trade execution
   - Explicit action confirmation for destructive, external, privileged, or execution-capable operations
   - Capability firewall between analysis and execution so reading/analyzing code, APKs, ZIPs, or scripts cannot itself trigger execution
   - Resource governance for CPU, memory, storage, network, archive expansion, file count, input size, analysis time, and concurrency with fail-closed limits
   - Security isolation for untrusted inputs, including path-traversal prevention, decompression-bomb protection, dangerous-file handling, network restrictions, and malware-risk quarantine signals
   - Privacy protection: minimize retained sensitive content, support redaction where applicable, and keep provenance without unnecessarily duplicating raw private data
   - Exportable analysis reports with stable references, findings, evidence, analyzer versions, limitations, and reproducibility metadata
   - Workspace search and retrieval by name, ID, hash, source, version, language/type, date, and analysis status
   - Offline-first degradation: core inspection and workspace operations remain useful without network access; network-dependent capabilities fail clearly rather than silently pretending success
   - Adapter architecture so new languages, file formats, URL parsers, speech engines, and analyzers can be added without changing the central agent contract
   - Contract-level regression tests for every modality and every boundary: voice→context, URL→evidence, file→artifact, code→analysis, APK→sandbox, artifact→GitHub, and save→execute separation
   - Performance targets measured for first response, voice latency, file ingestion, analysis throughput, workspace retrieval, and cancellation responsiveness
   - Security and reliability telemetry for rejected inputs, quarantines, timeouts, cancellations, parser failures, analyzer failures, and resource-limit events
   - No hidden network calls, hidden execution, hidden persistence, or hidden external writes

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
     -> MULTIMODAL INTERFACE
        -> Session Orchestrator
        -> Voice / STT / TTS
        -> URLs / Safe Fetch
        -> Code / Static Analysis
        -> Files / Archives / APK
        -> Artifact Workspace
        -> Provenance / Lineage
        -> GitHub Workspace
     -> SECURITY / RESOURCE FIREWALL
     -> GOVERNANCE GATE
     -> ACTION / AUDIT
```

### Non-negotiable Stage 11 rule

The 99% score is not a claim made by design. It must be earned through measurable benchmarks, evidence quality, regression results, security verification, architecture review, and CI evidence. No Stage 11 sub-stage is closed from design alone.

For every numbered item, the mandatory completion loop is:

**investigate → implement → inspect → test → confirm → prove → audit → ready → close**

No item may advance until that loop has produced evidence.

## Non-negotiable principles

- Build, do not destroy.
- Correct deeply; do not simplify away safety or evidence.
- Keep contracts and engines separated for auditability.
- Amar AI remains the central intelligence and policy coordinator inside the app.
- Open-source components require provenance and license evidence before reuse.
- Financial execution remains behind explicit user delegation, risk controls, security controls, ACK, read-back, reconciliation, and audit.
- The Android application remains the primary operating surface.
- Stage 11 must follow: inspect → design → implement → re-inspect → focused test → full test → CI → audit → close.
