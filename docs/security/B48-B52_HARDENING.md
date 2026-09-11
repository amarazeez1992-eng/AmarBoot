# AMAR BOT1 B48-B52 Security Hardening

## B48 — Android device identity persistence

- BOT1 commands require an Android Keystore RSA-2048 device identity.
- The private key remains inside `AndroidKeyStore`.
- Device sequence state is persisted through `SharedPreferences` in the production construction path.
- JVM/test construction remains possible through the injectable in-memory sequence store.
- `AmarMt5CommandClientFactory` is the preferred production construction path because it cannot silently omit device binding.

## B49 — Durable bridge replay protection

- The bridge keeps nonce, idempotency and device-sequence replay state.
- When `AMAR_COMMAND_REPLAY_LEDGER_PATH` is configured, the state is durable across bridge process restarts.
- Writes use a temporary file, restrictive permissions, flush/fsync, atomic replacement and directory fsync.
- Ledger corruption or persistence failure fails closed rather than accepting an untrusted command state.
- Tests cover restart persistence and corrupt-ledger rejection.

## B50 — End-to-end execution verification

A mobile command is not reported as executed merely because the bridge accepted it. Verification requires:

1. authenticated submission;
2. terminal ACK with `VERIFIED` status;
3. fresh BOT1 runtime state;
4. matching request ID and verified command status;
5. expected target symbol when supplied;
6. command-specific postcondition, such as RUNNING/STOPPED state, zero positions/orders for CLOSE_ALL, or matching settings.

A timeout or incomplete read-back is reported as unverified/failure. The client does not automatically repeat destructive commands.

## B51 — Runtime identity and reconciliation

The runtime identity now includes:

- bot ID;
- magic number;
- strategy ID;
- strategy version;
- target symbol;
- heartbeat freshness;
- market readiness;
- complete runtime configuration.

Identity mismatches fail closed. `MATCHED` cannot be manufactured by the UI.

## B52 — Health monitoring

The Android health monitor classifies remote state as healthy, stale, unavailable, identity-mismatched, market-unready, or execution-error. It is observational only and has no autonomous trading authority.

## Production safety requirement

The durable replay ledger should be configured outside the repository with `AMAR_COMMAND_REPLAY_LEDGER_PATH` pointing to a protected filesystem location owned by the bridge service. The live execution flag remains explicitly opt-in.

## Strategy integrity

These batches do not modify the approved trading strategy's grid, martingale, basket, tracking, or risk semantics. Changes are limited to transport security, identity, verification, reconciliation, and monitoring boundaries.
