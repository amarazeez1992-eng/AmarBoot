# AMARBOT — STAGE 4 — SEARCH ENGINE — FINAL DESIGN

**Status:** Living Document — Editable — Not Constitutional Closure  
**Version:** 4.0  
**Date:** 2026-09-26  
**Reference:** docs/governance/AMAR-AI-MASTER-CONSTITUTION.md  
**Master Constitution Blob SHA:** c8d34cdb140fc47bb883d986f288f6e8ff60987b

> This is a living design reference. It is not constitutional closure.

## Table of Contents

1. Identity and Scope
2. Inputs and Outputs
3. Engine Inventory
4. Query and Ambiguity Processing
5. Search Strategy and Plan
6. Provider Registry and Selection
7. Retrieval and Collection
8. Deduplication and Ranking
9. Search Material Extraction
10. Verification and Integrity
11. Iterative Search
12. Cache
13. Rate Limiting and Coordination
14. Provenance
15. Stage 12 Boundary Contract
16. Stage 23 Specialized Search Contract
17. Stage 15 Boundary
18. Search Quality Metrics
19. Security
20. Privacy and Retention
21. Performance and Scalability
22. Search Sessions
23. Observability and Tracing
24. Provider Circuit Breaker
25. Cost Tracking
26. Explainability and Feedback
27. Institutional Decision Matrix
28. Boundary Matrix
29. Fail-Closed and Recovery
30. Acceptance Criteria
31. Existing Code Relationship
32. Revision History
33. References and Constitutional Note

---

## 1. Identity and Scope

**Stage 4 = Search Engine.**

Core flow:

Query Understanding → Query Expansion → Parallel Search → Source Collection → Deduplication → Ranking → Search Material Extraction → Search Verification → Final Search Synthesis.

Stage 4 owns generic search/retrieval orchestration. It does not own Intent Authority (Stage 2), Memory Authority (Stage 3), Source Authority (Stage 6), Evidence Authority (Stage 7), Advanced Evidence Intelligence (Stage 11), Research Preparation Authority (Stage 12), Market Intelligence (Stage 15), Specialized Search Authority (Stage 23), Decision (Stage 13), or Execution (Stage 14).

Signal ≠ Decision ≠ Execution.

## 2. Inputs and Outputs

### Search Request

Includes, as applicable:

- requestId
- query
- language
- domain
- timeframe
- freshness
- entities
- constraints
- priority
- budget
- timeout
- requested coverage
- provenance requirements

### SearchResultPackage

Includes:

- packageId/packageVersion
- normalized query and variants
- entities and ambiguity state
- selected strategy
- SearchPlan
- provider results
- ranked results
- CandidateSearchMaterial
- verification state
- ProvenanceBundle
- iteration history
- cache metadata
- quality/observability references
- insufficiency state
- timestamps

## 3. Engine Inventory

Core engines/capabilities:

1. Query Understanding Engine
2. Query Expansion Engine
3. Entity Disambiguation Engine
4. Ambiguity Resolution Engine
5. Multi-language Engine
6. Search Strategy Selector
7. Search Plan Builder
8. Provider Registry Engine
9. Parallel Search Engine
10. Source Collection Engine
11. Search Deduplication Engine
12. Search Ranking Engine
13. Provider Quality Scoring Engine
14. Search Material Extraction Engine
15. Search Verification Engine
16. Final Search Synthesis Engine
17. Iterative Search Controller
18. Search Cache Engine
19. Search Rate Policy / Rate Limiter Interface
20. Search Quality Metrics Engine
21. Provenance Engine
22. Search Security & Privacy Guard

Institutional capabilities:

23. Search Observability & Tracing
24. Provider Circuit Breaker
25. Search Cost Tracking
26. Query Rewriting History
27. Search Session Management
28. Search Explainability
29. Search Feedback Capture

Not every capability requires a standalone engine.

## 4. Query and Ambiguity Processing

### 4.1 Query Understanding

Transforms raw input into SearchQueryModel with:

