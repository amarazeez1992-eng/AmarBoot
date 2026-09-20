# ERR-FRESHNESS-002 — Accuracy-First Verification Wait Policy

## Status
CONSTITUTIONALLY ADDED — IMPLEMENTATION PENDING

## Decision
The project owner approved a constitutional rule that response accuracy and evidence verification take priority over response speed.

## Rule
When an answer depends on research, market data, historical data, evidence comparison, calculation, or verification, AMAR AI must not weaken verification or invent/guess information merely to answer faster.

The Agent may wait substantially longer when necessary to complete reliable research and verification. The wait must remain observable and managed by task state/time-budget controls; it must not become an unbounded silent hang.

## Required distinctions
- Current information: verify against current evidence.
- Historical information: use attributable historical data/evidence.
- Stored knowledge: preserve source time, status, and evidence linkage.
- Future/forecast: never present as established fact.
- Missing/unavailable evidence: explicitly report insufficiency rather than fill gaps by guessing.

## Financial/trading requirement
For gold, forex, and other market questions, accuracy and source verification take precedence over latency. Historical questions must be answered from verifiable historical evidence/data when available. Current questions require current verification. Forecasts must remain forecasts and must not be presented as facts.

## Architectural impact
This is a constitutional quality rule, not permission to bypass existing Evidence, Verification, Freshness, Confidence, Audit, or Failure Transparency layers.

Implementation must preserve the existing architecture and add the rule at the appropriate orchestration/task-state/performance controls. No simplification, deletion, hardcoded answer mapping, or keyword-only shortcut is permitted.

## Closure requirement
This constitutional decision is accepted as a rule. Its implementation is not considered complete until:
Implemented → Integrated → Tested → Failure Tested → Regression Tested → Documented → Audited → Runtime Verified → Accepted → Stable.
