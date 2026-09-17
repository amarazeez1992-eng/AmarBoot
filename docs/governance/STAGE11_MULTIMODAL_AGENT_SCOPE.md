# AMAR AI — Stage 11 Multimodal Agent Scope

**Status: APPROVED SCOPE — NOT YET IMPLEMENTED**

This document defines the approved scope for Stage 11 Item 10. Capabilities are executed and closed one at a time; approval of the scope is not evidence of implementation.

## 1. Multimodal input

- Camera/image capture and image attachments.
- Screenshots and visual evidence.
- Video upload and bounded video/frame analysis.
- Voice input and voice output for two-way conversation.
- TXT, PDF and supported document ingestion.
- APK ingestion for safe static analysis.
- Source-code, configuration, logs and stack-trace ingestion.

## 2. Screen understanding and guided app assistance

The user may explicitly share the screen and ask Amar to inspect a visible application state, such as a settings problem in WhatsApp or Instagram, then explain where to navigate and what to change. Screen sharing must be permission-gated, visibly active, revocable, session-scoped, and fail closed when permission is absent.

The Agent must not silently take control of unrelated applications. Any future action capability requires its own explicit permission and governance gate.

## 3. File-analysis engines

The Agent workspace must provide controlled handoff and analysis for:

- APK
- TXT
- PDF
- source-code files and archives
- logs and stack traces
- images/screenshots/charts
- supported video files
- supported documents

Untrusted binaries are analyzed statically or inside an isolated sandbox only; arbitrary execution is prohibited by default.

## 4. Central engine orchestration

A central Agent coordinator connects available engines into a governed workforce. Depending on the task, it may select a single engine or a bounded combination of engines for:

- trading and market analysis
- research/search
- code analysis
- document/PDF analysis
- image/video/vision analysis
- speech/voice
- verification and evidence checking
- memory/context operations

The workforce is coordinated, not uncontrolled. Each engine receives an explicit task scope, resource budget, data scope, and cancellation boundary. No engine may bypass central policy, security, provenance, risk, or execution governance.

## 5. Research depth modes

The Agent exposes explicit, auditable research modes:

1. **Quick Search** — fast, limited source/evidence budget.
2. **Standard Search** — normal multi-source research.
3. **Expert Search** — deeper source comparison and verification.
4. **Deep Research** — maximum configured research depth, broader evidence collection, cross-checking and synthesis.

These are depth/resource modes, not accuracy guarantees. The Agent must record source scope, evidence used, limitations, and confidence.

## 6. Unified session

Text, voice, image, video, screen and files share one controlled task context when the user chooses to combine them. Session isolation prevents content from one unrelated task from silently contaminating another task.

## 7. Fallback and resilience

If an AI, vision, voice, OCR, search, code-analysis or other engine is unavailable, the coordinator must use an approved compatible fallback where available or clearly report the limitation. Silent failure is prohibited.

## 8. Security and privacy

Camera, microphone, screen, file and external-app viewing capabilities require explicit permission, visible active-state indication, revocation, safe shutdown, and configured retention boundaries.

## 9. Trading integration

Trading requests may use the same central workforce/orchestration layer for analysis, research, vision, code or verification, while all financial execution remains behind the existing explicit delegation, risk, security, ACK, read-back, reconciliation and audit governance. Analysis capability does not grant execution authority.

## 10. Completion rule

Each capability is implemented, tested, evidenced, independently re-verified, and closed individually before the next capability is started. The final Stage 11 closure requires the complete evidence set for all approved capabilities.
