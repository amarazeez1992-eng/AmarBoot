# B37 — Remote BOT 1 execution batch

## Approved execution chain

`Phone → Bot/Strategy/Timeframe/Symbol → HTTPS bridge → HMAC + TTL + replay gate → MT5 Common/Files queue → terminal-side expiry/scope gate → target-symbol verification → BOT 1 rebuild/execution → ACK`

### Batch 1: Remote target transport
- Added a broker-neutral BOT 1 lifecycle envelope.
- Commands carry the selected execution symbol explicitly.
- Supported lifecycle commands: START, STOP, REBUILD, CLOSE_ALL, BUY enable, SELL enable, UPDATE_SETTINGS.
- Existing Grid/Martingale/Basket semantics remain the authoritative strategy.

### Batch 2: Terminal fail-closed protection
- MT5 receiver checks request identity and expiration again.
- Target symbol must pass `SymbolSelect()` and `SymbolInfoTick()` validation before execution.
- Timer-based management allows the EA to manage XAUUSD while attached to a different chart symbol. MQL5 documents that `OnTick` is generated for the chart symbol, while timers are independent EA events. citeturn0search9turn0search2
- Shared command/ACK files use `FILE_COMMON`, matching MT5's documented terminal-common sandbox. citeturn1search0

### Batch 3: Bridge security
- BOT 1 commands require HTTPS bearer authentication, HMAC signature, account scope, BOT magic scope, allow-listed symbol, TTL and replay/idempotency protection.
- Live BOT 1 control remains disabled unless the bridge is explicitly configured with `AMAR_LIVE_EXECUTION=1`.
- Queueing is not reported as execution; the bridge returns `accepted=true, executed=false` until the terminal ACK path confirms application.

### Batch 4: Android control contract
- Android now has a dedicated BOT 1 remote-control protocol instead of overloading market-order semantics.
- The selected symbol is part of the signed execution envelope.
- Settings are validated before transmission.

### Batch 5: Verification
- Added Android protocol tests.
- Added Python bridge validation tests for valid commands, wrong scope and expired commands.
- CI must remain the release gate; MT5 live execution is only considered verified after compilation and a real terminal/account test.

## Safety decision

Changing the phone symbol does **not** silently retarget an existing live basket. A target-symbol change is treated as an execution-context change: the BOT 1 wrapper closes/deletes only its own managed exposure for the old target before switching, then rebuilds only when explicitly commanded/allowed.

This is intentional protection against accidentally rebuilding BTCUSD when the phone is set to XAUUSD.
