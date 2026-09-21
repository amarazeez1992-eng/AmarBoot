# Point 12 — Evidence Ranking — Independent Audit

## Date: 2026-09-21
## Base: main @ ebb34bb6
## Item: 3 — Evidence Engine
## Point: 12 — Evidence Ranking

## Scope

Deterministic evidence ranking. No recalculation of upstream signals.

## Files Audited

| # | File | Status |
|---|------|--------|
| 1 | RankedEvidence.kt | ✅ |
| 2 | EvidenceRankingResult.kt | ✅ |
| 3 | RankingSignal.kt | ✅ |
| 4 | AmarEvidenceRankingContract.kt | ✅ |
| 5 | AmarEvidenceRanker.kt | ✅ |
| 6 | AmarEvidenceRankingContractTest.kt | ✅ (16 tests) |

## Design Locks

| Element | Value |
|---------|-------|
| Status Gate | COMPLETE > PARTIAL > STALE |
| Weights | 50/25/15/10 |
| Tie-break | authority DESC → freshness DESC → fingerprint ASC |
| Unranked | UNVERIFIED, INSUFFICIENT, CONFLICTED |
| Fail-Closed | missing signal → not rankable |
| isDownstreamReady | ranked.isNotEmpty() && unranked.isEmpty() |
| Stateless | Yes |
| Deterministic | Yes |

## Protected Files (Unchanged)

- RelevantCandidate.kt ✅
- EvidenceStatus.kt ✅
- EvidenceStatusResult.kt ✅
- AmarEvidenceStatusContract.kt ✅
- AmarEvidenceStatusClassifier.kt ✅
- AmarEvidenceQualityUpstreamState.kt ✅
- AmarEvidenceQualityScoreEngine.kt ✅

## Tests

16 tests, all pass.

## CI Evidence

### Pre-Merge (5/5)
- Stage One: 35609189093 ✅
- Stage Two: 35609189100 ✅
- Stage 11: 35609189081 ✅
- Stage 10: 35609189090 ✅
- CodeQL: 35609189140 ✅

### Post-Merge (7/7)
- Stage Two: 35610225583 ✅
- Stage 8: 35610225742 ✅
- Stage 11: 35610225705 ✅
- Stage 10: 35610225649 ✅
- CodeQL: 35610225815 ✅
- Build APK: 35610225716 ✅
- Final APK Closure: 35610225945 ✅
- Stage One: NOT TRIGGERED — PATH-GATED

## Audit Result

**POINT 12: PASS**
