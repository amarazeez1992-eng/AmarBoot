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
- Stage 7 — Execution Governance: **CLOSED OFFICIALLY / CONSTITUTIONALLY**
- Stage 8 — Central Command Plane: CLOSED
- Stage 9 — Parallel Workforce / Performance / Reliability: CLOSED
- Stage 10 — Final 100% Audit and Release Gate: OPEN / IN PROGRESS
- Stage 11 — AMAR INTELLIGENCE 99: IN PROGRESS

## Approved new roadmap additions

The following requirements are now formally part of the roadmap and are not considered implemented merely because they are documented.

### Stage 10 — Security / Release Boundary additions
1. **Owner-controlled GitHub authorization** — explicit GitHub connection/authorization, grant/revoke/inspect, least privilege, auditable, no secrets committed to the repository.
2. **Central Provider security contract** — define the future Amar AI provider/API boundary and the owner-controlled **ABL** credential family; ABL is a credential architecture concept, not a hard-coded secret.
3. **Provider security audit** — secret handling, credential rotation/revocation, client isolation, provenance, logging, and release-boundary verification.

### Stage 11 — Intelligence / Memory / Provider additions
4. **GitHub-backed explicit memory** — when the user explicitly says "save this", store the item in a dedicated, human-readable, searchable GitHub-backed memory area with timestamps and provenance. User can inspect/update/delete saved entries.
5. **Central ABL Provider** — Amar AI acts as the central provider for approved future apps/agents; owner-issued ABL credentials are scoped, revocable, auditable, and rotatable. No client bypasses central governance.
6. **Automatic source refresh** — important saved/source-backed information can be re-checked against authoritative sources on a selectable schedule of **every 6 hours** or **every 24 hours**. Record source, timestamp, detected changes, previous value, and verification status. Ambiguous/material changes require review rather than silent overwrite.
7. **Provider + Memory + Research integration** — central coordination of GitHub memory, source refresh, research modes, evidence chains, and future external clients, with no governance bypass.

### Approved future reliability addition
8. **Smart Monitoring & Rollback Layer** — a future reliability/safety layer that continuously monitors quality, runtime health, repeated failures, regressions and critical degradation; compares the active state with the last verified safe state; fails closed when integrity is uncertain; and rolls back to the last verified safe state when a rollback condition is met. It must never silently rewrite itself or change policy without an explicit audited change. This is approved scope for a later implementation stage, not a current implementation claim.

Detailed scope: `docs/governance/STAGE10_11_CENTRAL_PROVIDER_MEMORY_SYNC_SCOPE.md`.

## Stage 11 item status

- 1. Intelligence Core: IN PROGRESS — final verification pending
- 2. Verification Layer: IN PROGRESS — final verification pending
- 3. Advanced Memory: IN PROGRESS — final verification pending
- 4. Research Engine: IN PROGRESS — final verification pending
- 5. Decision Intelligence: PENDING
- 6. Self-Critique: PENDING
- 7. Benchmark & Quality: PENDING
- 8. Observability: PENDING
- 9. Final 99% Gate: PENDING
- 10. Multimodal Agent Interface & Workspace: PENDING — expanded interface/engine package
- 11. Central Provider + GitHub Memory + Source Sync: PENDING — approved addition
- 12. Smart Monitoring & Rollback Layer: PENDING — approved future reliability addition

## Stage 7 — Execution Governance — final constitutional closure evidence

- Verified production/test implementation commit: `94e5ffaa033ed2af49be41cd5a6c94258ffa1d86`
- Evidence-record commit: `7d12ee2bbe4024d9e64a58b9f86a081cee85f254` (documentation-only)
- Final evidence-record CI run: `35175619707`
- Focused governance tests: PASS
- Full unit-test suite: PASS
- Android debug build: PASS
- CI diagnostics upload: PASS
- Independent second-pass repository audit: PASS
- Closure record: `docs/governance/STAGE7_EXECUTION_GOVERNANCE.md`
- Closure status: **CLOSED OFFICIALLY / CONSTITUTIONALLY**

The closure is based on the complete evidence set as a whole, not on the latest comment, latest test, latest commit, or latest successful addition alone. The final CI run verified the evidence-record commit, which contains documentation only and does not alter the verified implementation.

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
- **owner-controlled GitHub authorization security boundary**
- **future ABL provider credential/API security contract**

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
11. Central Provider + GitHub Memory + Source Sync — PENDING
12. Smart Monitoring & Rollback Layer — PENDING

### Stage 11 — Central Provider + GitHub Memory + Source Sync contract

Item 11 explicitly includes:

