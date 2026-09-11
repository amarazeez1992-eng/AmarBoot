# B62-B64 — Final Mobile-to-Laptop Gate

## Completed on mobile/GitHub

- Interface B lifecycle controls now use the verified BOT1 command gateway.
- Apply + Verify sends `UPDATE_SETTINGS` only through the authenticated/device-bound command path.
- Quick controls send `CLOSE_BUY`, `CLOSE_SELL`, `CLOSE_ALL`, and `REBUILD` through the verified lifecycle path.
- A command is shown as successful only after ACK + fresh MT5 runtime state satisfies the command postcondition.
- Read-only account discovery is used to obtain the MT5 account login; it does not authorize execution.
- Runtime absence, account unavailability, stale state, or verification failure remains fail-closed.
- Strategy vault remains local configuration storage and does not authorize live execution.
- Zero/OFF defaults remain the mobile UI defaults.

## Intentionally not fabricated

- No bridge URL, bearer token, signing secret, account number, broker symbol suffix, or live authorization is embedded in the repository.
- No MQL5 compile result is claimed from CI because GitHub Actions does not compile the MT5 Expert Advisor.
- Android timeframe selection remains a UI/context selection until the MT5-side timeframe contract is implemented and verified.

## Laptop is now the final external validation stage

1. Open MetaEditor and compile the RemoteTarget EA and included AMAR headers.
2. Resolve any broker-specific symbol suffix/prefix using MT5 symbol discovery.
3. Configure the bridge HTTPS certificate/key, bearer token, signing secret, durable replay ledger, account scope, magic scope, and allowed target symbols.
4. Start MT5 with the EA on the controlled test account.
5. Connect the Android app to the bridge using the real runtime configuration.
6. Verify read-only health/account/state first.
7. Verify `START`, `STOP`, `REBUILD`, `CLOSE_BUY`, `CLOSE_SELL`, `CLOSE_ALL`, and `UPDATE_SETTINGS` one by one with ACK + state verification.
8. Verify XAUUSD and BTCUSD target switching, including broker suffix variants, while confirming no unrelated-symbol/account collateral is touched.
9. Verify fail-closed behavior for stale market data, stale runtime heartbeat, wrong symbol, wrong magic, expired command, replayed sequence, and bridge interruption.
10. Only after all checks pass, enable controlled live execution and place the first intended trade.

The laptop is therefore not an intermediate development dependency. It is the final compile + terminal + broker execution validation gate.