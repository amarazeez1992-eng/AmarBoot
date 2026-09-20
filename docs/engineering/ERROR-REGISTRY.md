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


## ERR-PT10-001 — Point 10 upstream integration gap

**Status:** Corrected — CI verified on implementation commit 1a13bf99a4ed0c247cbb5f2c92c3c7cead256944  
**Area:** Stage 11 / Item 3 / Point 10  
**Detected:** 2026-09-20

### Symptom
Point 10 certification existed but the canonical runtime path did not yet connect the owning Point 1 Evidence Intake and Point 8 Tampering outputs.

### Root cause
The fail-closed composition boundary had no runtime adapter from the existing verification report to the Point 10 upstream-state contract.

### Correction
Connected Point 1 from the verification layer's usable/invalid evidence counts and Point 8 from its recorded provenance chain through AmarEvidenceTamperingDetector. The runtime now records and, for strict financial/trading requests, gates final approval on canonical Point 10 certification.

### Regression protection
Added canonical assembler tests for successful owner-state composition and tampered provenance rejection.

### Rule for future developers
Never infer an upstream Evidence Engine state in Point 10. Source it from the owning contract and preserve fail-closed behavior.

## ERR-RESEARCH-001 — External research retrieval ceiling and provider fragility

**Status:** Corrected — CI verified on implementation commit 1a13bf99a4ed0c247cbb5f2c92c3c7cead256944  
**Area:** External Research  
**Stage alignment:** Research foundation following Stage 0 → 10 closure  
**Detected:** 2026-09-20

### Symptom
The Agent requested 40 sources while the research contract and budget support an 80-source target, and provider retrieval was serialized.

### Root cause
The engine boundary was not consuming the available 80-source target and network provider calls could consume the Agent timeout sequentially.

### Correction
Raised the retrieval target to 80, bounded the external result set at 80, parallelized the three approved public providers, added bounded network timeouts, and canonical URL deduplication.

### Rule for future developers
Do not treat source count as proof of independence or authority. Missing or weak evidence must remain visible to the existing verification/evidence gates; never weaken those gates to make research appear successful.


## ERR-RESEARCH-002 — General factual synthesis did not surface the direct verified fact

**Status:** Corrected — pending fresh CI on current main  
**Area:** External Research / Local Reasoning  
**Detected:** 2026-09-20

### Symptom
A general factual request could retrieve evidence successfully but the final response only summarized the evidence instead of directly surfacing a matching factual sentence.

### Root cause
The local synthesis layer ranked evidence records but did not perform a final extractive sentence selection step. Arabic factual requests were also sent only to English Wikipedia, reducing cross-language evidence matching.

### Correction
1. Select Wikipedia by the request language (`ar` for Arabic, `en` otherwise).
2. Retrieve Wikipedia extracts as evidence rather than relying only on search snippets.
3. Add a generic direct-evidence sentence selector based on query/evidence token overlap.
4. Return the selected sentence with its source title and URI.
5. No question→answer mapping or hardcoded factual answer was added.

### Regression protection
`AmarLocalReasoningTest.local_reasoning_extracts_direct_fact_from_matching_evidence` verifies that a supplied Wikipedia-style evidence sentence is surfaced directly.

### Rule for future developers
Do not hardcode factual answers to make the Agent appear successful. Improve retrieval, evidence matching, and extractive synthesis instead.
