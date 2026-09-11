# B58-B61 — Mobile Completion Gate

## Completed
- B58: one Android lifecycle facade for BOT1 commands.
- B59: lifecycle operations use `AmarBot1CommandVerifier`; queue acceptance is never treated as execution success.
- B60: health is evaluated before a command is considered safe.
- B61: mobile execution preflight checks runtime health, bot identity, magic and selected/target symbol consistency.
- Side-specific close commands are now part of the authenticated BOT1 protocol: `CLOSE_BUY` and `CLOSE_SELL`.
- MT5 performs side-specific close execution only inside BOT1 magic + target-symbol scope and reports the remaining buy/sell position counts for postcondition verification.
- Android verification accepts a side-close command only when the corresponding terminal-side position count reaches zero.
- Production runtime wiring uses the factory path that preserves Android Keystore device identity and persistent sequence storage.

## Security invariants
1. Android does not contain bridge signing secrets by default.
2. BOT1 commands require Android Keystore device identity and monotonic sequence.
3. Bridge live mode remains fail-closed unless explicitly enabled.
4. Live mode requires a durable absolute replay ledger.
5. MT5 execution is not reported successful until ACK + runtime state postcondition verification.
6. Symbol selection is transport context only until MT5 confirms the target symbol.
7. The global Android timeframe selector remains UI/context state; it does not silently alter the authoritative MT5 strategy timeframe.
8. The original `Grid_Martingale_Basket_v2.mq5` strategy remains untouched.
9. `CLOSE_BUY` and `CLOSE_SELL` do not delete pending orders; they close only matching open positions within BOT1 target-symbol scope.

## Laptop-only final gate
The remaining gate requires a real Windows/MT5 environment and cannot be truthfully completed from Android/GitHub alone:
- install/verify the RemoteTarget EA in MT5;
- configure MT5 Common Files;
- configure the HTTPS bridge certificate/key and environment secrets;
- configure allowed account/magic/source/target symbols;
- enable live execution only for a controlled test account;
- connect the Android app;
- execute START/STOP/REBUILD/CLOSE_ALL/CLOSE_BUY/CLOSE_SELL with real ACK/state verification;
- verify XAUUSD/BTCUSD target switching against the actual broker symbol names;
- verify no collateral symbols/orders are touched;
- verify failure/timeout/market-stale behavior remains fail-closed.

This gate is the final integration test, not a substitute for CI.
