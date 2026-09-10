# AMAR B31-B33 — Secure Execution and MT5 Runtime

## B31 — Secure Command Channel
- Immutable command envelope.
- Request ID, idempotency key and nonce.
- Issued/expiry timestamps with clock-skew protection.
- HMAC-SHA256 canonical signing.
- Bearer authentication plus independent command signature.
- Account, BOT magic and symbol scope validation.
- Replay and duplicate-command rejection.
- Payload-size and malformed-request rejection.
- Fail-closed behavior.

## B32 — MT5 Runtime Boundary
- Real MT5 candle provider with concurrent atomic cache.
- Explicit asynchronous refresh; no fabricated market data.
- Secure `/commands` bridge entrypoint exists, but `AMAR_LIVE_EXECUTION` defaults to disabled.
- Live bridge startup requires token, TLS certificate/key, signing secret and an explicit symbol allow-list.
- Broker volume step/min/max are validated before order submission.

## B33 — BOT 1 Integration Boundary
- Commands are scoped to the configured BOT magic number and symbol.
- Market commands are tagged with the BOT magic number.
- BOT 1 strategy logic remains separate from transport and execution.
- Account-wide operations are not implicitly authorized by a BOT 1 command.

## Safety invariant
Entering account credentials, installing the bridge, or creating a command client does not enable live execution. Live execution requires explicit server configuration and all command validation gates to pass.

## Verification
Android CI verifies Kotlin unit tests, bridge Python syntax, and bridge command-channel tests before APK assembly.
