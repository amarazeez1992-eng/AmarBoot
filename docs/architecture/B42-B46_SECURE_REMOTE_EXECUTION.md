# AMAR BOT1 B42-B46 — Secure Remote Execution

## B42 — Device-bound command security

- Android generates an RSA-2048 signing key in `AndroidKeyStore`.
- The private key never enters the command payload or bridge.
- `device_id` is SHA-256 of the DER SubjectPublicKeyInfo.
- Each BOT1 command carries `device_id`, `sequence`, `device_public_key`, and `device_signature`.
- The bridge verifies the public-key signature before accepting the command.
- The existing HMAC signature remains as a second independent authentication layer.

## B43 — Monotonic sequencing

- A per-device monotonic command sequence prevents reordering within the bridge session.
- The MT5 receiver also rejects lower/equal sequences for the same device during its process lifetime.
- Nonce, TTL, idempotency, and sequence checks are separate controls.

## B44 — Fail-closed verification

A command is not considered verified merely because the bridge returned `202 QUEUED`.
Terminal `VERIFIED` ACK plus fresh runtime state are required for a verified lifecycle result.

## B45 — Desired/Actual reconciliation

`AmarBot1VerifiedRuntime` maps terminal read-back into the existing `AmarBot1ActualState` and delegates comparison to the existing `AmarBot1ReconciliationEngine`. It does not create a second runtime state machine.

## B46 — Safety conditions

Verification is blocked when:

- terminal state is unavailable or stale;
- market readiness is false;
- identity/magic/version/configuration drift is detected;
- requested target symbol differs from terminal target symbol;
- the command does not reach terminal verification.

Live execution remains separately locked by `AMAR_LIVE_EXECUTION=1` and requires the configured MT5 common-files path.
