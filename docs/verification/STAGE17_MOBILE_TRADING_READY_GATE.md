# AMARBOOT — Stage 17 Mobile Trading Ready Gate

**Status:** GATE CANDIDATE — awaiting exact-commit CI verification and main verification.

## Scope

Stage 17 is the final Android/mobile readiness gate. It does **not** activate broker/live execution and does **not** modify the protected MT5 implementation.

The next integration phase is explicitly deferred until after this gate:

`Laptop → CMG → Bridge → MT5`

## Required gate conditions

1. Stages 1–16 are present on `main`.
2. Application-wide unit/regression coverage and security hardening are green.
3. CodeQL is green.
4. AI Copilot remains governed by explicit tool authority and fail-closed behavior.
5. The Android execution boundary remains non-broker-writing; no Broker Adapter is connected.
6. Demo mode cannot execute trades.
7. Market/account values are never fabricated when trusted runtime data is unavailable.
8. Risk, validation, audit, idempotency, and security boundaries remain active.
9. The protected MT5 file remains outside Android feature work.
10. No Decision Room or MT5 live integration is activated by this gate.

## Stage 16 evidence

Stage 16 security hardening was merged to `main` at commit:

`31570cf89d2d6bfa13ee02bab62464dffd165833`

The exact Stage 16 head was:

`2398d4afc79272c46831a5ed4fdcd1bcae86147f`

Its required verification workflows completed successfully:

- AMAR AI Stage One Verification
- AMAR AI Stage Two Verification
- CodeQL

## Execution boundary evidence

`AmarExecutionBoundary` explicitly remains non-broker-writing. DEMO mode is fail-closed, and the boundary reports that no Broker Adapter is connected.

## MT5 boundary

The protected MT5 EA remains out of scope for Stage 17. MT5 integration is intentionally postponed until the dedicated final integration phase.

## Closure rule

Stage 17 may be marked **CLOSED** only after:

- this gate commit passes Stage One, Stage Two, and CodeQL;
- the resulting commit is merged to `main`;
- post-merge `main` is verified;
- the protected MT5 file is verified unchanged;
- no conflicting or duplicate readiness gate is found.
