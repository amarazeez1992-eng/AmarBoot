# Stage 11 — Item 10 Multimodal Agent Interface & Workspace

## Status

**IN PROGRESS**

## Purpose

Build the multimodal agent/workspace layer without weakening existing intelligence, memory, research, observability, safety, provenance, or execution boundaries.

## Approved capabilities

1. **Advanced Reasoning Core** — multi-step reasoning, multi-engine result fusion, uncertainty handling, self-critique integration, evidence-based answers, and deterministic behavior where required.
2. **Multimodal Perception** — text, image, audio/speech, video, camera, and shared-screen understanding. Video/camera analysis must identify speech/text, scenes/events, visible elements, motion, summaries, questions about segments, and important timestamps without fabrication.
3. **Live Camera Workspace** — explicit user-initiated camera sharing, continuous or session-scoped camera understanding, visible objects/text/UI/environment interpretation, re-analysis as the camera view changes, and step-by-step guidance. The agent must only claim what is actually visible and sufficiently understood.
4. **Live Screen Workspace** — explicit user-initiated screen sharing, continuous screen understanding, visible text/UI/error interpretation, re-analysis as the screen changes, and step-by-step guidance.
5. **Optional Device/App Control** — maximum control permitted by explicit OS/user permissions; tap/type/scroll/open/close/multi-step actions where authorized. No authentication, security, OS, camera, microphone, or permission bypass.
6. **Immediate Stop + Sensitive Confirmation** — stop control and confirmation gates for payments, deletion, money movement, publication, or other consequential actions. Camera/screen sharing must have an explicit visible stop state.
7. **Persistent Conversation Memory** — every conversation may be stored with conversation id, title, domain/topic, date, summary, keywords, messages, provenance, and retention state. Conversations remain separately addressable and searchable.
8. **Explicit Save/Delete Memory Commands** — user can say "save this" to persist a selected item and "delete this" to remove the selected memory. Deletion must prevent stale promoted copies from silently reappearing.
9. **Conversation Retrieval** — reopen a prior conversation with its context; search one conversation or all conversations; link conversations to a topic; pin/archive/delete/export; user-controlled retention.
10. **Knowledge Promotion** — raw conversation history is not automatically authoritative knowledge. Promotion to long-term knowledge requires validation, provenance, source/evidence linkage, and conflict state.
11. **Knowledge Engine** — validated knowledge is retrievable and can be updated, superseded, or removed with provenance preserved.
12. **Continuous Knowledge Discovery** — permitted public web sources, new sites, applications, documents, technologies, and information can be discovered and analyzed. New information is not accepted as fact without validation.
13. **Two Web Modes** — `RESTRICTED_SEARCH` (bounded tools, trusted-source preference, sensitive confirmations) and `OPEN_SEARCH` (broad public-web/site/document access, fast/deep/multi-source retrieval). OPEN_SEARCH does not bypass authentication, security controls, paywalls, OS permissions, or access restrictions.
14. **Trusted Source Engine** — source reliability, independence/diversity, provenance, citation traceability, conflict detection, and fail-closed behavior when evidence is insufficient.
15. **Multi-Expert Analysis** — multiple analysis engines can independently process a task and a fusion layer resolves agreement, disagreement, confidence, and evidence quality.
16. **Tool/Connector Layer** — websites, APIs, files, open-source programs, and future connectors use explicit permissions and auditability. No connector silently gains execution authority.
17. **Observability** — critical multimodal, memory, research, tool, permission, camera, screen, and deletion actions are auditable and traceable.
18. **Open Source Architecture** — provider/engine adapters must allow suitable open-source implementations to be integrated without replacing authoritative project contracts.
19. **Target quality** — engineering target is 9.9/10, earned through measurable tests and evidence rather than asserted by design.
20. **Centralized User Settings** — the user must control configurable behavior from one authoritative settings model. Capability preferences support `OPEN`, `LIMITED`, and `OFF` where applicable; camera/screen/device-control access supports explicit session/action policies; web mode, memory behavior, knowledge promotion, sensitive-action confirmation, answer detail, evidence visibility, progress visibility, copy/paste/notebook/export, workspace resizing, theme, dynamic/static/automatic background, background selection, colors, font family, font size, font weight, and UI scale are all user-configurable. Settings may restrict behavior but can never bypass OS permissions, safety gates, authorization, or fail-closed requirements.
21. **Optional Amar AI Provider API Gateway** — Amar AI must be capable of acting as an optional AI provider for external agents or applications that the owner explicitly authorizes. The system exposes a documented, versioned API/connector interface and owner-managed access credentials/API keys so an external agent can connect to Amar AI as a provider. This integration is strictly optional: absence, disabled state, unavailable credentials, or provider configuration failure must never fail the Amar AI core, Item 10, or the project build. Provider availability must be capability-scoped, auditable, revocable, rate/permission bounded where configured, and isolated from the internal authoritative intelligence contracts. The architecture must allow future provider protocols/adapters without hard-coding dependence on a specific third-party AI vendor.
22. **Amar AI In-App Update Engine** — the installed app must be able to discover a newer Amar AI release, show version/changelog information, obtain explicit user confirmation, download the verified release APK, and hand installation to the Android system installer. The update engine must verify release metadata and SHA-256 integrity before installation, reject invalid/corrupt/incompatible/downgrade packages by default, preserve app data/settings/memory through stable storage and migrations, and never uninstall the working version first. Automatic update checks may be enabled or disabled by the user, but installation remains confirmation-gated. The release channel must be tied to verified Amar AI builds; production updates require a stable signing identity and secrets must never enter source, logs, or artifacts.

