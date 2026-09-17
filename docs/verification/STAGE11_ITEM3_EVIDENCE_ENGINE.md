# Stage 11 — Item 3: Evidence Engine

Status: VERIFICATION IN PROGRESS

## Sequential execution rule

Item 3 is executed strictly in points **1 → 24**. A later point is not considered progress until the preceding point has completed its own verification cycle and constitutional closure. Point 13 work on any older branch/PR is therefore out of sequence and does not constitute Item 3 progress until points 1–12 are closed.

## Point 1 — Evidence Intake

Status: CONSTITUTIONALLY CLOSED

Point 1 is constitutionally closed after implementation, focused regression, full regression, debug build, architecture checks, PR merge, and successful main-branch post-merge final APK verification against the merged commit.

Canonical implementation: `AmarEvidenceIntake`.

Point 1 performs only structural admission:

- accepts findings with non-blank `sourceUri` and `evidence`;
- rejects structurally incomplete findings while preserving the original candidate for diagnostics;
- preserves input order and candidate identity;
- does not apply authority, freshness, source-independence, uniqueness, fingerprint-integrity, ranking, or claim-verification rules, because those belong to later Item 3 points/canonical components;
- does not introduce research orchestration, decision logic, or execution authority.

Focused coverage is in `AmarEvidenceIntakeTest` and integration is wired through `AmarAgentOrchestrator`.

### Point 1 closure evidence

- PR #77: merged successfully.
- Merged commit: `f655a4bf3577f9ca9ca699e0ca8c8c8c0596d1bb`.
- Point 1 CI run: `35234827293` — successful focused tests, full unit suite, debug build, architecture regression checks, and evidence artifact upload.
- Stage 10 regression/audit run associated with the merge: `35234827262` — successful regression, debug/release builds, artifact verification, dependency/license/provenance, security, architecture, execution-safety, and release-hygiene gates.
- Main post-merge Final APK Closure run: `35242700953` — successful on `main` at the merged commit.
- Final APK artifact: `AmarAI-final-debug-apk`.
- Final APK artifact SHA-256: `8caee0a8f723133e7521e24630ddc36cb443188308c8f708534f8526ec9776fb`.

This closure is limited to Point 1. Point 2 is the next authorized sequential point; no later Item 3 point is treated as completed by this closure.

## Root-cause architecture decision

The baseline already contained the canonical `AmarEvidence` model, `AmarEvidenceQualityEngine`, provenance support, source registry support, and semantic conflict detection. A second `AmarEvidenceEngine` would duplicate authority, freshness, fingerprint, and conflict responsibilities.

Therefore Item 3 is implemented by **strengthening and formalizing the existing canonical evidence-quality boundary**, not by adding a parallel engine.

## Canonical structure

```text
ResearchFinding
    |
    v
AmarEvidenceIntake          <- Point 1 structural admission
    |
    v
AmarEvidenceQualityEngine   <- canonical evidence-quality authority
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
- Sequential Point 1 intake boundary with focused tests.
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

1. Focused Point 1 tests.
2. Item 1 and Item 2 regression tests.
3. Existing Verification/Memory/Research/Decision/Critic focused regressions.
4. Full debug unit-test suite.
5. Debug APK build.
6. Architecture regression checks proving there is only one canonical evidence-quality boundary and no execution authority inside it.
7. CI test evidence artifact.
8. PR merge.
9. Main-branch post-merge recheck against the merged commit.
10. Only then: constitutional closure of Point 1.

**Point 1 has now satisfied these gates. Item 3 remains open because points 2–24 are not yet closed.**

**No closure is inferred from compilation or build success alone.**
