# Step 4 — Evidence Selection — Independent Audit

## Date: 2026-09-21
## Base: main @ a2b20a80
## Step: Step 4 — Evidence Selection (per Article 15)

## Scope

Selection Boundary only. No relevance recalculation.

## Artifacts

| # | File | Status |
|---|------|--------|
| 1 | SelectedEvidence.kt | ✅ |
| 2 | EvidenceSelectionResult.kt | ✅ |
| 3 | AmarEvidenceSelectionContract.kt | ✅ |
| 4 | AmarEvidenceSelection.kt | ✅ |
| 5 | AmarEvidenceSelectionContractTest.kt | ✅ |

## Design Locks

| Element | Value |
|---------|-------|
| Selection Rule | Top-N |
| N (default) | 10 |
| Tie-break | relevanceScore DESC, fingerprint ASC |
| Result | EvidenceSelectionResult(selected, dropped) |
| Dropped signal | Presence = SELECTION_LIMIT_REACHED |
| Mutation of RelevantCandidate | ❌ None |

## Protected Files

| File | Modified? |
|------|-----------|
| RelevantCandidate.kt | ❌ |
| AmarQuestionRelevance.kt | ❌ |
| AmarRetrievalRelevanceEngine.kt | ❌ |
| AmarSourceVerifier.kt | ❌ |
| AmarAiExternalResearch.kt | ❌ |
| MIN_RELEVANCE_SCORE | ❌ |

## Tests

12 tests, all pass.

## CI Evidence (Pre-Merge)

| Workflow | Run ID | Conclusion |
|----------|--------|------------|
| Stage One | 35560946042 | ✅ |
| Stage Two | 35560946299 | ✅ |
| Stage 11 | 35560945988 | ✅ |
| Stage 10 | 35560946089 | ✅ |
| CodeQL | 35560946044 | ✅ |

## CI Evidence (Post-Merge)

Merge SHA: a2b20a8026a510904fb6fa4047362daedea1942b

| Workflow | Run ID | Conclusion |
|----------|--------|------------|
| Stage Two | 35561960918 | ✅ |
| Stage 8 | 35561960917 | ✅ |
| Stage 11 | 35561960939 | ✅ |
| Stage 10 | 35561960930 | ✅ |
| CodeQL | 35561960932 | ✅ |
| Build APK | 35561960926 | ✅ |
| Final APK Closure | 35561960974 | ✅ |
| Stage One | — | NOT TRIGGERED — PATH-GATED |

## Audit Result

**STEP 4: PASS**
