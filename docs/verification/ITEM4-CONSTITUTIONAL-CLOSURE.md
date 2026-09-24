# AMARBOT — STAGE 11 — ITEM 4
# CONSTITUTIONAL CLOSURE

## 1. Item Identification

**Item ID:** 4  
**Name:** Hallucination Firewall  
**Status:** **READY FOR MAIN MERGE — Closure Pending Main Re-check**

Item 4 establishes the Hallucination Firewall verification boundary. Additions 1–7 provide independent verification signals, while Addition 8 is the sole final aggregation gate for Item 4.

Constitutional Closure is not yet signed. It remains pending the required Main Re-check after merge.

## 2. Accepted Scope

**Additions:** 8

Additional accepted corrections/patches:

- **Arabic correction:** inside Addition 6 — Self-Contradiction Detection.
- **A8 v2 missing-result patch:** inside Addition 8 — Final Hallucination Decision Gate.

Neither correction creates an additional architectural Addition.

## 3. Final Accepted HEADs

| Addition | Component | Final Accepted HEAD | Status / Relation |
|---|---|---|---|
| 1 | Unsupported Claim Blocking | `5db6a21a` | Accepted |
| 2 | Source Attribution Enforcer | `49aec5a9` | Accepted |
| 3 | Hallucination Detection Guard | `040f9c8a` | Accepted |
| 4 | Temporal Consistency Check | `b5dd0ca5` | Accepted |
| 5 | Cross-Source Agreement | `cc21b422` | Accepted |
| 6 | Self-Contradiction Detection | `67d434cd` | Accepted |
| 6 — Arabic correction | Arabic marker correction inside A6 | `20a36355` | Accepted correction inside A6 |
| 7 | Final Answer Claim Coverage | `d50e8721` | Accepted |
| 8 — original | Final Hallucination Decision Gate | `0c267a7b` | Superseded by `90f97a83` |
| 8 — v2 | Final Hallucination Decision Gate | `90f97a83` | Accepted replacement |
| Integration Test | Item 4 Integration Test | `1e8b0cd9` | Accepted |

The A8 original HEAD is retained as historical evidence because it is the predecessor of the accepted v2 state.

These HEADs represent the accepted implementation/test evidence for Item 4. They do not imply that historical intermediate branches or PRs were merged.

## 4. References

The constitutional closure package is supported by:

1. `docs/verification/STAGE11_ITEM4_HALLUCINATION_FIREWALL.md`
2. `docs/verification/ITEM4-TEST-EVIDENCE.md`

These documents are complementary evidence records. The repository remains the implementation source of truth.

## 5. Deliverables

### Production files

**8 production implementation files**, one for each Item 4 Addition:

1. `UnsupportedClaimBlocking.kt`
2. `SourceAttributionEnforcer.kt`
3. `HallucinationDetectionGuard.kt`
4. `TemporalConsistencyCheck.kt`
5. `AmarCrossSourceAgreementConsumer.kt`
6. `SelfContradictionDetection.kt`
7. `FinalAnswerClaimCoverageGate.kt`
8. `FinalHallucinationDecisionGate.kt`

### Test files

**9 test files total:**

**8 files — one for each Addition:**

1. Addition 1 test
2. Addition 2 test
3. Addition 3 test
4. Addition 4 test
5. Addition 5 test
6. Addition 6 test — including Arabic correction coverage
7. Addition 7 test
8. Addition 8 test — including A8 v2 coverage

**+ 1 Integration Test:**

9. Item 4 Integration Test

The Arabic correction is contained within the Addition 6 test file.

The A8 v2 patch is contained within the Addition 8 test file.

Neither creates a separate test file.

## 6. CI Evidence

Item 4 has **50 CI Run IDs across 10 evidence rows**, covering the five required CI workflows for each accepted verification point.

| Evidence Row | Stage One | Stage Two | Stage 11 | Stage 10 | CodeQL |
|---|---:|---:|---:|---:|---:|
| Addition 1 | `35916844328` | `35916844204` | `35916844313` | `35916844224` | `35916844409` |
| Addition 2 | `35917872173` | `35917872106` | `35917872101` | `35917872141` | `35917872301` |
| Addition 3 | `35924988250` | `35924988142` | `35924988141` | `35924988230` | `35924988191` |
| Addition 4 | `35977182506` | `35977182505` | `35977182384` | `35977182528` | `35977182540` |
| Addition 5 | `35978415044` | `35978415006` | `35978415120` | `35978415240` | `35978414960` |
| Addition 6 | `35979690706` | `35979690767` | `35979690711` | `35979690771` | `35979690779` |
| Addition 6 — Arabic correction | `35982778611` | `35982778779` | `35982778608` | `35982778642` | `35982778622` |
| Addition 7 | `35981156649` | `35981156809` | `35981156642` | `35981156662` | `35981156679` |
| Addition 8 — v2 | `35994454471` | `35994454459` | `35994454536` | `35994454460` | `35994454486` |
| Integration Test | `35995728896` | `35995728795` | `35995728845` | `35995728808` | `35995728789` |

