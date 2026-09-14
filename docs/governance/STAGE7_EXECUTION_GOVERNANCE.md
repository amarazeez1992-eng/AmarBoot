# AMAR AI — Stage 7 Execution Governance

Stage 7 establishes a fail-closed governance boundary for financial execution. It does not grant unrestricted broker or device authority.

## Implemented controls

- Central capability model.
- Explicit time-bounded user delegation and revocation.
- Execution proposal gates for evidence, security and risk.
- Idempotency by immutable proposal key.
- Explicit lifecycle: APPROVED → ACKNOWLEDGED → EXECUTED → VERIFIED.
- Read-back reconciliation with mismatch detection.
- Emergency lock that interrupts pending execution states and blocks new authorization.
- No broker execution is claimed by the governance module; the gateway remains a separate boundary.
- Focused failure-injection tests plus full unit/build verification are required by CI.

## Non-negotiable safety rules

1. Missing delegation never becomes implicit permission.
2. Expired/revoked delegation is rejected.
3. Missing evidence or failed security blocks execution.
4. Risk above the configured threshold blocks execution.
5. Duplicate idempotency keys return the original receipt rather than creating a second authorization.
6. Execution cannot skip ACK or verification.
7. Emergency lock fails closed.
8. `EXECUTED` is not final proof; `VERIFIED` is required.
9. This stage must not be marked CLOSED until focused tests, full unit tests, build verification and failure-injection evidence pass in CI.

## Regression gate

All rules in `docs/governance/ENGINEERING_REGRESSION_EXCLUSIONS.md` apply before and after every Stage 7 change.