- normalized query
- entities
- constraints
- timeframe
- information classes
- ambiguity indicators
- understanding score

The understanding score is limited to search-query interpretation. It is not Evidence Confidence, Decision Confidence, or Execution Authorization.

Acceptance: invalid or unsafe query structures fail closed.

### 4.2 Query Expansion

Conservative expansion using synonyms, terminology, spelling, domain variants, and language variants. Every expansion is traceable and reproducible.

### 4.3 Entity Disambiguation

Resolves multi-meaning entities using context, domain, timeframe, entity type, aliases, and available semantic context. If competing interpretations remain material, emit AMBIGUOUS_ENTITY.

### 4.4 Ambiguity Resolution

Four paths:

- AUTO_RESOLVE when one interpretation is sufficiently supported.
- MULTI_PATH when multiple interpretations can be searched safely within budget.
- CLARIFICATION_REQUEST when user intent is materially unresolved.
- REJECT when the request cannot be safely interpreted.

No silent assumption may convert material ambiguity into a false certainty.

### 4.5 Multi-language

Supports language detection, translation, transliteration, script normalization, and multilingual expansion. Every variant records source language, target language, transformation type/version, and original-query reference.

## 5. Search Strategy and Plan

### 5.1 Search Strategy Selector

Selects:

- GENERIC_STAGE4
- SPECIALIZED_STAGE23
- EXTERNAL_RESEARCH_STAGE12

Selection considers explicit contract requirement, capability fit, research scope, freshness, language, provider availability, latency, budget, and coverage.

Priority:

1. Explicit contract requirement
2. Capability requirement
3. Research scope
4. Generic sufficiency
5. Operational constraints

Tie-breaker:

1. explicit requirement
2. capability fit
3. lower complexity
4. lower latency
5. lower cost
6. higher provider availability
7. Generic Stage 4 only when no explicit specialized/research requirement remains.

Selector chooses a search route only; it never makes an evidence, trading, financial, decision, or execution judgment.

### 5.2 SearchPlan

Fields include:

- planId
- querySet
- routes
- providers
- budget
- timeout
- priority
- language/domain/timeframe/freshness
- resultLimit
- parallelism
- provenance policy
- cache policy
- rate policy
- schemaVersion

Invalid provider/route/budget/timeout/schema/provenance requirements prevent execution.

## 6. Provider Registry and Selection

### 6.1 Provider Registry

Provider record includes:

- providerId
- providerType
- capabilities
- supportedLanguages
- supportedDomains
- supportedSearchTypes
- rateLimits
- qualityTier
- availability
- API/schema version
- authentication-reference
- lifecycle state

Lifecycle:

REGISTERED → ACTIVE → DEGRADED → DEPRECATED → DISABLED

Operations: register, validate, update, activate, degrade, deprecate, disable.

Only registered providers may execute.

### 6.2 Provider Quality

Measures operational retrieval performance:

- success rate
- latency
- timeout rate
- extraction success
- duplicate behavior
- freshness performance where measurable
- query-class performance

Updates collect events after search attempts and aggregate using bounded rolling/time-decayed statistics. Provider quality is not Evidence Quality.

New providers may have INSUFFICIENT_HISTORY rather than an artificial score. API/schema changes may create a new performance cohort.

## 7. Retrieval and Collection

### 7.1 Parallel Search

Uses bounded parallelism, provider isolation, plan budget, and deadlines. One provider may fail without invalidating the whole plan when the minimum contract remains satisfied.

### 7.2 Source Collection

Collects result identity, provider reference, retrieval timestamp, URL/reference, metadata, and retrieval status. Stage 6 remains Source Authority.

### 7.3 Time-bounded Collection

SearchPlan defines total timeout, provider timeout, collection deadline, and retry budget. On deadline: COLLECTION_TIMEOUT. Partial results must expose coverage/insufficiency.

## 8. Deduplication and Ranking

### 8.1 Search Deduplication

Uses canonical/normalized URLs, content fingerprints where available, provider identity, and result identity.

