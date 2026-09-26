# AMARBOT — STAGE 3
# MEMORY AUTHORITY — FINAL DESIGN

Status: Design Locked — Implementation Pending
Stage: 3 — Memory Authority
Engines: 49
Internal Additions: 654
Discovery: Complete
Design: Approved
Code: Not Started
Execution: Not Started
Closure: Pending

---

## 1. Vision

Stage 3 = Memory Authority.

Manages Identity, State, History, Storage, Retrieval, Versions, Snapshots, Events, Provenance, Lineage, Dependencies, Conflicts, Uncertainty, Lifecycle, Policies, Integrity, Privacy, Health, Observability, Backup/Recovery, Quarantine, Branching, Counterfactual, Explainability, Pattern Discovery, Consistency Recovery.

Does not own Evidence Truth, Research, Decision, Execution, Broker, Market Signal.

**Rule: Memory ≠ Evidence ≠ Decision ≠ Execution.**

## 2. Boundaries with Stages 11–16

- Stage 11 Evidence Authority — Stage 3 provides Context only.
- Stage 12 Research Authority — Stage 3 provides Context only.
- Stage 13 Decision Authority — Stage 3 provides State/History.
- Stage 14 Execution Authority — Stage 3 no execution authority.
- Stage 15 Signal Authority — Stage 3 provides Context only.
- Stage 16 Bot Coordination — Stage 3 remains Memory Authority.

## 3. Engines and Internal Additions

### E1 — Memory Identity & Integrity
**Authority:** Memory Identity & Integrity Authority
**Internal Additions (23):**
1. Canonical Memory Representation
2. Logical Memory Identity
3. Stable Memory ID
4. Version Identity
5. Content Fingerprint
6. Metadata Fingerprint
7. Fingerprint Comparison
8. Identity Comparison
9. Collision Detection
10. Collision Resolution
11. Integrity Hashing
12. Integrity Verification
13. Mutation Detection
14. Identity/Version Consistency
15. Referential Identity Validation
16. Integrity Root
17. Integrity Metadata
18. Tamper-Evidence Record
19. Deterministic Identity Generation
20. Concurrent Identity Generation
21. Hash Algorithm Agility
22. Identity Persistence Across Migration
23. Identity Migration Mapping

### E2 — Memory Schema & Evolution
**Authority:** Memory Schema Authority
**Internal Additions (22):**
1. Schema Definition
2. Schema Versioning
3. Field Contract
4. Required/Optional Field Rules
5. Type Compatibility
6. Backward Compatibility
7. Forward Compatibility
8. Schema Validation
9. Schema Diff
10. Migration Planning
11. Migration Execution Contract
12. Migration Verification
13. Field Deprecation
14. Field Introduction
15. Field Transformation
16. Schema Rollback
17. Legacy Schema Reading
18. Event Schema Compatibility
19. Snapshot Schema Compatibility
20. Schema Release Metadata
21. Migration Mapping
22. Invalid Schema Detection

### E3 — Memory Store
**Authority:** Memory Store Authority
**Internal Additions (22):**
1. Persistence
2. Atomic Write
3. Atomic Read
4. Version-aware Storage
5. Transaction Boundary
6. Idempotent Write
7. Update
8. Archive
9. Restore
10. Delete
11. Snapshot Storage
12. Event Storage
13. Corruption Handling
14. Recovery
15. Storage Validation
16. Duplicate Handling
17. Read Consistency
18. Write Consistency
19. Storage Isolation
20. Capacity Handling
21. Concurrent Access Control
22. Storage Audit

### E4 — Memory Retrieval — Lexical
**Authority:** Lexical Retrieval Authority
**Internal Additions (20):**
1. Query Normalization
2. Tokenization
3. Exact Match
4. Phrase Match
5. Token Match
6. Field Search
7. Filtering
8. Ranking
9. Deduplication
10. Arabic Normalization
11. Candidate Threshold
12. Retrieval Audit
13. Language-aware Normalization
14. Deterministic Tie-Breaking
15. Empty-result Handling
16. Retrieval Safety Constraints
17. Query Expansion — lexical only
18. Match Threshold
19. Result Limiting
20. Retrieval Metadata

