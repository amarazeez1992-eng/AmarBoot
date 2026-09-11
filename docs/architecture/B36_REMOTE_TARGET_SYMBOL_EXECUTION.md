# B36 — Remote Target-Symbol Execution

Status: **APPROVED / IMPLEMENTED IN REPOSITORY**

## Decision
The selected trading symbol is a first-class execution-context field alongside bot, strategy and timeframe.

For BOT 1, the approved scenario is:

`Phone: Gold selected -> secure bridge command -> MT5 BOT 1 target symbol XAUUSD -> verify symbol/market -> rebuild -> execute BOT 1 on XAUUSD`

The MT5 chart may remain on another symbol (for example BTCUSD). The BOT 1 remote-target EA therefore must not depend on chart-symbol ticks for management of the selected target.

## Target symbol
Supported UI choices:
- XAUUSD — Gold
- BTCUSD — Bitcoin
- Custom broker symbol (for suffix/prefix variants such as XAUUSDm or BTCUSD.a)

The broker-visible symbol is authoritative for MT5 execution.

## Safety gates
Before remote execution/rebuild:
1. Target symbol must be explicit and non-empty.
2. MT5 must be able to select the target symbol.
3. `SymbolInfoTick()` must return a valid bid/ask quote.
4. Bid/ask must be finite, positive, and ordered correctly.
5. BOT 1 operations are isolated by `Magic` and target symbol.
6. A target-symbol switch is fail-closed if the requested symbol is unavailable.
7. UI selection alone is never treated as proof of execution.
8. BOT OFF stops BOT 1 engines only; it does not close positions or delete pending orders.
9. CLOSE ALL is the explicit destructive action: close BOT 1 positions and delete BOT 1 pending orders for the target context.

## MT5 implementation
`mt5/Experts/Grid_Martingale_Basket_v2.mq5` remains the authoritative strategy and now has ownership isolation plus corrected BOT OFF semantics.

`mt5/Experts/Grid_Martingale_Basket_v2_RemoteTarget.mq5` is the remote-target execution wrapper. It keeps the authoritative strategy source intact and redirects its existing `Symbol()` execution context to a verified target symbol. When the chart symbol differs from the target, a timer cycle uses target-symbol quotes so execution does not depend on unrelated chart ticks.

`mt5/Include/AMAR/AmarBot1CommandReceiver.mqh` accepts the target symbol as part of the command context and rejects malformed target-symbol settings.

## Bridge implementation
`bridge/amar_bot1_control.py` and `bridge/amar_bot1_file_queue.py` now serialize and validate `target_symbol` explicitly. Existing command vocabulary remains unchanged.

## Strategy preservation
No new indicator, filter, SMC/ICT logic, or entry rule is introduced. Grid, martingale, basket and trailing behavior remain the existing BOT 1 strategy; B36 only routes the execution context and adds ownership/safety isolation.

## Verification boundary
Repository tests can verify the command contract and Android build. Actual MT5 live execution remains unverified until the wrapper is compiled in MetaEditor and tested on a connected MT5 account with the broker's exact symbol names.