- **GitHub Workspace authorization** — owner-controlled connection, inspect/grant/revoke, least privilege.
- **Explicit GitHub memory** — "save this" creates a visible dedicated memory record; no silent arbitrary persistence.
- **Memory organization** — searchable records with timestamps, provenance/source, update history and user-controlled deletion.
- **ABL central provider** — Amar AI can expose approved capabilities to future applications/agents through owner-issued ABL credentials.
- **ABL lifecycle** — issue, scope, inspect, rotate, revoke, expire and audit credentials.
- **Client isolation** — each external application receives only explicitly granted capabilities.
- **Automatic source refresh** — configurable 6-hour or 24-hour source re-checks for important saved/source-backed information.
- **Change detection** — preserve previous value, new value, source, timestamp, evidence and verification state; flag material/ambiguous changes for user review.
- **Research integration** — refresh jobs use the evidence-aware research layer and respect source permissions/rate limits.
- **Provider resilience** — unavailable external clients/engines must not compromise Amar's local intelligence or governance.

These capabilities are approved scope, not implementation claims.

### Stage 11 — Multimodal Agent Interface & Workspace contract

Item 10 explicitly includes the following capabilities. They are requirements to design, implement, test, and verify one at a time; listing them here does **not** mean they are already implemented.

1. **Camera / image input** — capture or attach images for agent analysis.
2. **Screen sharing / screen capture** — user-controlled screen sharing for visual diagnosis and guided assistance; permission must be explicit and revocable.
3. **Screen understanding** — agent can interpret the shared screen and identify visible UI state, controls, errors, and navigation context; it must not silently control unrelated apps.
4. **Application guidance mode** — user can show an app such as WhatsApp or Instagram and ask Amar to inspect a settings problem and guide the user step-by-step.
5. **Code reading engine** — analyze source code, configuration, logs, stack traces, diffs, and project files with language-aware parsing where available.
6. **APK analysis** — accept APK files for static inspection, manifest/permissions/package/resource analysis, and safe reporting; no arbitrary execution of untrusted APKs.
7. **Text/PDF/document analysis** — accept TXT, PDF and supported document formats and extract/search/summarize/analyze their contents.
8. **Image analysis** — attach photos/screenshots/diagrams/charts for visual analysis and evidence extraction.
9. **Video analysis** — attach video for frame-aware visual inspection, with bounded processing and user-controlled upload/retention.
10. **Voice conversation** — speech-to-text input plus text-to-speech/audio responses for natural two-way voice interaction.
11. **Unified multimodal session** — text, voice, image, video, screen and files share one controlled session context without leaking data between sessions.
12. **File handoff to Agent** — controlled attachment of APK, TXT, PDF, code archives, logs, images and supported documents directly to the Agent workspace.
13. **Engine orchestration** — connect the available analysis engines through a central coordinator so the Agent can select one engine or a bounded combination according to the task.
14. **Agent workforce / engine pool** — trading, market analysis, research, code analysis, document analysis, vision, voice and verification engines can operate as a coordinated workforce when requested, with explicit scope and resource limits.
15. **Trading integration** — the same orchestration layer can route trading requests to the trading-analysis engines while preserving all existing execution governance and fail-closed controls.
16. **Research modes** — expose explicit research depth modes: **Quick Search**, **Standard Search**, **Expert Search**, and **Deep Research**, with different evidence/time/source budgets. Mode selection must be visible and auditable.
17. **Evidence-aware search** — every research mode records source scope, evidence used, confidence/limitations, and whether external sources were permitted.
18. **Permission and privacy controls** — camera, microphone, screen, files and external-app viewing require explicit user permission, clear active-state indication, revocation, and safe shutdown.
19. **Session safety / isolation** — uploaded files and multimodal content are sandboxed and scoped to the current task/session; sensitive content is not retained beyond configured policy.
20. **Graceful fallback** — if one AI/vision/voice/search engine is unavailable, the Agent falls back to an available compatible engine or clearly reports the limitation instead of silently failing.

### Workforce rule

The engine pool is a coordinated workforce, not uncontrolled parallel execution. The central Agent must select, sequence, combine, cancel, and verify engines according to task scope, evidence requirements, resource limits, and safety policy. No engine may bypass the central governance contracts.

### Research mode rule

Quick/Standard/Expert/Deep Research are **research-depth modes**, not accuracy guarantees. Higher depth means broader/deeper evidence collection and verification within configured limits. The Agent must distinguish sourced facts, analysis, uncertainty, and unsupported claims.

### Smart Monitoring & Rollback contract

Item 12 explicitly includes:

- health monitoring for core services and critical agent subsystems;
- quality/regression/degradation detection;
- repeated-failure and anomaly detection;
- comparison with the last verified safe state;
- fail-closed behavior when integrity cannot be established;
- controlled rollback to the last verified safe state;
- auditable rollback reason, evidence and resulting state;
- no silent self-rewrite, policy mutation or hidden self-modification;
- recovery verification before returning a subsystem to normal operation.

This is approved future scope, not an implementation claim.

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
