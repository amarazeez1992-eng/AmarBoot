# B35 — BOT 1 MT5 EA Integration Contract

Status: SOURCE COMMITTED / NOT LIVE AUTHORIZATION

## Authoritative EA

The protected BOT 1 EA baseline is:
`mt5/Experts/Grid_Martingale_Basket_v2.mq5`

Current source version: **2.01**.

Integrity SHA-1 of the protected baseline:
`1125cabef6b4a4b1d5eb2c5deaa4b5e7458479ad25`

This document describes the actual committed baseline. It must not claim safety
properties that are not present in the source.

## Current baseline reality

The current EA is a legacy trading engine and is **NOT live-ready**.

Observed baseline characteristics include:

- non-zero default inputs (`0.01`, `30`, `10`, `2.0`, `$50`, `-$30`);
- BUY and SELL enabled by default;
- hard-coded lot clamp up to `100` lots;
- grid rebuild code calls `CloseAll()` before rebuilding;
- the UI toggle can rebuild the grid when switching back on;
- market/grid behavior is implemented directly inside the EA;
- broker-specific execution validation and complete postcondition reconciliation are not
  sufficient to authorize live trading from this source alone.

These are **known baseline limitations**, not guarantees.

## Protected-source rule

The EA is intentionally protected while the Android/bridge architecture is hardened.
Do not silently modify this file as part of UI, AI, research, or bridge work.
Any future EA hardening must be an explicit, separately reviewed change with a new
integrity fingerprint and demo-terminal verification.

## Required live gate

Before any live authorization, a hardened execution path must verify at minimum:

- symbol digits and normalized prices;
- `SYMBOL_VOLUME_MIN/MAX/STEP` and requested volume;
- `SYMBOL_TRADE_STOPS_LEVEL` and broker distance constraints;
- supported order type and filling mode;
- `MqlTradeResult.retcode` and broker rejection semantics;
- partial fill/partial close behavior;
- duplicate/replay command handling;
- ACK plus read-back runtime state;
- reconciliation after every destructive or execution command;
- BOT1/manual/other-EA isolation by explicit scope and identity;
- emergency lock behavior;
- failure recovery without state corruption.

## Android command boundary

Required path:
`UI → command envelope → security validation → bridge → EA → ACK → read-back runtime state → reconciliation → audit`

Queue acceptance is never execution success.

## Emergency lock

Emergency lock is fail-closed. While locked, new execution commands must be rejected.
Clearing the lock requires explicit authorization and a fresh verified runtime state.

## Live authorization

No production/live-trading authorization is implied by:

- source commitment;
- CI success;
- AI confidence;
- research-source convergence;
- a UI state;
- or a successful command queue insertion.

Live authorization requires a separately verified MT5 demo/runtime gate.