Search-result deduplication is not Stage 7 Evidence Duplicate Detection.

### 8.2 Search Ranking

Ranks relevance using appropriate lexical/semantic/entity/query-coverage/freshness/domain factors.

Search Ranking ≠ Evidence Ranking.

### 8.3 Provider Quality

Provider quality may inform provider selection, but never becomes evidence authority or evidence confidence.

## 9. Search Material Extraction

The official name is **Search Material Extraction Engine**, not Evidence Extraction Engine.

Output is **CandidateSearchMaterial**, never automatically final Evidence.

May include title, text fragment, metadata, source reference, location, extraction timestamp/method, content fingerprint, and paywall status.

### Paywall

Recognize accessible, partial, paywalled, authentication-required, restricted, and unavailable states. Never invent inaccessible content or bypass protections. Snippets/metadata remain explicitly limited.

## 10. Verification and Integrity

### 10.1 Search Verification

Checks result identity, source reference, timestamp, provenance completeness, fingerprint consistency, package integrity, and schema validity.

### 10.2 Cryptographic Verification

When a fingerprint/signature exists:

- compute/compare fingerprint
- verify signature when applicable
- reject mismatches

Failure: CRYPTOGRAPHIC_INTEGRITY_FAILURE.

Stage 4 verification is handoff/package integrity, not Stage 7 Evidence Verification Authority or Stage 11 advanced revalidation.

## 11. Iterative Search

Iterative Search corrects retrieval when results are insufficient, an entity/information class is missing, ranking/coverage is poor, source diversity is low, extraction is incomplete, ambiguity persists, or a provider/route fails.

Each iteration records:

- previous query/providers/results
- detected deficiency
- correction strategy
- new query/providers/results
- delta/new information
- outcome

Within the current search session it learns to avoid repeated queries, providers, routes, and known failed paths. This is not model training and does not write Stage 3 Memory.

Default policy may allow up to three additional iterations after initial retrieval, subject to a configurable hard upper bound. Stop when requirements are met, no meaningful expansion exists, budget/timeout is exhausted, all providers fail, or no new path/information exists.

## 12. Cache

Cache key includes normalized query, constraints, provider/route, language, domain, timeframe, freshness, and relevant schema/config versions.

TTL is policy-driven. Live/current information uses very short TTL or disabled cache.

Invalidate on TTL expiry, provider/config/schema changes, freshness changes, explicit invalidation, provenance invalidation, or detected content change.

Cache ≠ Stage 3 Memory.

### Cache and Iterative Search

Valid compatible cache hits may be reused. Bypass cache when the iteration requires missing information, materially rewrites the query, raises freshness requirements, changes route/provider, or invalidates prior material.

After each iteration, validate new results, attach provenance, and cache only eligible material. Record cache hit/bypass reason and new-information delta. No-new-information stops the loop.

## 13. Rate Limiting and Coordination

Stage 1 owns general infrastructure rate limiting/network controls.

Stage 4 owns search-specific rate policy and coordination interface.

Global coordination covers provider quotas, route limits, request budgets, concurrency, burst limits, and retry budgets.

Actions:

ALLOW / DELAY / REJECT

No uncontrolled concurrency or infinite retry.

Rate Limiter ≠ Circuit Breaker.

## 14. Provenance

### 14.1 ProvenanceRecord

Logical schema:

- provenanceId
- requestId
- parentProvenanceId
- sourceRef
- providerId/providerVersion
- query/queryVariantId
- searchPlanId/routeId
- retrievalTimestamp
- retrievalAttempt
- sourceUrlOrReference
- contentFingerprint/fingerprintAlgorithm
- transformationChain
- extractionEngineVersion
- verificationState
- schemaVersion/packageVersion

Required for delivered material: provenanceId, requestId, providerId, query, timestamp, sourceRef, fingerprint when available, transformation history, extraction version, verification state, schema/package version.

### 14.2 Chain

