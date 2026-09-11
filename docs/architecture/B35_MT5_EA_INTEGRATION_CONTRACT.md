# B35 — BOT 1 MT5 EA Integration Contract

Status: SOURCE COMMITTED / NOT LIVE AUTHORIZATION

## Authoritative EA
The current committed BOT 1 EA baseline is:
`mt5/Experts/Grid_Martingale_Basket_v2.mq5`

It is a hardened B35 baseline derived from the supplied `Grid_Martingale_Basket_v2.mq5` source. The EA is version `2.10` in the repository.

## Safety baseline
- All numeric inputs default to `0`.
- BUY and SELL default to `false`.
- Invalid/incomplete configuration forces BOT 1 to `OFF`.
- `BOT OFF` stops new BOT1 activity and preserves existing BOT1 positions and pending orders.
- `REBUILD` removes BOT1 pending orders only and rebuilds the pending grid; it does not close BOT1 positions.
- `CLOSE ALL` is BOT1/symbol scoped and closes BOT1 positions plus removes BOT1 pending orders.
- Basket accounting, trailing, counts and order management are BOT1/symbol scoped.
- Manual trades and other EAs are excluded by magic + symbol scope.

## Identity and isolation
BOT 1 identity:
- `BOT_ID = BOT_1`
- `MAGIC = 20260908`
- current chart symbol is the BOT1 symbol scope

Every BOT1 position/order operation must match both the current symbol and magic `20260908`.

Manual positions must never become part of BOT1 basket accounting or BOT1 close/rebuild operations.

## Required operation boundaries
1. `BOT1_ONLY`: basket profit/loss, basket close, rebuild, pending deletion and BOT1 position counts.
2. `MANUAL_MANAGED`: optional future SL/trailing management only; never BOT1 basket accounting.
3. `ACCOUNT_GLOBAL`: emergency account action only when explicitly authorized by a separate policy; not part of normal BOT1 controls.

## Broker validation
Before live authorization, the EA must additionally verify in MetaEditor/MT5 runtime:
- normalized prices against symbol digits;
- volume against `SYMBOL_VOLUME_MIN/MAX/STEP`;
- pending distance against `SYMBOL_TRADE_STOPS_LEVEL` and broker constraints;
- valid order type/filling mode for the symbol;
- `MqlTradeResult.retcode`, not only the boolean return from `OrderSend`;
- partial-close/partial-fill behavior;
- rejected-order behavior without state corruption.

The committed baseline already performs symbol volume normalization and retcode checks, but these broker-specific runtime checks remain part of the B35 live gate.

## Command verification
The required Android command path is:
`UI → command envelope → security validation → bridge → EA → ACK → read-back runtime state → reconciliation → audit`.

The current committed EA source is the execution baseline; the terminal-side command receiver remains a separate integration module and must not be treated as a completed end-to-end command channel until ACK/read-back is demonstrated.

No UI button is a runtime state. `RUNNING`, `OFF`, `REBUILDING`, `CLOSING` and `EMERGENCY_LOCK` must be confirmed by runtime acknowledgement/read-back.

## Emergency lock
Emergency lock is fail-closed. While locked, new execution commands are rejected. Clearing the lock requires explicit authorization and a fresh verified runtime state.

## Live gate
B35 is **not Live-ready** until all of the following are verified:
1. EA source is committed. **DONE**.
2. MetaEditor compilation succeeds. **PENDING**.
3. MT5 Demo runtime verifies BOT1/manual/other-EA isolation. **PENDING**.
4. CLOSE ALL, REBUILD and BOT OFF semantics are verified in a real demo terminal. **PENDING**.
5. Command ACK/read-back/reconciliation is verified end-to-end. **PENDING**.
6. Failure/retcode/idempotency behavior is verified. **PENDING**.
7. Corresponding Android/bridge CI passes. The latest Android/bridge security CI had passed before the EA source commit; a new CI run is required for this commit.

No production/live-trading authorization is implied by source commitment or CI success.