**Total:** 10 rows × 5 workflows = **50 Run IDs**.

All recorded runs in this evidence set were successful at their respective accepted verification points.

## 7. Thirteen-Step Engineering Sequence

| Step | Item 4 Status |
|---|---|
| Inspect | ✅ |
| Requirements | ✅ |
| Architecture | ✅ |
| Implement | ✅ |
| Unit Test | ✅ |
| Integration Test | ✅ |
| Regression Test | ✅ |
| Evidence | ✅ |
| Audit | ✅ |
| Build | ✅ |
| Runtime/CI | ✅ |
| Main Re-check | ⏳ Pending |
| Constitutional Closure | ⏳ Pending |

### Main Re-check

**Status: ⏳ Pending**

Main-branch re-check cannot be declared complete before the accepted Item 4 changes are merged to `main` and the required post-merge repository verification succeeds.

No Main Re-check completion is fabricated in this document.

### Constitutional Closure

**Status: ⏳ Pending**

Constitutional Closure is the final step and will be applied only after successful Main Re-check.

**Closure signature will be applied in a follow-up commit after merge + Main Re-check success.**

## 8. Parking Lot / Open Items

The following items are transferred **verbatim** from `STAGE11_ITEM4_HALLUCINATION_FIREWALL.md` §7.

### 7.1 Addition 6 — `negativePredicateMarkers` dead code

The `negativePredicateMarkers` structure remains identified as dead/unreachable code because the current predicate-index selection reaches positive predicate markers before the negative-predicate marker path. It is recorded and not silently corrected during Item 4 closure.

### 7.2 Addition 3 — diff `+7 / -32`

The accepted Addition 3 production state includes the recorded `+7 / -32` diff context. The exact historical diff/reason remains an item to be documented in closure evidence rather than reconstructed retrospectively.

### 7.3 PR #184

PR #184 corresponds to the earlier Addition 3 production-fix path. Its relationship to the final accepted Addition 3 state remains an open historical classification: **Superseded?** No retrospective merge/closure assumption is made.

### 7.4 PR #185 — Draft state

PR #185 remains recorded as a draft associated with the Addition 3 debug/numeric investigation. Its draft state is retained as historical evidence.

### 7.5 `251d375` — Non-linear ancestor

Commit `251d375...` does not form the direct linear ancestor of the accepted Addition 3 HEAD. The divergence/relation remains recorded for audit purposes; no fabricated ancestry is asserted.

### 7.6 `0b234a46 + 90f97a83` — Process Violation

These two commits were pushed after the explicit user-directed A8 v2 test-fix sequence without a corresponding user command. They are recorded as **Process Violation**.

The final A8 v2 state was nevertheless explicitly accepted by the owner after verification, and the resulting accepted diff from the relevant correction point was verified as the required single test-file brace insertion.

### 7.7 Additions 4–7 — Orchestrator not wired

Additions 4–7 are intentionally not wired into the Orchestrator.

This follows **Decision A — independent Addition implementation + Integration Test first; Orchestrator wiring is deferred to the appropriate later closure/integration scope.**

The absence of Orchestrator wiring is therefore not classified as an implementation failure of Additions 4–7.

## 9. Constitutional Closure Status

**READY FOR MAIN MERGE — Closure Pending Main Re-check**

The Item 4 implementation, verification, evidence, audit, build, and CI requirements have been completed and accepted.

The following remain pending:

1. Merge the accepted Item 4 implementation to `main`.
2. Perform the required Main Re-check against the resulting `main`.
3. Confirm Main Re-check success.
4. Apply the constitutional closure signature in a follow-up commit.

Therefore, Item 4 is **not yet constitutionally closed** at this stage.

**Closure signature will be applied in a follow-up commit after merge + Main Re-check success.**

## 10. Next

**Next:** Item 5

Item 5 work may proceed only according to the established owner-controlled design and implementation sequence after the required Item 4 merge and closure sequence is completed.
