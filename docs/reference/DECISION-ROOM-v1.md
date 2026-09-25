# AmarBoot — Decision Room — MASTER PACKAGE v1.0

> Reference copy of the Decision Room package supplied by the owner for architectural discussion.
> This document is preserved as a reference and is not a new Stage, constitution, or implementation authority.

## Document Identity

- Owner: Amar
- Auditor / Engineer: DeepSeek
- Executor: ChatGPT
- Repository: amarazeez1992-eng/AmarBoot
- Branch: main
- Platform: Android / Kotlin / Jetpack Compose
- Status in the supplied package: FINAL — CLOSED FOR IMPLEMENTATION

## Executive Vision

Decision Room is an institutional analytical layer, not a signal screen, indicator dashboard, or execution channel. It aggregates evidence, sources, schools, strategies, data, markets, and timeframes; cleans and verifies them; checks independence, conflicts, and quality; builds unified market state; and produces interpretable, reconstructable, auditable analytical decisions.

It is protected against temporal contamination, duplication, and parallel authority. It does not execute trades.

## Core Principles

- QUALITY > QUANTITY
- FREE_CORE 100%
- MOBILE_FIRST
- MT5_INDEPENDENT
- AI_OPTIONAL
- OPEN_SOURCE_FUTURE
- TIME_IRRELEVANT

## Absolute Prohibitions

1. No closure without evidence.
2. No NOT_TRIGGERED -> SUCCESS.
3. No Mergeable = Ready.
4. No Design = Implementation.
5. No historical CI = current proof.
6. No fix without root cause.
7. No protected-file modifications.
8. No migration without design.
9. No parallel authority.
10. No hardcoded answers.
11. No guessing.
12. Current evidence is the truth source.

## Roles

### Owner
Makes final decisions, opens/closes stages, resolves disputes, and approves proposals.

### DeepSeek
Audits outputs, issues stage instructions, gives green/yellow/red assessments, and proposes engineering decisions.

### ChatGPT
Executes approved work, accesses GitHub, inspects the repository, and provides evidence/reports.

DeepSeek and ChatGPT communicate through the owner.

## Work Protocol

1. Audit current work.
2. Value assessment.
3. Gap analysis.
4. Improvement proposal.
5. Approval.
6. Implementation with minimal fix, regression tests, and audit trail.
7. Constitutional closure.

## Full Verification

Root cause -> minimal fix -> compile -> unit tests -> integration tests -> APK build -> static verification -> runtime verification -> regression -> audit -> closure.

## Architecture

External sources -> collection/web access -> master library/discovery -> deduplication/independence -> quality/freshness -> market state/timeframes -> trading divisions -> Manager aggregation -> Decision Engine -> Verification -> Snapshot/Freeze -> Ledger -> Alerts/UI.

Optional AI path:

AI -> Schema -> Policy -> Evidence -> Verify -> Manager

The AI path is optional and must not become final decision authority.

Mandatory decision path:

Manager -> Decision Engine -> Verify -> Snapshot -> Freeze -> Ledger

## Manager / Decision Engine Boundary

Manager coordinates aggregation and policy and produces ManagerOutput. It must not emit BUY/SELL/WAIT/NO_TRADE.

Decision Engine is the final Decision Room decision authority and consumes ManagerOutput.

## AI Boundary

The system must operate without AI. AI may enhance research and evidence but cannot directly become Decision Engine authority. AI failure must not equal core decision failure. Security is fail-closed.

## Execution Boundary

Decision Room must not:
- send orders;
- connect to MT5 for execution;
- execute trades;
- modify positions;
- access broker accounts;
- hold broker execution keys.

Future-compatible outputs may include read-only MT5 integration, JSON/PDF export, user notifications, and advisory Entry/SL/TP.

## Protected MT5 Artifact

Protected file:
`mt5/Experts/Grid_Martingale_Basket_v2.mq5`

Version: 2.01
SHA-1: `1125cabef6b4a4b1d5eb2c5deaa4b5e7458479ad25`

No modification, restructure, replacement, or overwrite.

## Source Lifecycle

HealthStatus:
- ACTIVE
- DEGRADED
- DEAD
- UNKNOWN

SourceStatus:
- DISCOVERED
- PENDING_REVIEW
- VALIDATED
- ACTIVE
- SUSPENDED
- REJECTED
- ARCHIVED

These states are separate.

Failure rule:
- 3 consecutive failures in 30 days -> DEGRADED.
- 7 consecutive failures -> DEAD + SUSPENDED.
- Any success resets the failure counter.
- 5 consecutive successes support recovery to ACTIVE.
- SUSPENDED -> ACTIVE requires manual review.