### E5 — Memory Tiers
**Authority:** Memory Tier Management Authority
**Internal Additions (20):**
1. Tier Definition
2. Tier Membership
3. Tier Eligibility
4. Promotion Detection
5. Demotion Detection
6. Promotion Proposal
7. Demotion Proposal
8. Tier Transition
9. Transition Validation
10. Tier Capacity Management
11. Cross-Tier Retrieval
12. Tier-specific Retention
13. Tier-specific Access Policy
14. Tier Metadata
15. Transition History
16. Tier Consistency
17. Tier Recovery
18. Tier Migration
19. Tier Protection
20. Tier-aware Ranking

### E6 — TTL & Expiry
**Authority:** Memory Expiry Authority
**Internal Additions (20):**
1. TTL Definition
2. Expiry Timestamp Management
3. Expiry State Detection
4. Expiry Policy Resolution
5. Absolute TTL
6. Relative TTL
7. Expiry Evaluation
8. Expiry Transition
9. Expiry Extension Contract
10. Expiry Override Policy
11. Protected Memory Expiry
12. Tier-aware TTL Policy
13. Expiry Grace Handling
14. Expiry Audit
15. Expired-memory Retrieval Policy
16. Expiry/Reactivation Contract
17. Clock Consistency
18. TTL Versioning
19. Expiry Determinism
20. Expiry Recovery

### E7 — Concurrency Safety
**Authority:** Memory Concurrency Authority
**Internal Additions (23):**
1. Atomic Read/Write
2. Transaction Boundary
3. Optimistic Concurrency
4. Pessimistic Lock Contract
5. Version Check
6. Compare-and-Swap
7. Concurrent Update Detection
8. Race Detection
9. Deterministic Tie-Breaking
10. Idempotent Operations
11. Snapshot Isolation
12. Read Isolation
13. Write Isolation
14. Event Ordering
15. Conflict Preservation
16. Retry Contract
17. Deadlock Avoidance Contract
18. Cancellation Safety
19. Partial Operation Recovery
20. Concurrency Stress Testing
21. Identity Generation Lock
22. Snapshot Consistency
23. Cross-Engine Mutation Coordination

### E8 — Access & Isolation
**Authority:** Memory Access & Isolation Authority
**Internal Additions (20):**
1. Identity Scope
2. User Isolation
3. Bot Isolation
4. Session Isolation
5. Task Scope
6. Tenant/Domain Scope
7. Access Context
8. Read Authorization
9. Write Authorization
10. Delete Authorization
11. Cross-Scope Detection
12. Scope Filtering
13. Sensitive Memory Classification
14. Protected Memory Access
15. Delegated Access Contract
16. Access Expiry
17. Access Audit
18. Isolation Verification
19. Isolation Testing
20. Boundary Violation Detection

### E9 — Event Ledger / Audit Authority
**Authority:** Sole Audit Authority inside Stage 3
**Internal Additions (22):**
1. Event Definition
2. Event ID
3. Append-only Persistence
4. Event Ordering
5. Event Schema Versioning
6. Event Type Registry
7. Event Payload Contract
8. Actor/Context Attribution
9. Previous Version Reference
10. Resulting Version Reference
11. Event Timestamp
12. Event Integrity Metadata
13. Event Chain Reference
14. Event Query
15. Event Filtering
16. Event Replay Input
17. Event Retention Policy
18. Event Corruption Detection
19. Event Compatibility
20. Event Deduplication/Idempotency
21. Event Audit
22. Historical Event Immutability

### E10 — Point-in-Time Retrieval
**Authority:** Internal Temporal Retrieval Authority
**Internal Additions (20):**
1. As-of Query
2. Effective-Time Handling
3. Valid-Time Handling
4. Version Selection
5. Snapshot Selection
6. Event Boundary Resolution
7. Historical State Reconstruction Interface
8. Temporal Ordering
9. Historical Consistency Check
10. Point-in-Time Integrity Verification
11. Version Pinning
12. Historical Access Policy
13. Time Precision Contract
14. Boundary Timestamp Handling
15. Missing-History Detection
16. Historical Retrieval Audit
17. Deterministic Reconstruction Request
18. Historical Schema Compatibility
19. Snapshot/Event Reconciliation
20. Point-in-Time Failure Handling

