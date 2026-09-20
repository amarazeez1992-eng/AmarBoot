# AMAR AI — STEP 2: EVIDENCE INTAKE DESIGN

**Status:** DESIGN DOCUMENT — IMPLEMENTATION NOT STARTED  
**Base:** main @ b02e181b199c171955dd9605ba18da9dcc097253  
**Branch:** feat/step2-evidence-intake

## 1. Purpose and relationship to Item 3 Point 1

**Point 1 = Evidence Intake.** Constitutional **Step 2 = the operational implementation boundary for Point 1**.

Its purpose is to close the operational gap required for the Item 3 evidence-quality pipeline without changing downstream evidence-quality owners.

Step 2 receives raw provider/retrieval candidates, normalizes them, performs only technical validation and technical deduplication, and emits normalized candidates for the constitutional Step 3 relevance gate.

Step 2 does not make an evidence-quality or answerability judgment.

## 2. Strict boundary

### Step 2 MAY perform
- URL canonicalization.
- Source normalization.
- Title normalization.
- Evidence/text normalization while preserving meaning.
- Deterministic technical fingerprint generation.
- Technical duplicate detection/deduplication.
- Structural validation of candidate shape.
- Rejection of technically malformed/unusable candidates.

### Step 2 MUST NOT perform
- Question relevance or semantic answerability.
- `MIN_RELEVANCE_SCORE`.
- Authority scoring or authority acceptance.
- Freshness analysis.
- Source independence analysis.
- Conflict analysis.
- Claim ↔ evidence verification.
- Evidence acceptance/admission.
- Reasoning or final-answer generation.

A candidate that is valid but unrelated MUST survive Step 2 so that Step 3 can reject it on relevance grounds. A trusted but unrelated source is still not admitted evidence.

## 3. Data structures

### EvidenceCandidate

`EvidenceCandidate` is a **boundary DTO**, not a new evidence model. It represents raw provider output entering the constitutional pipeline.

Minimum conceptual fields:
- provider/source identifier
- title
- URL
- excerpt/evidence text
- retrieval timestamp

It must not contain computed relevance, authority, freshness, acceptance, or downstream quality state.

### NormalizedCandidate

Represents a technically valid candidate after deterministic normalization:
- normalized source
- normalized title
- canonical URL
- normalized evidence text
- retrieval timestamp
- technical fingerprint
- normalization metadata/flags where needed

### IntakeRejectedCandidate

Represents a candidate rejected for a technical intake reason only, such as malformed URL, blank evidence, or structurally invalid input.

It must not encode a relevance, authority, freshness, or acceptance decision.

## 4. Contract

The constitutional contract is:

```kotlin
interface AmarEvidenceIntakeContract {
    fun intake(
        question: String,
        candidates: List<EvidenceCandidate>
    ): EvidenceIntakeResult
}
```

The `question` is retained at the boundary for traceability and future pipeline correlation. Step 2 MUST NOT use it to calculate relevance.

Conceptual result:

```kotlin
data class EvidenceIntakeResult(
    val candidates: List<NormalizedCandidate>,
    val rejectedCandidates: List<IntakeRejectedCandidate>
)
```

The result separates technically usable candidates from technically rejected candidates. Neither collection is an evidence-acceptance decision.

## 5. Current-state analysis

The current retrieval implementation performs relevance scoring and applies `MIN_RELEVANCE_SCORE` before returning results. This means the current runtime boundary still combines retrieval with part of constitutional Step 3.

This document does **not** move that logic into Step 2.

The migration decision is:

1. Step 2 is implemented as normalization + technical dedup/validation only.
2. Step 3 will become the constitutional owner of Question Relevance.
3. Relevance filtering currently embedded in retrieval is migrated to Step 3 as a separate, tested change.
4. No relevance threshold is changed merely to perform Step 2.
5. No duplicate relevance implementation is introduced in Step 2.

Until that migration is separately implemented and verified, the architecture must be reported as transitional rather than falsely declared fully separated.

## 6. Target data flow

```text
Provider Retrieval
      ↓
EvidenceCandidate (Raw)
      ↓
STEP 2 — INTAKE
(Normalize + Technical Dedup)
      ↓
NormalizedCandidate
      ↓
STEP 3 — RELEVANCE
(Question Relevance)
      ↓
STEP 4 — SELECTION
      ↓
Point 2 → Point 9 → Point 10
```

No later stage may silently take ownership of an earlier stage's constitutional responsibility.

## 7. Test requirements

The implementation must include deterministic tests for:

1. URL normalization.
2. Whitespace normalization.
3. Title normalization.
4. Text normalization with meaning preserved.
5. Duplicate canonical URL → technical deduplication.
6. Duplicate technical fingerprint → technical deduplication.
7. Invalid URL → intake rejection.
8. Blank evidence → intake rejection.
9. Valid unrelated article → passes Step 2 and remains available for Step 3.
10. Trusted unrelated article → passes technical intake; trust is not acceptance.
11. No relevance score is calculated by Step 2.
12. No freshness calculation is performed by Step 2.
13. No authority calculation is performed by Step 2.
14. Repeated identical input produces identical normalized output and rejection reasons.

Tests must also prove that `ResearchFinding` is not modified by Step 2.

## 8. Explicit non-touch list

Step 2 implementation MUST NOT modify:
- `ResearchFinding`
- `AmarEvidenceFreshnessAnalyzer`
- `AmarLocalReasoning`
- `AmarEvidenceQualityUpstreamState`
- `MIN_RELEVANCE_SCORE`

Step 2 also MUST NOT change the meaning of Point 10.

## 9. Acceptance criteria

Step 2 is complete only when current evidence proves all applicable gates:

- Point 1 has an operational intake implementation.
- Step 2 performs only its constitutional responsibilities.
- No relevance computation exists in Step 2.
- Retrieval/Step 3 separation is explicitly tracked; migration is not hidden.
- The normalized candidate boundary is deterministic and tested.
- Relevant focused tests pass.
- Full relevant regression tests pass.
- Build/compile passes.
- CI is green on the current commit.
- Architecture/contract audit passes.
- Runtime evidence is collected where the implementation is integrated into runtime.
- Independent substantive re-verification passes under the project constitution.

Design-document completion alone does not close Point 1, Step 2, or Item 3 Point 10.

## 10. Rollback plan

The rollback baseline is:

`main @ b02e181b199c171955dd9605ba18da9dcc097253`

The design-only phase must not alter Point 1 runtime behavior or Point 10 behavior.

If implementation introduces a regression, revert the Step 2 implementation branch/commit to this baseline. Point 1 and Point 10 remain unchanged until an independently verified integration change is approved.

## 11. Constitutional status

This document records the approved boundary and migration plan only.

**Current status:**
- Step 1: COMPLETE on main.
- Step 2: DESIGN APPROVED; implementation pending.
- Step 3: BLOCKED pending Step 2 implementation/integration evidence.
- Step 4: BLOCKED pending Step 3.

No closure claim is made by this document.
