# B35 — BOT 1 MT5 EA Integration Contract

Status: IMPLEMENTATION CONTRACT / NOT LIVE AUTHORIZATION

## Authoritative EA
The reviewed source is `Grid_Martingale_Basket_AMAR_v3.mq5` from the approved BOT 1 EA artifact.

## Isolation rule
Every BOT 1 position/order operation must match:
- symbol = current BOT 1 symbol
- magic = `20260908`

Manual positions may be managed by a separate optional trailing/management engine, but they must never become part of BOT 1 basket accounting or BOT 1 close/rebuild operations.

## Required operation boundaries
1. `BOT1_ONLY`: basket profit/loss, basket close, rebuild, pending deletion and BOT1 position counts.
2. `MANUAL_MANAGED`: optional SL/trailing management only, never basket accounting.
3. `ACCOUNT_GLOBAL`: emergency account action only when explicitly authorized by a separate policy; not part of normal BOT1 controls.

## Rebuild safety
A grid rebuild must delete BOT1 pending orders first. It must not call a global close routine as an implicit side effect. If the product requirement is to rebuild while positions remain open, the rebuild operation must preserve positions and replace only the BOT1 pending grid. A separate Global Rebuild command may close BOT1 positions and pending orders, then rebuild.

## Broker validation
Before placing a pending order:
- normalize price to symbol digits;
- validate volume against SYMBOL_VOLUME_MIN/MAX/STEP;
- validate distance against SYMBOL_TRADE_STOPS_LEVEL;
- reject invalid prices/volumes before OrderSend;
- inspect `MqlTradeResult.retcode`, not only the boolean return from `OrderSend`.

## Command verification
The Android command path is:
`UI → command envelope → security validation → bridge → EA → ACK → read-back runtime state → reconciliation → audit`.

No UI button is a runtime state. `RUNNING`, `OFF`, `REBUILDING`, `CLOSING` and `EMERGENCY_LOCK` must be confirmed by runtime acknowledgement/read-back.

## Emergency lock
Emergency lock is fail-closed. While locked, new execution commands are rejected. Clearing the lock requires explicit authorization and a fresh verified runtime state.

## Live gate
B35 is not Live-ready until:
- EA source is committed to the repository;
- MT5 MetaEditor compilation succeeds;
- Demo runtime tests verify isolation and command behavior;
- ACK/read-back/reconciliation is verified;
- CI passes on the corresponding Android/bridge commit.