User Query → Query Transformation → Search Plan → Provider → Result → Material → Verification → Synthesis.

### 14.3 Query

Provenance is queryable by provenanceId, requestId, sourceRef, providerId, materialId, and packageId.

Stage 4 owns retrieval provenance. Stage 7 owns evidence authority. Stage 11 consumes lineage for advanced evidence intelligence.

## 15. Stage 12 Boundary Contract

Stage 12 owns External Research / Research Preparation.

Stage 4 owns Search Retrieval Infrastructure.

### Direction

If a request is inherently research workflow:

User/Stage 2/Agent → Stage 12 → ExternalResearchRequest → Stage 4 → ResearchSearchPackage → Stage 12.

If a request is ordinary search:

User/Stage 2/Agent → Stage 4 → search.

If Stage 4 discovers that a request has become a research workflow, it hands off through the Stage 12 contract rather than recursively reprocessing the same request.

Requests carry requestId, parentRequestId, initiatingAuthority, currentStage, recursionDepth, and visitedStages. Repeated stage recursion without a new capability is blocked as RECURSIVE_REQUEST_BLOCKED.

### Contract

ExternalResearchRequest may contain research query, scope, source classes, language, timeframe, freshness, domain, priority, coverage, and provider restrictions.

ResearchSearchPackage may contain original query, SearchQueryModel, SearchPlan reference, ranked results, source references, CandidateSearchMaterial, provenance, timestamps, coverage, insufficiency, conflicts, iteration history, and package version.

### Version Negotiation

Both sides expose contractVersion, schemaVersion, minimumSupportedVersion, and capabilities.

NEGOTIATE → ACCEPT / DOWNGRADE / REJECT.

Incompatible contracts fail closed.

Stage 12 does not own provider orchestration, generic ranking, search dedup, cache authority, or SearchPlan/rate policy.

## 16. Stage 23 Specialized Search Contract

Technical design decision recorded here:

> **Stage 23 = Specialized Search Engines.**

This is a design decision and does not by itself amend the Constitution or stage map.

Stage 4 sends SpecializedSearchRequest containing query, entities, domain, timeframe, language, constraints, budget, provenance requirements, and contractVersion.

Stage 23 returns SpecializedSearchResultPackage.

Stage 4 integrates specialized results through collection → dedup → ranking → material extraction → verification → synthesis.

Fallback when Stage 23 is unavailable/incompatible/timed out/disabled:

1. alternative registered capable provider/route
2. Generic Stage 4 when semantically sufficient
3. fail closed if no valid path remains

No invented specialized substitute.

## 17. Stage 15 Boundary

Stage 4 searches for market information.

Stage 15 owns live signals and market-intelligence/signal processing.

Search for market information ≠ Market Signal Intelligence.

Stage 4 does not generate trading signals. Stage 15 does not become a replacement Search Engine.

## 18. Search Quality Metrics

Measures include:

- Query Understanding Accuracy
- Expansion Precision
- Retrieval Recall
- Ranking NDCG
- Extraction Fidelity
- Synthesis Fidelity
- provider performance
- latency
- failure rate
- cache effectiveness

Evaluation uses offline golden datasets, integration evaluation, and runtime observability.

Metric events include stage/engine version, query class, provider, timestamp, config version, and dataset version.

No metric is accepted as a fact without a benchmark. Quality metrics are engineering measurements, not decision confidence.

## 19. Security

Controls include:

- SSRF protection against private/localhost/metadata/internal targets
- query/provider parameter injection protection
- content-to-prompt injection isolation
- credential isolation
- response/request size limits
- concurrency/time/retry limits
- malicious result handling

External result content is untrusted data, not system instructions.

## 20. Privacy and Retention

Store only what is required for search function, auditability, cache policy, and provenance.

Retention applies separately to requests, results, provenance, cache data, sessions, and operational metrics.

Support TTL, explicit deletion, invalidation, and retention expiration.

Search data does not become persistent Stage 3 Memory automatically.

## 21. Performance and Scalability

