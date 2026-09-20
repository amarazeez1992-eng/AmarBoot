# ERR-FRESHNESS-001 — Point 4 Freshness Integration / CI Root Cause

Status: CI VERIFIED — RUNTIME PHONE VERIFICATION PENDING

## Scope
Stage 11 → Item 3 → Point 4 — Freshness Engine.

## Confirmed root cause
Commit `de3a0bf7ad60cc7379321dadde086445f40b926d` introduced only a regression test, but the branch already contained a source-corruption defect inherited from its parent.

In `AmarAgentOrchestrator.kt`, the general-factual consensus change had been written with literal escaped newline sequences (`\\n`) inside a `//` comment instead of real line breaks. Kotlin therefore treated the following verifier declaration as part of the comment. Compilation then failed with unresolved `decisionVerification` references.

This was source-generation/editing corruption, not a Freshness Engine algorithm failure and not a reason to change STALE semantics.

## Root correction
Commit `8d3b8241bca42ac63d1e7a1dc5a6560d917313d4` restored the executable declarations. Commit `0722c834cbdb62ad47302e422e4a43384a6cff95` then corrected orchestrator telemetry to retain the complete run result before reading research/source-verification metadata.

No evidence/freshness policy was changed.

## Verified gates on 2026-09-20
For commit `0722c834cbdb62ad47302e422e4a43384a6cff95`:
- Stage 11: PASS
- Stage 8: PASS
- Stage 2: PASS
- Stage 10 audit: PASS
- Final APK Closure: PASS
- Build APK: PASS
- CodeQL: PASS
- Stage 1: PASS
- Debug/release builds and full unit tests: PASS
- APK verification/installability checks: PASS

## Guardrails
- STALE behavior remains unchanged: verified with reduced freshness score.
- FUTURE evidence remains blocked.
- No Point 5 implementation was introduced.
- No Stage 12 work was introduced.
- No architecture simplification or deletion was performed.

## Remaining closure gate
Point 4 is **not constitutionally closed yet** because phone/runtime behavior has not been directly verified in this environment.

Required runtime checks:
1. Fresh evidence is accepted with expected freshness metadata.
2. Stale evidence keeps reduced freshness score without changing current semantics.
3. Future evidence is fail-closed.
4. No duplicate final response is emitted.
5. Progress/status and elapsed-time telemetry are visible and truthful.
6. No fabricated source counts or answers occur.

After these runtime checks, a separate Point 4 closure record may be created. Point 5 remains blocked until Point 4 is closed.
