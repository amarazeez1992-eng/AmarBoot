# AMAR BOT1 B47 — Security and Execution Audit

## Scope

B47 hardens the already-established BOT1 remote execution path without changing the authoritative trading strategy.

## Verified controls

1. **Transport authentication**
   - Bearer token is compared in constant time.
   - JSON content type is mandatory.
   - Request body size is bounded.

2. **Command authentication**
   - HMAC-SHA256 remains mandatory.
   - Android BOT1 commands additionally carry an Android Keystore-backed RSA-2048 device signature.
   - `device_id` is bound to the DER public-key digest.

3. **Replay protection**
   - TTL and clock-skew checks are enforced.
   - Nonce and idempotency are separate controls.
   - Per-device command sequence is enforced at the bridge.
   - MT5 now persists the accepted per-device sequence in terminal Global Variables, so a terminal restart does not reset the sequence floor.

4. **Execution scope**
   - Account login and BOT magic are checked.
   - Source and target symbols are allow-listed.
   - Target symbol patterns are explicit configuration, not implicit wildcard access.

5. **Fail-closed lifecycle**
   - BOT1 live control remains locked unless `AMAR_LIVE_EXECUTION=1`.
   - MT5 common-files configuration is required before lifecycle commands are queued.
   - `202 QUEUED` is not treated as execution success.
   - Terminal ACK and fresh runtime state remain the verification boundary.

6. **Market safety**
   - MT5 target readiness requires a synchronized symbol and a fresh valid tick.
   - Remote START/REBUILD/settings changes remain subject to the existing market-health gate.

## Execution-path invariant

`Android UI -> Execution Controller -> Signed BOT1 command -> Bridge authentication/scope/replay gates -> authenticated FILE_COMMON queue -> MT5 receiver -> target-symbol/market readiness -> strategy execution -> terminal ACK/state -> Android verification`

No direct UI-to-MT5 execution path is introduced.

## Residual hardening items

- Android command sequence persistence across application process/device restart should be wired through `AmarDeviceSecurity` with application context before production live enablement.
- Bridge replay ledgers are process-local; terminal-side persistent sequence enforcement prevents stale/replayed commands from executing after bridge restart, but a durable bridge ledger is still a useful future defense-in-depth layer.
- The MT5 wrapper remains the required live execution boundary; laptop/terminal testing is required before claiming live broker execution.

## Verification policy

A stage is not marked complete until the relevant GitHub Actions run reports `success`. A queued HTTP response, UI state, or local assumption is not sufficient evidence of execution.
