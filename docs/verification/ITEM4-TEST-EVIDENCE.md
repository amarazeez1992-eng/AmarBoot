# ITEM 4 — TEST EVIDENCE

## Stage 11 — Hallucination Firewall

**Document:** `docs/verification/ITEM4-TEST-EVIDENCE.md`  
**Status:** Approved  
**Scope:** Item 4 Additions 1–8 + Addition 6 Arabic correction + Integration Test  
**Repository:** `amarazeez1992-eng/AmarBoot`

---

## 1. Complete CI Run ID Table

| Addition | HEAD | Stage One | Stage Two | Stage Eleven | Stage 10 | CodeQL | Conclusion |
|---|---|---:|---:|---:|---:|---:|---|
| Addition 1 | `5db6a21a` | `35916844328` | `35916844204` | `35916844313` | `35916844224` | `35916844409` | PASS |
| Addition 2 | `49aec5a9` | `35917872173` | `35917872106` | `35917872101` | `35917872141` | `35917872301` | PASS |
| Addition 3 | `040f9c8a` | `35924988250` | `35924988142` | `35924988141` | `35924988230` | `35924988191` | PASS |
| Addition 4 | `b5dd0ca5` | `35977182506` | `35977182505` | `35977182384` | `35977182528` | `35977182540` | PASS |
| Addition 5 | `cc21b422` | `35978415044` | `35978415006` | `35978415120` | `35978415240` | `35978414960` | PASS |
| Addition 6 | `67d434cd` | `35979690706` | `35979690767` | `35979690711` | `35979690771` | `35979690779` | PASS |
| Addition 6-Arabic | `20a36355` | `35982778611` | `35982778779` | `35982778608` | `35982778642` | `35982778622` | PASS |
| Addition 7 | `d50e8721` | `35981156649` | `35981156809` | `35981156642` | `35981156662` | `35981156679` | PASS |
| A8 v2 | `90f97a83` | `35994454471` | `35994454459` | `35994454536` | `35994454460` | `35994454486` | PASS |
| Integration Test | `1e8b0cd9` | `35995728896` | `35995728795` | `35995728845` | `35995728808` | `35995728789` | PASS |

**Total:** 10 evidence rows × 5 CI workflows = **50 Run IDs**.

---

## 2. Test Files

| Addition | Test file | Tests |
|---|---|---:|
| Addition 1 | `app/src/test/java/com/personal/gridbot/amaros/agent/UnsupportedClaimBlockingTest.kt` | 8 |
| Addition 2 | `app/src/test/java/com/personal/gridbot/amaros/agent/SourceAttributionEnforcerTest.kt` | 8 |
| Addition 3 | `app/src/test/java/com/personal/gridbot/amaros/agent/HallucinationDetectionGuardTest.kt` | 10 |
| Addition 4 | `app/src/test/java/com/personal/gridbot/amaros/agent/TemporalConsistencyCheckTest.kt` | 10 |
| Addition 5 | `app/src/test/java/com/personal/gridbot/amaros/agent/correlation/AmarCrossSourceAgreementConsumerTest.kt` | 5 |
| Addition 6 | `app/src/test/java/com/personal/gridbot/amaros/intelligence/verification/SelfContradictionDetectionTest.kt` | 5 |
| Addition 6-Arabic | `app/src/test/java/com/personal/gridbot/amaros/intelligence/verification/SelfContradictionDetectionTest.kt` | 7 |
| Addition 7 | `app/src/test/java/com/personal/gridbot/amaros/intelligence/verification/FinalAnswerClaimCoverageGateTest.kt` | 7 |
| A8 v2 | `app/src/test/java/com/personal/gridbot/amaros/intelligence/verification/FinalHallucinationDecisionGateTest.kt` | 7 |
| Integration Test | `app/src/test/java/com/personal/gridbot/amaros/intelligence/verification/Item4HallucinationFirewallIntegrationTest.kt` | 9 |

**Note:** Addition 6-Arabic modifies the same test file as Addition 6; the count shown is the count at that specific accepted Arabic-correction HEAD.

---

## 3. Integration Test

**File:** `app/src/test/java/com/personal/gridbot/amaros/intelligence/verification/Item4HallucinationFirewallIntegrationTest.kt`

