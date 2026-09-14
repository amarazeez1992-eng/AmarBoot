# AMAR AI — Stage 10 Final 100% Audit

**STATUS: OPEN — audit gate active**

## Scope

Stage 10 is the final Android application release gate. It verifies security, dependency/license/provenance hygiene, regression coverage, Android release artifacts, architecture consistency, and open-source release hygiene.

The protected MT5 EA baseline is **explicitly outside the Android release artifact scope**. It remains protected, is not approved for live trading, and must not be treated as an Android release dependency or live-execution component.

## Mandatory closure gates

1. Complete security audit.
2. Dependency/license/provenance audit.
3. Complete regression suite.
4. Android release build and artifact verification.
5. Final architecture consistency review.
6. Documentation/open-source release hygiene.
7. Zero unresolved critical/high defects **within the Android release scope**.
8. Successful Stage 10 CI/release verification.

## Baseline audit findings

### Execution safety

The Agent/application execution path remains fail-closed and governed by explicit delegation, risk, security, ACK/read-back/reconciliation and audit boundaries. The AI supervisor is not a direct broker executor.

### Protected MT5 artifact

`mt5/Experts/Grid_Martingale_Basket_v2.mq5` is a protected artifact at version 2.01. Its current defaults and implementation include trading/grid behavior. It is therefore **not approved for live trading** and is **excluded from the Android release artifact**. No Android release component may depend on or invoke this EA. A future MT5/demo/runtime phase requires a separate controlled hardening and verification gate before any execution is enabled.

### Open-source and provenance

The repository contains an MIT license for AMAR-owned source, explicit third-party notices, a security policy, and an open-source provenance/license policy. Public visibility is not treated as reuse permission.

### CI/security automation

CodeQL is configured for Java/Kotlin and JavaScript/TypeScript. Stage 10 additionally verifies the complete Android test/build/release artifact path.

## Current release decision

**NOT RELEASE-CLOSED.** The Android release remains open until all Stage 10 CI gates pass and the final architecture/regression audit confirms zero unresolved critical/high defects inside the Android release scope. The protected MT5 EA remains non-live and outside that release artifact.

## Required evidence

The Stage 10 workflow records focused regression tests, full unit tests, debug and release builds, release APK existence/size checks, dependency graph generation, repository secret-pattern checks, protected execution checks, and diagnostic artifacts.

**Rule:** inspect → implement/fix → re-inspect → focused test → full test → release build → CI → final audit → close only when zero critical/high defects remain within the release scope.