## Evidence Lifecycle

OBSERVED -> NORMALIZED -> SANITIZED -> VALIDATED -> UNIQUE -> CLASSIFIED -> SCORED -> USED_IN_DECISION -> ARCHIVED

Reject reasons include:
DUPLICATE, STALE, LOW_QUALITY, NON_INDEPENDENT, INVALID, OUT_OF_TIME, UNTRUSTED, CONFLICTED, INSUFFICIENT_SAMPLE.

## Decision Lifecycle

DRAFT -> COMPOSED -> EVALUATED -> FROZEN (60s) -> PERSISTED -> PUBLISHED -> EXPIRED -> OUTCOME_RECORDED

Outcome recording is separate from decision generation.

## Decision Snapshot

### Snapshot Input
Contains Evidence Graph, Source Profiles, Regime, Engine Versions, Manager Version, Decision Engine Version, Policy Version, Weight Version, Timestamp, and no decision output.

### Original Decision Output
Contains Decision, Layer Scores, Composite Score, Scenarios, and Timestamp.

### Replay
Load Snapshot Input -> run Decision Engine -> compare with Original Decision Output.

## Ledger

currentHash = SHA256(CanonicalSerialize(payload) + previousHash)

Canonical serialization:
- sorted keys;
- no extra whitespace;
- NFC normalization;
- deterministic number format;
- UTF-8.

Purpose: integrity and tamper detection, not a digital signature.

Official integrity wording:
Ledger Hash-Chain Integrity verified.

## Test Types

1. Unit
2. Contract
3. Integration
4. Evidence
5. Conflict
6. Regime
7. Historical
8. Calibration
9. Robustness
10. Failure
11. Security
12. Determinism

Test states:
- MANDATORY
- APPLICABLE
- NOT_APPLICABLE

NOT_APPLICABLE requires reason, approvedBy, and approvedAt.

## Contracts

1. Data Contracts / JSON Schemas
2. Module Interfaces / Kotlin
3. Ledger Schema / Canonical Hash
4. Evidence Graph Schema
5. Source Registry Schema
6. Error Taxonomy
7. Manager Engine Contract
8. Division Contract
9. Engine Contract
10. Discovery Engine Contract
11. Master Library Contract

## Failure Protocol

Level 1 — minor: source outage -> log and continue.

Level 2 — moderate: main source outage or contract mismatch -> log, alert, review.

Level 3 — critical: parallel authority, data integrity failure, ledger mismatch, determinism failure, look-ahead, execution attempt, or invalid stage closure -> STOP -> LOG -> PROTECT -> ROOT CAUSE -> FIX -> TEST -> AUDIT -> OWNER APPROVAL -> RESUME.

## Success Conditions

- correct agreements;
- variable independence;
- conflict detection and display;
- WAIT reachable;
- ledger integrity;
- reconstructable decision;
- AI optional;
- Tier System;
- Discovery;
- clear UI;
- determinism;
- point-in-time correctness;
- execution boundary;
- no parallel authority.

## Internal Work Map

The supplied package defined 14 internal work stages and 162 execution points. These numbers are internal to the Decision Room package and are not a replacement for the Amar 33-stage architecture.

### Stage 0.0 — Pre-Flight Audit
1. Repository structure
2. Existing inventory
3. Missing inventory
4. Parallel-authority detection
5. MT5 v2.01 inspection
6. Schools/indicators inspection
7. Free-source inspection
8. Source-tier classification
9. Gap analysis
10. Path decision

### Stage 0 — Constitution & Contracts
11. Constitution v2.1-FINAL.1
12. Data Contracts
13. Module Interfaces
14. Ledger Schema
15. Evidence Graph Schema
16. Source Registry Schema
17. Error Taxonomy
18. Folder Structure
19. Schema Tests
20. Manager Engine Contract
21. Division Contract
22. Engine Contract
23. Discovery Engine Contract
24. Master Library Contract

### Stage 1 — Foundation & Security
25. Android setup
26. Secrets Management / Keystore
27. Logging / Audit
28. Fail-closed Error Handling
29. CI pipeline
30. Testing framework
31. DI Hilt/Koin
32. Notification framework
33. Room
34. Network

### Stage 2 — Collection & Web Access
35. Source Registry implementation
36. Market Data Collector
37. News Collector
38. Research Collector
39. Normalization
40. Sanitization
41. Rate Limiting
42. Source Health Monitor
43. Chromium Headless
44. HTML Parser / Jsoup
45. API Client
46. RSS
47. Site Adapters
48. Cache