Design supports bounded parallelism, provider isolation, timeouts, backpressure, caching, retry budgets, graceful degradation, and horizontal/provider-level scalability.

Measure:

- p50/p95/p99 latency
- throughput
- concurrent searches
- provider latency
- queue latency
- extraction latency
- iteration latency
- cache latency

Numerical targets require real benchmarks and are configuration targets, not constitutional constants.

## 22. Search Sessions

Search Session supports bounded multi-step searches with:

- sessionId
- request chain
- active search context
- previous results
- constraints
- iteration history
- provenance references

Search Session ≠ Stage 3 Memory.

Persistent memory requires an explicit Stage 3 contract.

## 23. Observability and Tracing

Accepted as institutional capability.

Every search should support:

- correlationId
- requestId
- searchId
- provider spans
- iteration spans
- cache spans
- timing
- failures
- resource events

OpenTelemetry-compatible instrumentation is supported at implementation time.

Observability ≠ Provenance:

- Observability = what happened during execution.
- Provenance = where material came from and how it was transformed.

## 24. Provider Circuit Breaker

Accepted.

States:

CLOSED → OPEN → HALF_OPEN → CLOSED.

Triggered by policy-defined health/failure conditions and cooldown.

Circuit Breaker isolates unhealthy providers. It does not decide relevance, evidence quality, or strategy quality.

## 25. Cost Tracking

Accepted.

Track:

- provider
- request/route
- estimated/actual cost
- budget
- remaining budget
- cost unit
- timestamp

Budget breach produces BUDGET_EXCEEDED and stops or falls back according to SearchPlan.

General accounting infrastructure may remain outside Stage 4; Stage 4 owns search cost policy/tracking.

## 26. Explainability and Feedback

### 26.1 Query Rewriting History

Record original/revised query, reason, transformation type, engine version, timestamp, and iterationId.

This is provenance/iteration history, not persistent Memory.

### 26.2 Search Explainability

Report may include:

- normalized query
- entities
- ambiguity resolution
- selected strategy
- providers
- SearchPlan
- ranking factors
- iterations
- cache decisions
- failures
- provenance references

Search Explainability ≠ Decision Explainability.

### 26.3 Feedback Capture

Capture explicit user/result feedback such as relevance corrections, result rejection, clicks where appropriate, and query corrections.

Feedback may feed metrics/evaluation datasets.

No autonomous model retraining, permanent preference mutation, or uncontrolled strategy mutation is part of Stage 4 core.

## 27. Institutional Decision Matrix

| Proposal | Decision | Placement |
|---|---|---|
| Observability & Tracing | ACCEPT | Stage 4 capability/infrastructure support |
| Provider Circuit Breaker | ACCEPT | Provider reliability |
| Cost Tracking | ACCEPT | Search cost policy |
| A/B Testing Framework | REJECT as Core | External experimentation capability |
| Query Rewriting History | ACCEPT | Provenance/Iteration |
| Search Session Management | ACCEPT | Stage 4 Session |
| Content Freshness Verification | ACCEPT as Contract | Stage 6 authority + Stage 4 cache |
| Multi-tenant Support | ACCEPT as Isolation | Stage 4 isolation + Stage 1 identity infrastructure |
| Search Explainability | ACCEPT | Stage 4 |
| Feedback Loop | ACCEPT as Capture | Metrics/Evaluation workflow |

A/B testing remains outside the core execution architecture to avoid turning Stage 4 into an experimentation platform.

## 28. Boundary Matrix

| Capability | Owner |
|---|---|
| Intent Understanding | Stage 2 |
| Search Query Understanding | Stage 4 |
| Memory | Stage 3 |
| Source Identity/Properties | Stage 6 |
| Evidence Authority | Stage 7 |
| Advanced Evidence Intelligence | Stage 11 |
| Generic Search | Stage 4 |
| External Research Preparation | Stage 12 |
| Specialized Search | Stage 23 |
| Market Intelligence/Signals | Stage 15 |
| Decision | Stage 13 |
| Execution | Stage 14 |
| Infrastructure Rate Limiting | Stage 1 |
| Search Rate Policy | Stage 4 |
| Retrieval Provenance | Stage 4 |
| Persistent Memory | Stage 3 |

