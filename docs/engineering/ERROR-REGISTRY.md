# AMAR AI — Engineering Error Registry

## ERR-QP-001 — Evidence policy embedded in orchestration

**Status:** Corrected  
**Area:** Agent backbone / Query Policy  
**Stage alignment:** Stage 2 + Stage 10; supports Stage 7/11 evidence boundaries  
**Detected:** 2026-09-20

### Symptom
The orchestrator decided research and strict evidence behavior directly from `AgentIntent`.

### Root cause
The policy boundary was implicit inside orchestration instead of being represented as an explicit, testable component.

### Risk
General factual requests could become coupled to financial-grade evidence behavior as the pipeline grows.

### Correction
Introduced `AmarQueryPolicy` with explicit modes and deterministic policy outputs.

### Regression protection
`AmarQueryPolicyTest.kt` verifies:
1. system/local requests bypass external research;
2. general factual requests use research without the financial strict gate;
3. financial/trading requests retain the strict evidence gate.

### Rule for future developers
Do not reintroduce query-policy decisions directly into the orchestrator or implement them as raw keyword checks. Extend the policy component and its tests instead.