**HEAD:** `1e8b0cd9d723ead87025a87c9d95f2baa32945fb`

**CI:** 5/5 PASS

### Cases A–I

| Case | Scenario | Expected result |
|---|---|---|
| A | Full PASS propagation | `PASS / NO_BLOCKING_SIGNAL` |
| B | Addition 1 blocking signal | `BLOCK / ADDITION_1_BLOCKED` |
| C | Addition 4 insufficient temporal data | `BLOCK / ADDITION_4_INSUFFICIENT_TEMPORAL_DATA` |
| D | Addition 5 disagreement | `BLOCK / ADDITION_5_DISAGREEMENT` |
| E | Arabic self-contradiction | `BLOCK / ADDITION_6_SELF_CONTRADICTION`, pair `(0,1)` |
| F | Addition 7 incomplete coverage | `BLOCK / ADDITION_7_COVERAGE_INCOMPLETE` |
| G | Fail-closed insufficient Addition 6 state | `BLOCK / ADDITION_6_INSUFFICIENT_SELF_CONTRADICTION_DATA` |
| H | Missing Addition 1 result | `BLOCK / INSUFFICIENT_ADDITION_N_DATA`, missing `[1]` |
| I | All seven blocking signals simultaneously | `BLOCK / ADDITION_1_BLOCKED` only |

**Code verification at Integration Test HEAD:** The `FinalDecisionReason` enum at `1e8b0cd9` contains both `ADDITION_6_SELF_CONTRADICTION` and `ADDITION_6_INSUFFICIENT_SELF_CONTRADICTION_DATA` exactly as written above.

Case E invokes the actual `SelfContradictionDetector` and verifies the exact pair indices rather than fabricating a contradiction result.

Case H passes `addition1 = null` and verifies the A8 v2 missing-result path.

Case I supplies all seven blocking signals and verifies the established priority rule: Addition 1 is returned as the blocking reason.

---

## 4. CI Workflows

| Workflow | Purpose |
|---|---|
| **AMAR AI Stage One Verification** | Stage One repository/project verification required by the CI gate. |
| **AMAR AI Stage Two Verification** | Stage Two verification required by the CI gate. |
| **Amar Stage Eleven** | Stage 11-specific verification and tests. |
| **Amar Stage 10 — Final 100% Audit and Release Gate** | Final audit/release-gate verification required before accepting the change. |
| **CodeQL** | Static security/code analysis for the repository. |

A final Addition/Test result is considered CI-complete only when all five workflows report `success`.

---

## 5. Evidence Path

An auditor can independently retrieve the evidence using the repository and the recorded HEAD SHA.

For each row:

1. Identify the recorded **HEAD** commit.
2. Retrieve the commit metadata and diff from GitHub.
3. Verify the commit message and changed-file scope.
4. Retrieve the five GitHub Actions workflow runs associated with that exact commit SHA.
5. Match each Run ID against the workflow name recorded in this document.
6. Confirm `status = completed` and `conclusion = success`.
7. For the Integration Test, inspect the recorded test file at HEAD `1e8b0cd9` and verify Cases A–I.

The Run ID is the immutable CI evidence reference; the HEAD SHA establishes exactly which repository state produced that run.

---

## 6. No Fabricated Data

This evidence package does not claim that test fixtures are real external evidence.

The tests use deterministic local fixtures required to exercise the implementation contracts. No external research source, live market feed, network response, or fabricated production result is used as evidence of system behavior.

Where test fixtures contain example URIs such as `https://example.com/...`, they are test data only and are not presented as real research sources.

The Integration Test does not fabricate detector outputs for Cases E, H, or I:

- **Case E** invokes the actual self-contradiction detector.
- **Case H** explicitly supplies a missing nullable Addition result.
- **Case I** explicitly supplies the seven blocking signals and verifies A8's deterministic priority.

---

## Evidence Status

**Item 4 Integration Test:** PASS  
**Additions 1–8:** PASS  
**Addition 6 Arabic correction:** PASS  
**CI evidence:** 50 Run IDs / 10 accepted evidence rows / 5 workflows per row  
**Document status:** Approved
