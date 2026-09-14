# AMAR AI — Stage 8 Central Command Plane

**STATUS: CLOSED — verified 2026-09-14**

Stage 8 makes Amar AI the application command authority. Subordinate control-room modules consume Agent decisions and route them; they do not own policy, create implicit permissions, or obtain unrestricted device authority.

## Implemented boundary

- Central command-plane routing with explicit routes.
- Agent decisions are immutable, policy-approved, versioned and time-bounded.
- Principal permissions are explicit, versioned and time-bounded.
- Revocation immediately blocks subsequent routing and device authorization.
- Emergency lock overrides routing and device permissions.
- Device capabilities are allow-listed and separate from execution capabilities.
- Execution cannot bypass the Stage 7 execution-governance boundary through the device route.
- Decision IDs are idempotent; duplicate submissions return the original receipt.
- The command plane does not execute broker or device operations itself.

## Required lifecycle

Agent decision → command-plane validation → bounded route → subordinate gateway → audited outcome.

A subordinate module cannot turn a rejected, stale, revoked or emergency-locked decision into an executable command.

## Non-negotiable rules

1. Amar AI remains the application policy authority.
2. No implicit permission is created from a route, UI state or device availability.
3. Revocation is fail-closed.
4. Expiry is fail-closed.
5. Emergency lock overrides otherwise valid permissions.
6. Device access is least-privilege and capability-specific.
7. Execution authority remains behind Stage 7 governance and its explicit delegation/risk/security gates.
8. Duplicate decision IDs cannot create a second routed command.
9. This module is a routing/policy boundary, not a broker/device executor.

## Verification and audit evidence

The Stage 8 close gate has been satisfied on commit `db55367b2bdf735eb42b2eeffd584433a5288039`.

- Focused Stage 8 tests: **PASS** — workflow run `34862221490`, job `104037088671`.
- Full unit-test suite: **PASS**.
- Debug build verification: **PASS**.
- CI workflow: **PASS**.
- Regression guard updated with the idempotency-fixture failure pattern before closure.
- Architecture audit: command authority is centralized in `AmarCentralCommandPlane`; subordinate access is capability-bounded; execution remains behind Stage 7 governance; device access cannot be inferred from execution permission; emergency lock and revocation are fail-closed; duplicate decision IDs are immutable/idempotent.

The previous CI failure was inspected from its actual log, the semantic test fixture was corrected, and the corrected commit was re-verified by the complete Stage 8 CI gate.

## Close gate

**CLOSED.** Focused permission/lifecycle tests, full unit tests, debug build verification, CI success, regression exclusions, and explicit architecture audit are all verified.

The permanent regression guard in `docs/governance/ENGINEERING_REGRESSION_EXCLUSIONS.md` remains mandatory for every future Stage 8 or later change.