### E11 — Retrieval Index Engine
**Authority:** Retrieval Index Authority
**Internal Additions (18):**
1. Index Definition
2. Index Building
3. Incremental Index Update
4. Full Rebuild
5. Partial Rebuild
6. Index Integrity Verification
7. Corruption Detection
8. Corruption Isolation
9. Index Versioning
10. Index Migration
11. Index Compatibility
12. Index Validation
13. Index Health
14. Performance Monitoring
15. Rebuild Trigger
16. Rebuild Audit
17. Deterministic Build
18. Recovery

### E12 — Time Authority
**Authority:** Memory Time Authority
**Internal Additions (13):**
1. Clock Source
2. Clock Source Selection
3. Authoritative Timestamp
4. Timezone Handling
5. UTC/Canonical Representation
6. Clock Consistency Check
7. Drift Detection
8. Time Precision Contract
9. Timestamp Validation
10. Monotonicity Check
11. Source Reliability Metadata
12. Time Failure Handling
13. Recovery

### E13 — Historical Access Policy
**Authority:** Historical Memory Access Policy Authority
**Internal Additions (12):**
1. Historical Access Contract
2. As-of Permission Evaluation
3. Historical Role Mapping
4. Historical Scope Rules
5. Historical Ownership Mapping
6. Historical Delegation
7. Historical Policy Version
8. Access Boundary Evaluation
9. Temporal Permission Resolution
10. Historical Access Audit
11. Fail-Closed Handling
12. Policy Conflict Handling

### E14 — Snapshot & Versioning
**Authority:** Memory Version & Snapshot Authority
**Internal Additions (14):**
1. Snapshot Creation
2. Snapshot Identity/Reference Management
3. Version Creation/Tracking
4. Version History Management
5. Snapshot Integrity Association
6. Snapshot Metadata Management
7. Restore Coordination
8. Version Comparison
9. Snapshot Diff
10. Snapshot Retention
11. Snapshot/Event Relationship Management
12. Snapshot/Schema Version Association
13. Restore Validation
14. Snapshot Lifecycle State Management

### E15 — Provenance Chain
**Authority:** Memory Provenance Authority
**Internal Additions (11):**
1. Origin Reference
2. Source Reference
3. Transformation Step
4. Actor Reference
5. Creation Event Reference
6. Mutation Event Reference
7. Parent Provenance Reference
8. Provenance Chain Node
9. Provenance Chain Link
10. Provenance Integrity Marker
11. Provenance Completeness State

### E16 — Lineage
**Authority:** Memory Lineage Authority
**Internal Additions (11):**
1. Lineage Node
2. Parent Relation
3. Child Relation
4. Merge Relation
5. Split Relation
6. Derived Relation
7. Superseded Relation
8. Version Lineage
9. Memory Lineage Reference
10. Lineage Integrity Marker
11. Lineage State

### E17 — Referential Integrity & Orphan Detection
**Authority:** Memory Referential Integrity Authority
**Internal Additions (11):**
1. Reference Validator
2. Reference Type
3. Target Identity
4. Reference Status
5. Broken Reference Marker
6. Orphan Marker
7. Integrity Scan
8. Integrity Violation
9. Repair Candidate
10. Quarantine Candidate
11. Referential Integrity State

### E18 — Memory Conflict Detection
**Authority:** Memory Conflict Detection Authority
**Internal Additions (11):**
1. Conflict Detector
2. Conflict Type
3. Conflict Pair
4. Conflict Group
5. Conflict State
6. Temporal Conflict
7. Value Conflict
8. Structural Conflict
9. Version Conflict
10. Unresolved Conflict Marker
11. Conflict Reference

### E19 — Memory Dependency Graph
**Authority:** Memory Dependency Graph Authority — Dependency Layer داخل E29
**Internal Additions (11):**
1. Dependency Node
2. Dependency Edge
3. Dependency Type
4. Parent Dependency
5. Child Dependency
6. Dependency Path
7. Dependency Cycle Marker
8. Dependency Integrity State
9. Impact Reference
10. Dependency Version Reference
11. Dependency Graph State

### E20 — Migration Engine
**Authority:** Memory Migration Execution Authority
**Internal Additions (11):**
1. Migration Plan
2. Migration Step
3. Source Schema Reference
4. Target Schema Reference
5. Migration Mapping
6. Field Transformation
7. Compatibility Check
8. Migration State
9. Migration Result
10. Migration Failure
11. Migration Audit Reference

