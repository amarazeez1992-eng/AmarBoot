# B30 — BOT 1 Live State Mapping

## Status
Implemented as a read-only integration slice.

## Scope
B30 connects the Android BOT 1 market-status view to the self-hosted MT5 bridge for real data only.

## Data
- MT5 health and terminal connectivity
- Account balance and equity
- BOT 1 positions/pending-order counts filtered by Magic 20260908 and selected symbol
- Floating profit for the filtered BOT 1 state
- Real bid/ask/spread
- Real candles for H4, H1, M30, M15, M5 and M1
- Deterministic descriptive direction percentage calculated from returned candles

## Safety
- No POST execution endpoint exists.
- No order, close, rebuild, or stop command is exposed by B30.
- Market analysis is descriptive only and cannot change BOT 1 settings.
- BOT 1 MQL5 source remains unchanged.
- HTTPS and bearer authentication remain mandatory.
- Bridge defaults to loopback binding until a secure deployment is configured.

## Connection UX
The BOT 1 market-status screen accepts the bridge HTTPS address, bridge token, and MT5 symbol in memory for the current session. Credentials are not persisted by this screen.

## Next boundary
B31 may introduce a secure command channel only after the read-only path is proven on the user's laptop and phone. B31 must add request IDs, idempotency, replay protection, audit, command authorization, and a second live gate before any command endpoint is enabled.
