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

## ERR-INT-002 — Intent classifier lost Arabic morphology and trading instruments

**Status:** Corrected  
**Area:** Intent Understanding  
**Stage alignment:** Stage 2; protects Query Policy and downstream Evidence boundaries  
**Detected:** 2026-09-20

### Symptom
Query Policy regression tests showed financial and factual requests could enter the wrong intent path.

### Root cause
The exact-token false-positive fix was too literal: Arabic definite articles were not canonicalized, structured trading symbols were not recognized as entities, and the `ما هي` factual form was missing.

### Correction
Fixed normalization/entity recognition inside `AmarIntentUnderstanding`; Query Policy and Evidence strictness were not weakened.

### Regression protection
`AmarIntentClassificationRegressionTest.kt` covers Arabic morphology, XAUUSD classification, and Arabic factual questions.

### Rule for future developers
Do not compensate for intent-classification defects by weakening evidence policy or adding keyword checks to orchestration. Fix the understanding/entity layer and add a regression test.
\n