### Stage 3 — Deduplication & Independence
49. URL Dedup
50. Content Dedup
51. Source Family Detection
52. Claim Dedup
53. Dataset Dependency
54. Independence Calculator
55. Evidence Graph Builder
56. Graph Query
57. Independence Tester
58. Graph Visualizer

### Stage 4 — Quality & Freshness
59. Quality Scorer Q
60. Freshness Decay F
61. Base Weight Registry
62. Effective Weight
63. Multi-Regime lambda
64. Freshness Detector
65. Old/New Comparator
66. Quality Tester
67. Cache Cleanup
68. Archive

### Stage 5 — Market State & Timeframes
69. Regime Detector
70. MTF Analyzer
71. Hierarchical Layers
72. Conflict Engine
73. HTF/LTF Rules
74. Supply/Demand
75. FVG
76. Order Block
77. Premium/Discount
78. Zone Synthesizer

### Stage 6 — Trading Sections & Engines
79. Quant Division
80. Algo Division
81. Indicator Division
82. ICT Group
83. SMC Group
84. Wyckoff Group
85. Elliott Group
86. Price Action Group
87. Volume Profile Group
88. Pattern Division
89. Bot Division
90. Zone Division
91. News Division
92. Research Division
93. Trend Division
94. Engine Registry
95. Engine Orchestrator
96. Division Synthesizers
97. Cross-Division Aggregator
98. Division Controller

### Stage 7 — Decision Engine + Manager
99. Layer Score Calculator
100. Composite Decision
101. Conflict Thresholds
102. WAIT / NO TRADE
103. Scenario Generator A/B/C
104. Decision Snapshot
105. Version Manager + Freeze
106. Agreement Formula
107. Manager Engine Core
108. Manager Aggregation
109. Manager Weighting
110. Manager Policy Coordination

### Stage 8 — Verification & AI
111. Verification Layer
112. Output Isolation
113. Prompt Injection Defense
114. Fail-Closed
115. AI Research Agent
116. AI Critic Agent
117. AI Budget Manager
118. AI Cache

### Stage 9 — Ledger, UI & Alerts
119. Decision Ledger / Hash Chain
120. Mobile UI Main
121. Why Explainer
122. Alerts Engine
123. Decision Version Display
124. Notifications Hub
125. Metrics Dashboard
126. Search Interface
127. Settings UI
128. Detail View
129. Color State Manager
130. Progress Indicator
131. History View
132. JSON/PDF Export

### Stage 10 — Discovery Engines
133. Indicator Discovery
134. Bot Discovery
135. Strategy Discovery
136. School Discovery
137. Research Discovery
138. News Discovery
139. Trend Discovery
140. Refresh Scheduler 12h
141. Refresh Scheduler 24h
142. On-Demand Refresh
143. Discovery Cache
144. Discovery Notification

### Stage 11 — Master Library
145. Schools Library
146. Indicators Library
147. Strategies Library
148. Bots Library
149. Research Library
150. News Library
151. Patterns Library
152. Extensions Library
153. Internal Search Engine
154. Auto-Indexing

### Stage 12 — Outcome & Learning
155. Outcome Tracker
156. Calibration Engine
157. Champion/Challenger
158. Historical Replay
159. Point-in-Time Verify
160. Shadow Mode
161. Learning Loop with Gates
162. Approval Process

## Stage Governance

Stage states:
NOT_STARTED -> OPEN -> IN_PROGRESS -> PENDING_CLOSURE -> AUDITED -> APPROVED -> CLOSED

Failure:
AUDITED -> IN_PROGRESS

No direct OPEN -> CLOSED, IN_PROGRESS -> CLOSED, or PENDING_CLOSURE -> CLOSED without audit.

Golden rule:
No N+1 before N closure.

## Tracking Symbols

- [ ] NOT_STARTED
- [~] IN_PROGRESS
- [x] constitutionally closed
- [!] stopped

## Communication Protocol

Owner gives task -> ChatGPT executes -> GitHub evidence/report -> Owner sends to DeepSeek -> DeepSeek audits -> Owner approves -> task closes -> next task.

## Namespace Rule

Before creation: inspect the full repository, list files, detect conflicts, propose a unique namespace, and obtain approval before creating new structures.

## Final Boundary Statement

Decision Room is an analytical subsystem/reference package. It does not override the AMAR Master Constitution or the current Amar 33-stage map. It does not create a parallel Decision Authority, Evidence Authority, or Execution Authority.
