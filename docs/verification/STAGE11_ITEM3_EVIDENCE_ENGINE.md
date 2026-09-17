# Stage 11 — Item 3: Evidence Engine

Status: VERIFICATION IN PROGRESS

## Root-cause architecture decision

The baseline already contained the canonical `AmarEvidence` model, `AmarEvidenceQualityEngine`, provenance support, source registry support, and semantic conflict detection. A second `AmarEvidenceEngine` would duplicate authority, freshness, fingerprint, and conflict responsibilities.

Therefore Item 3 is implemented by **strengthening and formalizing the existing canonical evidence-quality boundary**, not by adding a parallel engine.

## Canonical structure

```text
ResearchFinding
    |
    v
AmarEvidenceQualityEngine
    |-- AmarEvidencePolicy (versioned configuration)
    |-- authority scoring
    |-- freshness decay
    |-- source independence
    |-- duplicate/fingerprint detection
    |-- integrity validation
    |-- deterministic ranking
    v
AmarEvidenceQualityReport
    |
    +--> AmarVerificationLayer
           |-- semantic conflict detection
           |-- provenance chain
           |-- claim verification
           v
        verification result
```

## Included

- Explicit versioned evidence policy.
- Deterministic authority/freshness/independence/uniqueness scoring.
- Fingerprint integrity validation.
- Fail-closed behavior for integrity violations.
- Duplicate evidence detection.
- Deterministic evidence ranking.
- Bounded quality/status output.
- Regression tests for strong, stale, duplicate, tampered, invalid-policy, and future evidence cases.

## Deliberately excluded

- Research retrieval/orchestration.
- Recommendation or trading decisions.
- Model/provider routing.
- Broker/order execution.
- A second competing evidence engine.
- Semantic conflict ownership: this remains in `AmarConflictDetector` inside the existing Verification Layer.

## Verification gate

Item 3 cannot close until all of the following succeed against the same source tree:

1. Focused Item 3 tests.
2. Item 1 and Item 2 regression tests.
3. Existing Verification/Memory/Research/Decision/Critic focused regressions.
4. Full debug unit-test suite.
5. Debug APK build.
6. Architecture regression checks proving there is only one canonical evidence-quality boundary and no execution authority inside it.
7. CI test evidence artifact.
8. PR merge.
9. Main-branch post-merge recheck against the merged commit.
10. Only then: constitutional closure record.

**No closure is inferred from compilation or build success alone.**
