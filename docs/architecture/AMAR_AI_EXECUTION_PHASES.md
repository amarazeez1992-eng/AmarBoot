# AMAR AI — Execution Phases

Status: APPROVED BY OWNER

## Phase A — Application readiness

The Android application owns:
- natural-language discussion and strategy review;
- deterministic app navigation/introspection;
- explicit execution-intent parsing;
- durable `PENDING_MT5` command records;
- grid and tracking lifecycle intents;
- dollar-based stop-loss/profit-trigger intents;
- AI authority/emergency-stop boundaries;
- measured market evidence and validation engines.

No Android component claims that a broker order was executed merely because an intent was queued.

## Phase B — Laptop/MT5 bridge (final integration stage)

Only after the Android application is ready, connect:

`AMAR AI -> Android command record -> laptop bridge -> CMG/command channel -> MT5 -> terminal ACK -> fresh state -> verification -> Android result`

The bridge must preserve:
- authentication;
- HMAC/signature validation;
- TTL;
- nonce/idempotency/replay protection;
- account/magic/symbol allow-lists;
- broker volume min/max/step validation;
- terminal ACK;
- fresh runtime state;
- fail-closed behavior.

## Execution truth

The only accepted final states are:
- `PENDING_MT5`
- `EXECUTED`
- `REJECTED`
- `VERIFIED`
- `FAILED`
- `STALE`

`QUEUED` or `accepted=true` alone is never execution success.

## User command model

Examples that the application must prepare as canonical intents:
- open BUY/SELL with explicit symbol and lot;
- close all;
- set dollar stop-loss;
- set dollar profit trigger;
- start/stop grid;
- start/stop tracking;
- set bot lot.

The AI may discuss and prepare these intents, but it must not invent missing financial values.

## Final stage boundary

The laptop and MT5 connection is deliberately the last stage. Until it is explicitly configured, the application remains real at the command-contract and persistence level but cannot honestly claim broker execution.