### E21 — Lifecycle Engine
**Authority:** Memory Lifecycle Authority
**Internal Additions (11):**
1. Lifecycle State
2. State Transition
3. Transition Rule
4. Creation State
5. Active State
6. Archived State
7. Retired State
8. Protected State
9. Lifecycle Eligibility
10. Transition Event
11. Lifecycle Integrity State

### E22 — Decay & Importance
**Authority:** Memory Importance Evaluation Authority
**Internal Additions (11):**
1. Base Importance
2. Time Decay
3. Usage Weight
4. Recency Weight
5. Context Weight
6. Protection Modifier
7. Importance State
8. Decay State
9. Decay Evaluation
10. Importance Update
11. Decay Metadata

### E23 — Consolidation Engine
**Authority:** Memory Consolidation Authority
**Internal Additions (11):**
1. Candidate Group
2. Similarity Context
3. Consolidation Plan
4. Merge Mapping
5. Retained Identity
6. Source Mapping
7. Lineage Mapping
8. Conflict Preservation
9. Consolidation State
10. Consolidation Result
11. Consolidation Event

### E24 — Semantic Retrieval
**Authority:** Semantic Retrieval Authority
**Internal Additions (11):**
1. Semantic Representation
2. Embedding Reference
3. Similarity Measure
4. Candidate Generation
5. Semantic Ranking
6. Semantic Threshold
7. Context Filter
8. Access Filter
9. Deduplication Reference
10. Retrieval Metadata
11. Semantic Retrieval State

### E25 — Contextual Retrieval
**Authority:** Contextual Retrieval Authority
**Internal Additions (11):**
1. Context Representation
2. Context Profile
3. Context Matching
4. Session Context Resolver
5. Task Context Resolver
6. Temporal Context Resolver
7. Context Weighting
8. Context Filter
9. Context Relevance Score
10. Candidate Enrichment
11. Context Retrieval State

### E26 — Multi-Hop Retrieval
**Authority:** Multi-Hop Retrieval Authority
**Internal Additions (11):**
1. Hop Resolver
2. Starting Node
3. Traversal Path
4. Relationship Selector
5. Hop Limit Controller
6. Cycle Detector
7. Access Filter
8. Path Ranking
9. Candidate Expansion
10. Traversal State
11. Path Audit Reference

### E27 — Temporal Retrieval
**Authority:** Public Temporal Retrieval Authority
**Internal Additions (11):**
1. Temporal Query
2. Valid Time Resolver
3. Transaction Time Resolver
4. Time Range Filter
5. Temporal Ordering
6. Version Resolver
7. Historical State Resolver
8. Late Event Handler
9. Archived Memory Resolver
10. Temporal Consistency State
11. Temporal Retrieval State

### E28 — Adaptive Retrieval
**Authority:** Retrieval Coordination Authority
**Internal Additions (11):**
1. Query Classifier
2. Strategy Selector
3. Retrieval Profile
4. Result Quality Evaluator
5. Strategy Adaptation
6. Fallback Controller
7. Retrieval Fusion Controller
8. Candidate Budget
9. Adaptation State
10. Strategy History
11. Retrieval Decision Trace

### E29 — Memory Graph Engine
**Authority:** Memory Graph Authority
**Internal Additions (11):**
1. Graph Node
2. Graph Edge
3. Relationship Type
4. Edge Metadata
5. Graph Version
6. Node Resolver
7. Edge Resolver
8. Graph Traversal Interface
9. Graph Integrity State
10. Graph Update Plan
11. Graph Query State

### E30 — Memory Learning Layer
**Authority:** Memory Learning Proposal Authority
**Internal Additions (11):**
1. Learning Signal
2. Learning Observation
3. Pattern Candidate
4. Usage Pattern
5. Retrieval Pattern
6. Feedback Pattern
7. Learning Proposal
8. Learning State
9. Learning Window
10. Learning Evaluation
11. Learning Audit Reference

### E31 — State Reconstruction
**Authority:** Memory State Reconstruction Authority
**Internal Additions (11):**
1. Reconstruction Request
2. Target State
3. Event Sequence
4. Snapshot Base
5. Replay Boundary
6. Version Resolver
7. Schema Resolver
8. State Applicator
9. Reconstruction Trace
10. Completeness State
11. Reconstruction Result

