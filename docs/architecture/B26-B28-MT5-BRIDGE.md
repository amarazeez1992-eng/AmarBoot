# B26–B29 — MT5 Bridge Architecture

## B26 — MT5 Adapter Boundary

The Android application communicates with a local bridge contract rather than directly embedding broker-specific execution logic.

Flow:
`Android → authenticated bridge → MetaTrader 5 terminal → BOT 1`

The bridge is responsible for connectivity, account/market reads, BOT 1 state mapping and explicit live-execution gating.

## B27 — Local MT5 Bridge Contract

A self-hosted bridge is the first free architecture. No paid VPS or third-party trading service is required by this design.

Read operations:
- health/status
- account snapshot
- market snapshot
- positions
- pending orders
- BOT 1 status

The bridge authenticates every client, rejects malformed requests, requires HTTPS, and defaults to read-only.

## B28 — Execution Safety Boundary

Live execution remains fail-closed until an actual MT5 execution connector is implemented and separately verified.

Required future gates:
1. authenticated bridge session
2. explicitly enabled account
3. explicit live authorization
4. connector capability check
5. request validation
6. idempotency/request tracking
7. audit record

Entering an account password or bridge token alone must never enable live trading.

## B29 — Hardened Read-Only Connection

The bridge now provides:
- constant-time bearer-token comparison
- HTTPS/TLS 1.2+ enforcement
- no-store response headers
- explicit symbol validation and Market Watch selection
- account trading capability visibility without enabling execution
- position/order filtering by symbol and BOT magic number
- BOT 1 status aggregation using magic `20260908` by default
- Android read-only client contract
- deterministic bridge contract tests and CI validation

The Android client does not expose any trade endpoint. The bridge rejects POST execution requests.

## BOT 1 protection

`Grid_Martingale_Basket_v2.mq5` is intentionally unchanged in B26–B29. Integration is built around the existing strategy.

## Next implementation slice

Verify live read-only connection against the user's MT5 terminal, then map BOT 1 controls into a separate command contract. Execution remains locked until the complete command path is tested in DEMO mode.
