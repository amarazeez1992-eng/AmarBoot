# AMAR AI — ADMISSION LAYER DESIGN

## Status
- Stage: Stage 11 — AMAR INTELLIGENCE 99
- Scope: Evidence Admission / Relevance ownership migration
- Baseline: main @ e2f799750d6babb801abfbba1b501d7e9fa9cc95
- Status: DESIGN ONLY — not activated
- Merge policy: no merge until CI + APK + runtime regression are green
- Freshness is explicitly outside this migration.
- ResearchFinding remains unchanged during this migration.

## 1. Purpose
This document establishes one canonical authority for evidence admission. The current defect is distributed ownership of relevance decisions across retrieval, source verification, and reasoning. The target authority is AmarEvidenceAdmissionLayer. AmarRetrievalRelevanceEngine becomes an internal implementation that calculates relevance characteristics but does not own the constitutional accept/reject decision.

Core distinction:
- relevanceScore = measurement
- admissionState = contractual decision

A trusted source is not automatically relevant evidence.

## 2. Definitions
### 2.1 AdmissionState
Values: ADMITTED, REJECTED.
ADMITTED means the finding passed the canonical Admission policy for the supplied question and may enter downstream verification. REJECTED means it failed Admission and must not enter downstream verification or reasoning as accepted evidence.

### 2.2 AdmittedFinding
Fields:
- finding: ResearchFinding
- state: AdmissionState
- relevanceScore: Double
- admissionReason: String

The original ResearchFinding is preserved. Admission state is not authority, freshness, independence, confidence, or a trading decision.

### 2.3 AdmissionResult
Fields:
- admitted: List<AdmittedFinding>
- rejected: List<AdmittedFinding>

Invariant: admitted contains ADMITTED only. rejected contains REJECTED only. Only admitted is eligible for SourceVerifier. Rejected remains audit/telemetry information.

## 3. AmarEvidenceAdmissionContract
Contract: admit(question: String, findings: List<ResearchFinding>): AdmissionResult

Contract invariants:
1. The question is the actual user question used to obtain the candidates.
2. Every input finding receives exactly one Admission outcome.
3. Admission is the sole constitutional owner of relevance acceptance.
4. Relevance is decided before downstream verification.
5. Rejected findings cannot be promoted downstream.
6. The same question and finding content produce a deterministic result.
7. Admission does not invent factual answers.
8. Admission does not verify authority, freshness, independence, final confidence, claim support, or trading decisions.

## 4. Responsibilities
### Admission does
- Receive question and raw ResearchFinding candidates.
- Invoke AmarRetrievalRelevanceEngine.score().
- Apply the approved Admission policy and threshold.
- Produce ADMITTED or REJECTED.
- Preserve the original finding and record its relevance measurement.
- Produce deterministic audit reasoning.
- Separate admitted and rejected outputs.

### Admission does not
- own authority scoring
- own freshness
- own independence
- own duplicate/fingerprint/tampering checks
- own claim-to-evidence verification
- own confidence calibration
- generate BUY/SELL, entry, stop-loss, take-profit, or broker actions

## 5. Relevance Engine Boundary
Target ownership:
- AmarEvidenceAdmissionLayer = canonical authority
- AmarRetrievalRelevanceEngine = internal implementation

AmarRetrievalRelevanceEngine.score() remains the calculation mechanism. Its independent accept() path must not remain a second constitutional authority. The current MIN_RELEVANCE_SCORE value is not changed by this design.

## 6. ResearchFinding Preservation
ResearchFinding remains unchanged during this migration. Its relevanceScore field is retained for compatibility and transition safety. No removal or reinterpretation is authorized in this design phase.

## 7. Integration Points
### Orchestrator
Target flow: User Question → Intent/Understanding → Research → Raw Findings → Admission → admitted Findings → Source Verification → Freshness → Independence → Claim/Evidence checks → Reasoning → Final Answer.

### AmarAiExternalResearch
Current behavior calculates, filters, and sorts by relevance. Target behavior is candidate retrieval. It must not become a hidden second Admission authority.

### AmarSourceVerifier
Current verify() and isIndependent() contain relevance threshold gates. Target behavior consumes admitted findings and removes duplicated relevance decisions. Authority and independence checks remain owned by SourceVerifier.

### AmarReasoning
Current synthesis contains an independent lexical relevance function, ranking/filtering, and a raw-record fallback. Target behavior consumes evidence already permitted by the canonical pipeline. No raw fallback may bypass Admission.

### AmarEvidenceFreshnessAnalyzer
No migration. It remains independent and must not be changed to compensate for retrieval or relevance defects.

## 8. Seven-Step Migration
### Step 1 — Data contracts only
Add AdmissionState, AdmittedFinding, AdmissionResult, and the Admission contract. No runtime behavior change. Commit independently and run existing tests.