### E32 — Replay Engine
**Authority:** Memory Replay Authority
**Internal Additions (11):**
1. Replay Request
2. Replay Session
3. Event Cursor
4. Event Applicator
5. Starting State
6. Event Ordering
7. Replay Checkpoint
8. Replay Trace
9. Divergence Detector
10. Replay Result
11. Replay State

### E33 — Governance & Policy Engine
**Authority:** Memory Governance Authority
**Internal Additions (11):**
1. Governance Rule
2. Policy Definition
3. Authority Definition
4. Scope Definition
5. Constraint
6. Rule Priority
7. Applicability Condition
8. Governance Version
9. Change Proposal
10. Violation State
11. Governance Record

### E34 — Memory Policy Engine
**Authority:** Memory Policy Execution Authority
**Internal Additions (11):**
1. Policy Definition
2. Policy Scope
3. Policy Condition
4. Policy Evaluation
5. Policy Constraint
6. Policy Action
7. Policy Priority
8. Policy Version
9. Applicability State
10. Policy Decision Trace
11. Policy Violation State

### E35 — Encryption & Privacy
**Authority:** Memory Data Protection Authority
**Internal Additions (11):**
1. Encryption Profile
2. Encryption Algorithm Reference
3. Key Reference
4. Key Version
5. Encryption State
6. Decryption Request
7. Privacy Classification
8. Data Protection Rule
9. Access Protection Check
10. Protection Failure State
11. Encryption Audit Reference

### E36 — Memory Health & Performance
**Authority:** Memory Health Evaluation Authority
**Internal Additions (11):**
1. Health Check
2. Health Indicator
3. Performance Metric
4. Latency Metric
5. Error Metric
6. Capacity Metric
7. Resource Metric
8. Health State
9. Performance State
10. Degradation Signal
11. Diagnostic Snapshot

### E37 — Memory Observability
**Authority:** Memory Operational Observability Authority
**Internal Additions (11):**
1. Metric Collector
2. Trace Collector
3. Operational Event Collector
4. Correlation ID
5. Trace Span
6. Telemetry Record
7. Observability Pipeline
8. Sampling Policy
9. Telemetry Retention
10. Diagnostic View
11. Observability State

### E38 — Backup & Recovery
**Authority:** Memory Backup & Recovery Authority
**Internal Additions (11):**
1. Backup Plan
2. Backup Artifact
3. Backup Version
4. Backup Manifest
5. Integrity Reference
6. Encryption Reference
7. Recovery Plan
8. Recovery Point
9. Restore State
10. Recovery Validation
11. Recovery Event

### E39 — Quarantine Engine
**Authority:** Memory Quarantine Authority
**Internal Additions (11):**
1. Quarantine Record
2. Quarantine Reason
3. Trigger
4. Original Reference
5. Isolation State
6. Review State
7. Release Eligibility
8. Recovery Candidate
9. Quarantine Policy
10. Quarantine Version
11. Quarantine Event

### E40 — Retrieval Transparency & Audit
**Authority:** Retrieval Transparency Authority
**Internal Additions (11):**
1. Retrieval Transparency Record
2. Retrieval Decision Trace
3. Candidate Selection Trace
4. Ranking Explanation
5. Exclusion Reason Registry
6. Missing Result Indicator
7. Insufficient Retrieval Marker
8. Strategy Disclosure
9. Retrieval Path Reference
10. Provenance/Lineage Link
11. Transparency Validation

### E41 — Memory Explainability
**Authority:** Memory Explainability Authority
**Internal Additions (11):**
1. Memory Explanation Model
2. Origin Explanation
3. Version Explanation
4. Relationship Explanation
5. Retrieval Relevance Explanation
6. Lifecycle Explanation
7. Conflict Explanation
8. Provenance Explanation
9. Lineage Explanation
10. Uncertainty Explanation
11. Explanation Consistency Validator

### E42 — Counterfactual Memory Space
**Authority:** Counterfactual Space Authority
**Internal Additions (11):**
1. Counterfactual Space Manager
2. Base Version Pinning
3. Scenario State
4. Hypothetical Event Layer
5. Scenario Isolation
6. Scenario Diff
7. Scenario Dependency Mapping
8. Scenario Provenance
9. Scenario Lifecycle
10. Scenario Cleanup
11. Scenario Audit Record

