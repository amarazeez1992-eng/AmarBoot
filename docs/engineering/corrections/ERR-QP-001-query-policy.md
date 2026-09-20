# AMAR AI — Query Policy Correction

## Scope
This correction introduces the missing policy boundary between Intent Understanding and the existing Agent Orchestrator.

## Problem
General factual requests and financial/trading requests were sharing intent-driven research flow without an explicit policy object. This made the evidence strictness decision live directly inside the orchestrator and risked applying financial-grade blocking to non-financial requests.

## Root cause
The orchestrator derived `needsResearch` and `strictEvidence` directly from `AgentIntent`. That was a hidden policy decision embedded in orchestration rather than a named, testable policy layer.

## Correction
Added `AmarQueryPolicy` as a dedicated policy component.

Modes:
- LOCAL_CONVERSATIONAL
- GENERAL_FACTUAL
- FINANCIAL_TRADING
- STRATEGY_ENGINEERING

Rules:
- Identity/time/date/general conversational: local path.
- General factual: normal research and uncertainty handling; no financial strict gate.
- Financial/trading: existing strict evidence gate remains active.
- Strategy engineering: research-capable path without silently converting it into financial trading.

## Architectural constraints preserved
- One Agent.
- One Orchestrator.
- Existing Evidence Engine remains authoritative for financial strictness.
- No keyword-only financial classifier.
- No arbitrary confidence threshold introduced.
- No Stage 11 / Item 3 / Point 10 changes.

## Verification
Dedicated unit tests cover all four policy families relevant to the current backbone.
