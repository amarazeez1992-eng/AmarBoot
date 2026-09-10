# B26–B28 — MT5 Bridge Architecture

## B26 — MT5 Adapter Boundary

The Android application communicates with a local bridge contract rather than directly embedding broker-specific execution logic.

Flow:
`Android → authenticated bridge → MetaTrader 5 terminal → BOT 1`

The bridge is responsible for connectivity, account/market reads, command routing, request IDs and explicit live-execution gating.

## B27 — Local MT5 Bridge Contract

A self-hosted bridge is the first free architecture. No paid VPS or third-party trading service is required by this design.

Required operations:
- health/status
- account snapshot
- market snapshot/candles
- positions
- pending orders
- BOT 1 status
- control commands

The bridge must authenticate every client, reject malformed requests, use request IDs for idempotency, and default to read-only.

## B28 — Execution Safety Boundary

Live execution remains fail-closed until an actual MT5 connector is implemented and verified.

Required gates:
1. authenticated bridge session
2. explicitly enabled account
3. explicit live authorization
4. connector capability check
5. request validation
6. idempotency/request tracking
7. audit record

Entering an account password alone must never enable live trading.

## BOT 1 protection

`Grid_Martingale_Basket_v2.mq5` is intentionally unchanged in B26–B28. These stages build the integration boundary around BOT 1. Strategy review/modification is a later, explicitly separate task.

## Next implementation slice

Implement the local bridge protocol and a read-only MT5 connector first. Only after connection, account data, market data and BOT 1 state are verified should command execution be enabled.