## 29. Fail-Closed and Recovery

Fail closed on:

- invalid query
- unsafe unresolved ambiguity
- invalid SearchPlan
- unregistered provider
- incompatible contract
- cryptographic integrity failure
- unavailable required route
- exhausted budget
- timeout with insufficient coverage
- all valid providers failing
- provenance requirement failure
- security violation

Recovery is bounded:

Detect → Isolate → Retry/Failover if permitted → Revalidate → Audit.

When reliable evidence is insufficient:

> **الأدلة غير كافية لإصدار نتيجة موثوقة**

## 30. Acceptance Criteria

Every required capability must have:

- definition
- owner
- inputs
- outputs
- engine/capability placement
- boundary
- failure path
- fail-closed behavior
- provenance
- security/privacy consideration
- testability
- quality metric where measurable

Design completeness does not mean implementation, CI success, user acceptance, or constitutional closure.

## 31. Existing Code Relationship

No existing component is automatically canonicalized by this document.

Previously identified relevant components include:

- AmarResearchEngine.kt
- AmarRetrievalRelevanceEngine.kt
- AmarDeepResearchOrchestrator.kt
- AmarEvidenceRanker.kt
- AmarSourceVerifier.kt
- AmarIntentUnderstanding.kt

Implementation mapping requires caller analysis, boundary verification, tests, and explicit execution authorization.

No move, delete, rename, or replacement is authorized by this document.

## 32. Revision History

### R1 — Initial
Initial Stage 4 Search Engine design with the nine constitutional search capabilities.

### R2 — Seven Gap Closure
Added/defined SearchPlan, Stage 12 boundary contract, Stage 23 contract, Iterative Search, Search Cache, Rate Limiting, and Search Quality Metrics.

### R3 — Twenty-Two Gap Closure
Added Provider Registry, Ambiguity Resolution, Provenance, Search Strategy Selector, Entity Disambiguation, Multi-language processing, Provider Quality, global rate coordination, Search Material Extraction naming, query understanding score, time-bounded collection, paywall handling, cryptographic verification, bounded iteration learning, contract version negotiation, Stage 23 fallback, Stage 15 boundary, persistence/retention, security, performance, and privacy.

### R4 — Final
Closed five remaining design gaps and added/clarified institutional capabilities:

- Observability & Tracing
- Provider Circuit Breaker
- Cost Tracking
- Query Rewriting History
- Search Session Management
- Freshness Contract
- Multi-tenant Isolation
- Search Explainability
- Feedback Capture

A/B Testing was rejected as a Core Stage 4 capability.

## 33. References and Constitutional Note

### References

| Reference | Purpose | Blob SHA |
|---|---|---|
| docs/governance/AMAR-AI-MASTER-CONSTITUTION.md | Master Source of Truth | c8d34cdb140fc47bb883d986f288f6e8ff60987b |
| docs/governance/AMAR-STAGES-MAP.md | Stage organization/map reference | verify from repository at implementation time |
| docs/verification/STAGE3-FINAL-DESIGN.md | Stage 3 Memory Authority boundary | 368db7ca2ae9ae628926377bd6206522078d987b |
| docs/engineering/corrections/ERR-RETRIEVAL-003-query-relevance-gate.md | Retrieval relevance reference | verify from repository at implementation time |

### Constitutional Note

> هذه الوثيقة مرجع تصميمي حي.
> ليست إغلاقًا دستوريًا.
> قابلة للتعديل والإضافة والحذف.
> الالتزام بها غير إلزامي أثناء التنفيذ.
> أي تعديل يُسجّل عبر commit.

The Master Constitution remains the Single Source of Truth. Any future constitutional change follows the Constitution's change-control process.

**END — STAGE 4 FINAL DESIGN REVISION 4**
