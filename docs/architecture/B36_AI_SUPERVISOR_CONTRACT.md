# B36 — AMAR AI Supervisor Contract

Status: IMPLEMENTED / ADVISORY ONLY

## Authority boundary
The Supervisor may analyze BOT 1 telemetry, runtime state, historical behavior, performance, drawdown, execution quality, anomalies and strategy profiles. It may create proposals and explanations.

It has **no direct Broker or MT5 execution capability**.

## Mandatory proposal path
`AI Agent → Analysis → Proposal → Decision Engine → Simulation → Risk Policy → Execution Policy → Security Gateway → User Approval → MT5`

## Proposal invariants
Every proposal must:
- identify BOT 1 and its strategy context;
- include rationale and confidence;
- state whether simulation is required;
- require explicit user approval before any execution path;
- remain non-executable at the Supervisor layer.

## Safety
The Supervisor cannot bypass:
- emergency lock;
- permissions;
- risk policy;
- connector health;
- command validity;
- idempotency/replay protection;
- security gateway;
- runtime verification/reconciliation.

## Approved AI roles
- BOT analysis
- performance/drawdown analysis
- execution-quality analysis
- anomaly detection
- market/context analysis
- strategy evaluation
- simulation-based optimization proposals
- explanations/reports
- monitoring assistance

No Direct AI Trading is implemented or authorized.
