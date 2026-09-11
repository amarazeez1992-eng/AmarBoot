# B53-B57 Execution Hardening Plan

## B53 — Live replay safety gate
- Live bridge startup now fails closed unless `AMAR_COMMAND_REPLAY_LEDGER_PATH` is configured.
- The live ledger path must be absolute.
- Replay persistence remains atomic (`fsync` + replace) and ledger corruption is fail-closed.
- CI covers missing/relative/valid live-ledger configuration behavior.

## B54 — Verified command wiring boundary
- Added `AmarBot1CommandRuntimeRegistry` as the explicit Android production wiring point.
- The registry constructs `AmarMt5CommandClient` through `AmarMt5CommandClientFactory`, preserving Android Keystore device identity and persistent sequencing.
- It exposes `AmarBot1CommandVerifier`, so UI integrations can report execution success only after terminal ACK + runtime postcondition verification.
- The registry is empty by default; no endpoint, token, signing secret, account, or magic is embedded or inferred.
- Actual UI activation remains a separate integration step because real bridge credentials/configuration are environment-specific.

## B55 — Runtime monitoring
- `AmarBot1HealthMonitor` remains observational and fail-closed.
- A stale/unavailable/identity-mismatched/market-unready runtime is never treated as execution-safe.
- No autonomous trading action is attached to the monitor.

## B56 — Symbol/timeframe consistency
- Symbol selection is already propagated through the BOT1 remote target-symbol contract and MT5 wrapper.
- Target-symbol execution remains fail-closed on unavailable/unsynchronized/stale market data.
- The global Android timeframe selector is currently a UI/context selector and candle countdown; it is **not** claimed to alter MT5 strategy timeframe until a verified MT5-side timeframe contract exists.
- This prevents a UI timeframe choice from silently changing or falsely representing trading semantics.

## B57 — Final execution validation
Before live activation on the laptop/MT5 environment, validate the complete chain:

`Android UI → verified command client → HTTPS bridge → HMAC + device signature + sequence → durable replay ledger → MT5 common-files queue → EA receiver → target-symbol market gate → command execution → ACK → state read-back → postcondition verification → UI status`

Required live checks:
1. Correct account and BOT1 magic scope.
2. Correct target symbol, including broker suffix variants where explicitly allow-listed.
3. Fresh market tick and synchronized symbol.
4. Device sequence survives Android process restart.
5. Bridge replay state survives bridge restart.
6. Duplicate/out-of-order commands are rejected.
7. STOP does not close positions or delete pending orders.
8. CLOSE_ALL closes positions and deletes pending orders for BOT1 scope.
9. REBUILD performs the documented destructive rebuild semantics only after verification gates.
10. UI reports success only from verified terminal state, never from queue acceptance.

## Integrity rule
`Grid_Martingale_Basket_v2.mq5` remains the authoritative strategy and is not modified by this hardening path. The remote-target wrapper is infrastructure around it.