### E43 — Memory Branching
**Authority:** Memory Branch Authority
**Internal Additions (11):**
1. Branch Manager
2. Branch Identity
3. Parent Reference
4. Branch Versioning
5. Branch Event Layer
6. Branch Lineage
7. Branch Provenance
8. Branch Isolation
9. Branch Lifecycle
10. Merge Eligibility
11. Branch Integrity Validation

### E44 — Tamper Detection
**Authority:** Tamper Detection Authority
**Internal Additions (11):**
1. Tamper Detection Scanner
2. Integrity Comparison
3. Hash Verification
4. Snapshot Integrity Check
5. Event Chain Check
6. Version Reference Check
7. Unauthorized Mutation Indicator
8. Provenance/Lineage Consistency Check
9. Detection Classification
10. Affected Scope Mapping
11. Tamper Detection Record

### E45 — Snapshot Diff Engine
**Authority:** Snapshot Diff Authority
**Internal Additions (11):**
1. Snapshot Comparator
2. Version Comparator
3. Memory Object Diff
4. Field-level Diff
5. Added/Removed Detector
6. Changed Value Detector
7. Event-to-Diff Mapper
8. Lineage Diff Mapping
9. Provenance Diff Mapping
10. Diff Integrity Validator
11. Comparison Result Model

### E46 — Memory Pattern Discovery
**Authority:** Memory Pattern Discovery Authority
**Internal Additions (11):**
1. Pattern Candidate Detector
2. Recurrence Detector
3. Co-occurrence Detector
4. Temporal Pattern Detector
5. Relationship Pattern Detector
6. Retrieval Pattern Detector
7. Usage Pattern Detector
8. Pattern Support Mapping
9. Pattern Deduplication
10. Pattern Lifecycle
11. Pattern Validation

### E47 — Memory Uncertainty
**Authority:** Memory Uncertainty Representation Authority
**Internal Additions (11):**
1. Uncertainty State Model
2. Unknown State
3. Incomplete State
4. Conflict State Mapping
5. Staleness State
6. Temporal Uncertainty
7. Provenance Uncertainty
8. Lineage Uncertainty
9. Uncertainty Propagation
10. Uncertainty Resolution Status
11. Uncertainty Audit Record

### E48 — Degradation Detection
**Authority:** Memory Degradation Detection Authority
**Internal Additions (11):**
1. Degradation Detector
2. Trend Analyzer
3. Baseline Comparator
4. Performance Degradation Detector
5. Retrieval Degradation Detector
6. Storage Degradation Detector
7. Index Degradation Detector
8. Capacity Degradation Detector
9. Integrity-related Degradation Indicator
10. Degradation Classification
11. Degradation Handoff

### E49 — Consistency Recovery & Repair
**Authority:** Memory Consistency Recovery & Repair Authority — Approval Gate mandatory
**Internal Additions (11):**
1. Consistency Diagnosis
2. Repair Planner
3. Referential Repair
4. Version Consistency Repair
5. Snapshot Consistency Repair
6. Dependency Consistency Repair
7. Metadata Repair
8. Repair Validation
9. Atomic Repair Transaction
10. Recovery Escalation
11. Repair Audit Record

## 4. Final Count

- E1–E10: 10 engines, 212 additions
- E11–E14: 4 engines, 57 additions
- E15–E49: 35 engines, 385 additions
- **Total: 49 engines, 654 additions**
- Check: 212 + 57 + 385 = 654

## 5. Final Decisions

- E19 = Dependency Layer داخل E29 (retains name E19).
- E27 = Public Temporal Interface.
- E10 = Internal Temporal Engine.
- E28 = Retrieval Layer Coordinator only.
- E40 includes Retrieval Audit.
- E14 = Version/Snapshot Authority.
- E45 = Diff Capability.
- E9 = sole Audit Authority.
- E18 = Conflict Detection; E47 = Uncertainty Representation.
- E36 = Health; E48 = Degradation Detection.
- E49 = mandatory Approval Gate.
- No Stage 3 Engine owns Evidence Truth, Decision, Execution, Broker, Market Signal.

## 6. Status

Discovery: Complete
Design: Final Draft — Approved
Code: Not Started
Execution: Not Started
Closure: Pending

Final Count: 49 Engines + 654 Internal Additions

END OF STAGE 3 — FINAL DESIGN
Next: Phase 0 → Implementation Planning