## Required camera safety and privacy invariants

- Camera sharing is always explicit and user-initiated.
- Camera capture must not begin silently or persist after the user stops the camera session.
- The UI must expose a clear active-camera indicator and immediate stop control.
- No covert camera activation, permission bypass, background capture, or security bypass.
- Sensitive visual content must remain subject to existing privacy, permission, audit, and fail-closed rules.
- The agent must distinguish observed visual evidence from inference and uncertainty.
- No fabricated objects, people, text, events, or environmental claims when visibility is insufficient.
- Camera frames/video must not automatically become long-term memory or authoritative knowledge; promotion requires the same validation/provenance rules as other knowledge.

## Required settings safety invariants

- User settings are preferences/policy, not an authorization bypass.
- OS-denied camera, microphone, screen-capture, storage, network, or control permissions always override user preference and result in a safe disabled/blocked state.
- Sensitive actions remain confirmation-gated or blocked according to the safety authority.
- `OPEN_SEARCH` never means unrestricted authentication/security/OS bypass.
- `OFF` must mean unavailable, not merely hidden in the UI.
- `LIMITED` must enforce an actual bounded policy, not a cosmetic label.
- Every persisted settings change must be auditable and deterministically recoverable.
- Presentation settings (backgrounds, colors, fonts, scale) must not alter intelligence, evidence, safety, memory, or execution authority.
- One authoritative settings owner must be used; duplicate settings authorities are prohibited.

## Required provider API safety invariants

- Provider API access is optional and must be fail-open with respect to project availability: its absence or outage cannot break core Amar AI functionality or CI.
- API credentials are owner-managed secrets and must never be committed to source control, logs, evidence artifacts, or client-visible source code.
- Each issued credential must be scoped, revocable, auditable, and independently disableable.
- External agents receive only the capabilities explicitly granted to their credential; no implicit access to internal execution authority, private memory, device control, or sensitive actions.
- Sensitive operations remain subject to the existing authorization/confirmation/safety authorities even when invoked through the provider API.
- Provider API errors/timeouts/auth failures must degrade safely and must not corrupt authoritative memory, knowledge, evidence, or project state.
- API contract/version changes require regression evidence and backward-compatibility policy before activation.
- External-provider connectivity must never become a single point of failure for Amar AI.

## Required update-engine safety invariants

- Update discovery, download, and installation are optional services and must never become a single point of failure for normal Amar AI operation.
- Installation always requires explicit user confirmation and Android system/user authorization; no silent install or permission bypass.
- Release metadata must be complete and validated; missing, malformed, incompatible, or stale metadata fails closed.
- APK integrity must be cryptographically verified before installation; hash mismatch or unreadable package blocks installation.
- The current working installation must not be uninstalled or replaced before the new package is accepted by the system installer.
- Updates must preserve user data/settings/memory through stable storage and explicit migrations; failed updates must not intentionally delete authoritative user state.
- Downgrades are blocked by default.
- Production update artifacts must use the same stable application signing identity; signing secrets must remain outside source control and logs.
- Update checks/downloads/install attempts/results must be auditable without recording credentials or sensitive package secrets.
- A failed network/download/installation attempt must degrade safely and leave the existing installed version usable.
- The update engine must not depend on Gemini, GPT, Claude, or any other third-party AI provider.

## Required invariants

- Fail closed when required evidence, authorization, visibility, or safety state is missing.
- Zero silent critical failures.
- No fabricated visual/video/camera/screen observations.
- No implicit execution authority.
- One authoritative owner per responsibility unless synchronization is explicitly designed.
- Memory deletion and supersession must be traceable and regression-tested.
- Conversation archive and validated knowledge must remain distinguishable.
- Open web mode must never mean security bypass.
- Existing Stage 11 Items 1–9 remain integrated; no deletion, omission, simplification, or scope reduction is permitted.
- The optional provider API is an adapter/service boundary, not a replacement for the Amar AI core and not a required build/runtime dependency.
- The update engine is an adapter/service boundary, not a required network service for core operation.

## Verification plan

The implementation must complete the constitutional closure chain:

**تحقيق → تنفيذ → فحص → اختبار → تأكيد → إثبات → تدقيق نهائي → جاهز → إغلاق**

Required evidence includes focused Item 10 tests for multimodal perception, camera, screen, workspace coordination, reasoning transparency, centralized settings, memory/governance, optional provider API contract/auth/revocation/failure isolation, update-engine manifest/integrity/confirmation/downgrade/data-preservation/failure isolation contracts, relevant full regression, build, architecture/contract regression, memory/research/safety/observability checks, current CI on the exact closure commit, evidence artifact, and independent final audit.

Until every required gate has current evidence, Item 10 remains **IN PROGRESS / final verification pending**.