### Step 2 — Admission implementation
Add AmarEvidenceAdmissionLayer. It receives question + findings, calls score(), applies the approved policy, and separates admitted/rejected. Do not activate it in the runtime path yet. Add unit tests for relevant, unrelated, empty, blank, deterministic, and threshold-boundary cases.

### Step 3 — Orchestrator wiring
Connect the canonical research path to Admission. Prove rejected findings cannot reach downstream accepted-evidence consumption. No parallel relevance authority may be introduced.

### Step 4 — SourceVerifier migration
Make SourceVerifier consume admitted findings. Remove the duplicated relevance gates from verify() and isIndependent(). Preserve authority and independence behavior. Run SourceVerifier regressions.

### Step 5 — External Research migration
Remove constitutional relevance filtering from search(). Return candidates. Any temporary relevance ordering must not reject candidates or determine eligibility. Admission remains the only acceptance decision.

### Step 6 — Reasoning migration
Remove the independent relevance implementation and the raw-record fallback from reasoning. Preserve direct-fact extraction and existing LocalReasoning behavior. Run the complete retrieval regression suite.

### Step 7 — Relevance authority removal
Finalize the boundary so no production consumer independently compares against the threshold to admit/reject evidence. Repository-wide search must demonstrate one constitutional Admission path.

Every step requires: independent commit, targeted regression test, CI green before proceeding.

## 9. Test Plan
### Unit
- relevant evidence admitted
- unrelated evidence rejected
- required facet/entity failures rejected
- threshold boundary
- empty input
- blank evidence
- deterministic repeated evaluation

### Integration
Prove Research → Admission → SourceVerifier is the only accepted-evidence path. A trusted but unrelated source must not become accepted merely because it is authoritative.

### Mandatory regression questions
1. ما عاصمة أمريكا؟
2. كم عمر الفنانة شيرين؟
3. كم عدد الأحرف العربية والإنكليزية؟
4. ما عاصمة العراق؟
5. من أنت؟
6. كيف حالك؟
7. مرحبا
8. كم الوقت الآن؟
9. عدد دول العالم
10. XAUUSD
11. ما سعر الذهب الآن؟

Core invariant: Question A must never receive unrelated Evidence B as accepted evidence.

## 10. CI / APK / Runtime Gates
No merge is permitted unless applicable Stage 1, Stage 2, Stage 10, Stage 11, CodeQL, unit tests, APK build, installation/runtime verification, freshness regression, and evidence-verification regression are green.
A green compile alone is not completion. A green unit suite alone is not completion. Runtime behavior must prove the canonical Admission path.

## 11. Rollback Plan
Primary rollback baseline: e2f799750d6babb801abfbba1b501d7e9fa9cc95

If a migration step breaks existing tests, changes unrelated behavior, creates a second authority, allows rejected evidence downstream, breaks APK/runtime, changes Freshness, or breaks Point 10 binding: stop; do not merge; discard/revert the migration branch as appropriate; compare against the stable baseline; re-audit the failed contract; fix root cause before restarting.
The preferred recovery is to create the next implementation branch from the known stable baseline rather than destructively modifying main.

## 12. Non-Negotiable Constraints
1. AmarLocalReasoning remains a consumer, not an Admission authority.
2. AmarEvidenceFreshnessAnalyzer remains untouched.
3. ResearchFinding remains unchanged during this migration.
4. AmarEvidenceQualityUpstreamState remains untouched.
5. MIN_RELEVANCE_SCORE is not changed without explicit policy review.
6. No hardcoded factual answers.
7. No keyword patch in orchestration as a substitute for Admission.
8. No raw-finding fallback may bypass Admission.
9. No second relevance authority.
10. Every migration step is independently committed and tested.
11. No merge before CI + APK + runtime regression are green.
12. Admission does not authorize unsupported trading outputs.

## 13. Acceptance Criteria
- One canonical Admission authority exists.
- AmarRetrievalRelevanceEngine is implementation, not constitutional authority.
- External Research produces candidates rather than final Admission decisions.
- SourceVerifier no longer independently decides Relevance.
- Reasoning no longer independently decides Relevance.
- No fallback bypasses Admission.
- Freshness remains unchanged.
- ResearchFinding contract remains intact.
- Relevant evidence is admitted deterministically.
- Unrelated evidence is rejected deterministically.
- Rejected evidence cannot reach downstream reasoning as accepted evidence.
- Existing and new regressions are green.
- CI is green.
- APK builds.
- Runtime regression passes.

## 14. Review Gate Before Coding
This document is a design contract only. No implementation activation is authorized until the following are reviewed: AdmittedFinding, AdmissionResult, AmarEvidenceAdmissionContract, single-authority rule, seven-step migration sequence, and rollback policy.
After approval, implement Step 1 only. Do not combine steps into one uncontrolled patch.