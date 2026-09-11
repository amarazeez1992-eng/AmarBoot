# B62-B64 — Final Mobile-to-Laptop Gate

## Completed on mobile/GitHub

- Bot Lab now exposes **Interface 1 as the only active interface**; the retired Interface 2 prototype is no longer part of the active application surface.
- BOT1 lifecycle controls remain routed through the verified command gateway and facade.
- The lifecycle/destructive set includes `START`, `STOP`, `REBUILD`, `CLOSE_BUY`, `CLOSE_SELL`, and `CLOSE_ALL`.
- Apply + Verify sends `UPDATE_SETTINGS` only through the authenticated/device-bound command path.
- A command is shown as successful only after ACK + fresh MT5 runtime state satisfies the command postcondition.
- Read-only account discovery does not authorize execution.
- Runtime absence, account unavailability, stale state, or verification failure remains fail-closed.
- Strategy vault remains local configuration storage and does not authorize live execution.
- Zero/OFF defaults remain the mobile UI defaults.
- Global visual effects remain presentation-only and never issue trading commands.

## Protection boundary

- The production Bot Lab interface is wired to the existing BOT1 business logic, persistence and verified runtime path.
- UI redesigns must not replace verified command execution with visual-only button simulations.
- The original `Grid_Martingale_Basket_v2.mq5` EA remains untouched by application/UI hardening.

## Intentionally not fabricated

- No bridge URL, bearer token, signing secret, account number, broker symbol suffix, or live authorization is embedded in the repository.
- No MQL5 compile result is claimed from Android/GitHub CI because GitHub Actions does not compile the MT5 Expert Advisor.
- Android timeframe selection remains a UI/context selection until the MT5-side timeframe contract is implemented and verified.
- Mobile/GitHub completion does not imply that the real MT5 terminal, broker, HTTPS bridge, or live account has been validated.

## Laptop is the final external validation stage

1. Open MetaEditor and compile the RemoteTarget EA and included AMAR headers.
2. Resolve broker-specific symbol suffix/prefix using MT5 symbol discovery.
3. Configure the bridge HTTPS certificate/key, bearer token, signing secret, durable replay ledger, account scope, magic scope, and allowed target symbols.
4. Start MT5 with the EA on the controlled test account.
5. Connect the Android app to the bridge using the real runtime configuration.
6. Verify read-only health/account/state first.
7. Verify `START`, `STOP`, `REBUILD`, `CLOSE_BUY`, `CLOSE_SELL`, `CLOSE_ALL`, and `UPDATE_SETTINGS` one by one with ACK + state verification.
8. Verify XAUUSD and BTCUSD target switching, including broker suffix variants, while confirming no unrelated-symbol/account collateral is touched.
9. Verify fail-closed behavior for stale market data, stale runtime heartbeat, wrong symbol, wrong magic, expired command, replayed sequence, and bridge interruption.
10. Only after all checks pass, enable controlled live execution and place the first intended trade.

The laptop is therefore the final compile + terminal + broker execution validation gate.
