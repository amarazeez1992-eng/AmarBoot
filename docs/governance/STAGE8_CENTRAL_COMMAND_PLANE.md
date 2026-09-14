# AMAR AI — Stage 8 Central Command Plane

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

## Close gate

Stage 8 remains OPEN until focused permission/lifecycle tests, full unit tests, debug build verification, CI success, and an explicit architecture audit are all verified.

The permanent regression guard in `docs/governance/ENGINEERING_REGRESSION_EXCLUSIONS.md` applies to every Stage 8 change.
