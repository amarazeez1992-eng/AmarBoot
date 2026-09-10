# ADR-0007 — B34–B36 Professional BOT 1 Architecture

Status: APPROVED
Date: 2026-09-10

## Decision

AMAR BOT 1 is elevated from a UI-controlled strategy into a professional runtime component. Implementation is ordered as B34, B35, then B36.

## B34 — AMAR BOT RUNTIME PROFESSIONAL LAYER

B34 establishes the runtime contract around BOT 1 without inventing additional trading bots.

Required components:
- Bot Identity: bot ID, magic, version, strategy ID.
- Configuration: lot, grid, direction, multiplier, max orders, basket TP/SL, trailing and strategy metadata.
- State Machine: OFF, STARTING, RUNNING, STOPPING, REBUILDING, CLOSING, ERROR, EMERGENCY_LOCK.
- Execution Engine boundary.
- Position and Pending Order state.
- Grid, Basket, Trailing and Martingale engine boundaries.
- Risk Engine boundary.
- Command Engine and complete command lifecycle.
- Reconciliation Engine: desired state vs actual MT5 state.
- Event, Audit and Telemetry boundaries.
- Persistent Strategy Profiles with ten slots per BOT.
- Runtime state must be sourced from the runtime/MT5 acknowledgement, not inferred solely from UI button state.

Command lifecycle:
COMMAND → VALIDATE → ACCEPT → EXECUTE → ACKNOWLEDGE → VERIFY → AUDIT

Every command must have identity, version, timestamps/expiry, status, result/error information and idempotency/replay protection.

## B35 — BOT 1 REAL EA INTEGRATION

B35 integrates the approved BOT 1 EA `Grid_Martingale_Basket_v2` with the B34 runtime. The original MQL5 source is authoritative and must be inspected before modifying execution behavior.

Required verification:
- symbol and magic isolation for every BOT 1 operation;
- no accidental closure/modification of unrelated account positions/orders;
- command acknowledgement and post-command state verification;
- desired/actual state reconciliation;
- deterministic error handling;
- emergency lock and fail-closed behavior;
- MT5 compile and runtime testing before any live authorization.

No claim of complete BOT 1 live integration is valid until the actual EA source is integrated and compiled/tested in MT5.

## B36 — AMAR AI SUPERVISOR INTEGRATION

AI is an advisory/supervisory layer, not a direct broker executor.

Approved flow:
AI Agent → Analysis/Proposal → Decision Engine → Simulation → Risk Policy → Execution Policy → Security Gateway → User Approval → MT5

The AI Agent cannot bypass risk, security, permissions, command validation, idempotency, emergency lock, connector health or user approval.

Approved future AI roles:
- BOT analysis;
- performance and drawdown analysis;
- execution-quality analysis;
- anomaly detection;
- market/context analysis;
- strategy evaluation;
- simulation-based optimization proposals;
- explanations and reports;
- supervisor/monitoring assistance.

## Multi-Bot rule

BOT 2, BOT 3 and BOT 4 may exist as saved/configuration slots in the UI, but are not considered actual trading bots until independently approved and implemented with their own engines.

## Strategy Profiles

BOT 1 has ten strategy slots. A slot is a real profile boundary, not merely a visual button. A profile may contain grid, direction, lot model, multiplier, max orders, TP/SL, trailing, risk profile, rebuild rules, entry rules and metadata.

## Chart integration

The BOT runtime will eventually expose entry, grid levels, pending orders, positions, basket targets, trailing, average price, exposure and next-grid information to the chart. No synthetic live market values are permitted.

## Non-negotiable safety

Live execution remains disabled by default. Entering credentials or selecting a real account does not authorize execution. All live execution remains behind explicit authorization and fail-closed security boundaries.

## Work order

B34 first. Then B35 after the runtime contracts are verified. Then B36 after BOT 1 runtime and safety boundaries are stable. Work may be delivered in multiple batches, but later stages must not silently bypass earlier verification.
