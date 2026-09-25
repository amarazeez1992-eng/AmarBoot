# AMARBOT — STAGE 16
# BOT INTELLIGENCE — v2 (Cleaned)

**Status:** Living Document — Editable  
**Revision:** v2 (Cleaned)  
**Items:** ~111  
**Additions:** ~1241  
**Modification:** Allowed — Add / Edit / Delete without constitutional re-approval  
**Previous Version:** STAGE16-BOT-INTELLIGENCE.md

> This version is a cleaned, editable reference. The previous v1 file is preserved unchanged.

---

# الجزء الأول — القواعد الحاكمة

## 1.1 القاعدة الأساسية

**Stage 16 = Coordinator + Builder + Consumer.**

Stage 16 مسؤول عن:
- تنسيق مكونات البوت.
- بناء مكونات البوت الخاصة به.
- استهلاك قدرات ومخرجات Stages الأخرى ضمن حدودها.
- لا يمتلك Authority موازية لأي Stage آخر.

## 1.2 الحدود الدستورية

| Stage | الاختصاص |
|---|---|
| Stage 11 | Evidence / Verification / Confidence |
| Stage 12 | Research / Sources |
| Stage 13 | Decision / Reasoning / Recommendation |
| Stage 14 | Execution / Authorization / Broker |
| Stage 15 | Signals / Market Intelligence |
| Stage 16 | Bot Construction / Coordination / Consumption |

القاعدة:

**Signal ≠ Decision ≠ Execution**

Stage 16 لا يعيد بناء Authority موجودة في Stage 11–15.

## 1.3 قواعد الاستهلاك

- Evidence / Verification / Confidence → Stage 11.
- Research / Sources → Stage 12.
- Decision / Reasoning / Recommendation → Stage 13.
- Execution / Authorization / Broker → Stage 14.
- Signals / Market Intelligence → Stage 15.
- Bot-specific construction / coordination / consumption → Stage 16.

## 1.4 القواعد التشغيلية

- أي Action داخلي خاص بالبوت يمكن أن يكون Native داخل Stage 16.
- أي Action مالي أو تنفيذ فعلي يمر عبر Stage 14.
- Stage 16 لا يصبح Decision Authority مستقلًا.
- Stage 16 لا يصبح Execution Authority مستقلًا.
- Stage 16 لا ينشئ Evidence Authority موازية.
- Stage 16 لا ينشئ Research Authority موازية.
- Stage 16 لا ينشئ Signal Authority موازية.

## 1.5 مبدأ العدد

العدد **120 Item ليس رقمًا دستوريًا**.

يمكن تعديل العدد مستقبلًا فقط إذا أثبت الفحص أن:
- Item غير ضروري.
- Item مكرر.
- Item متعارض.
- Item ناقص.
- أو توجد حاجة موثقة لإضافة جديدة.

لا توجد قاعدة تفرض الوصول إلى رقم محدد.

---

# الجزء الثاني — الاتفاقيات التشغيلية

## Agreement 1 — Bot UI

كل Bot يمتلك UI خاصًا به.

- الحقول ديناميكية حسب البوت.
- AI يستطيع تصميم واجهة البوت.
- المستخدم يتحكم بالمظهر.
- UI تحفظ حسب Bot Number.
- الأزرار الداخلية → Native.
- الأزرار ذات الأثر المالي → Stage 14.
- المسار المالي:
  **Stage 16 → Stage 14 → Bridge → MT5 → Broker**

## Agreement 2 — Bridge

الـBridge هو طبقة الربط بين التطبيق وMT5.

المسار:

**Android App → API → Cloud → MT5**

ولا يعتمد النظام على Laptop كوسيط.

## Agreement 3 — Zero Laptop

**Laptop = Not Required.**

النظام مصمم بحيث يستطيع المستخدم تشغيل وإدارة المنظومة من الهاتف دون الحاجة إلى Laptop.

## Agreement 4 — Free Cloud

الخطة المستهدفة:

**Oracle Cloud Free Tier**

المواصفات المستهدفة عند توفرها ضمن حدود الخطة:

- 2 OCPU
- 12 GB RAM

ولا يُعامل توفر هذه الموارد كضمان دائم؛ التوفر الفعلي يخضع لحدود Oracle Free Tier والمنطقة والموارد المتاحة.

إذا وُجد بديل Open Source / Free مناسب يحقق المتطلبات، يمكن تقييمه.

الهدف:
- تشغيل الخدمات المطلوبة 24/7.
- تشغيل MT5 على البيئة السحابية المناسبة.
- وصول الهاتف عبر API / Remote Access حسب الحاجة.

## Agreement 5 — MT5 Link & Secrets

MT5 يعمل على البيئة السحابية المناسبة، والهاتف هو واجهة التحكم.

- **Android Keystore** مسؤول عن أسرار/بيانات الاعتماد المحلية على الجهاز.
- **Server Secrets** لها طبقة تخزين وإدارة مستقلة على الخادم.
- لا يتم الخلط بين Android Keystore وServer Secrets.
- تسجيل الدخول **مرة واحدة قدر الإمكان**.
- بيانات الحساب لا تُخزن كنص مكشوف داخل التطبيق.
- MT5 يمكن أن يبقى متصلًا وفق آلية التشغيل المعتمدة.

## Agreement 6 — Order Flow

المسار التشغيلي:

**Phone → API → Cloud → MT5 → Broker → Execution**

Stage 16 لا ينفذ الصفقة بنفسه.

## Agreement 7 — Cost

**لا يوجد اشتراك شهري إلزامي** كقاعدة تصميمية.

استخدام Free Tier أو البرمجيات الحرة يعتمد على توفرها وحدودها وشروطها، ولا يعني أن جميع الخدمات الخارجية مضمونة مجانًا في جميع الظروف.

## Agreement 8 — Security

- فصل Android Keystore عن Server Secrets.
- حماية بيانات الحساب والاعتمادات.
- عدم تخزين الأسرار بشكل مكشوف داخل APK.
- تطبيق مبدأ أقل صلاحية.
- تسجيل العمليات الحساسة.
- الحفاظ على الفصل بين الهاتف والخادم وMT5.

---

# الجزء الثالث — الـ111 Item كاملة

## 1. Discovery Engine

**Boundary:** Consumer only; Stage 12 owns discovery/source authority.
**Source / Authority:** Stage 12

1. Multi-Source Scanner
2. Scheduled Crawler
3. Source Registry
4. Rate Limiter
5. Deduplication
6. Metadata Extractor
7. Provenance Tracker
8. Freshness Scorer
9. Reputation Filter
10. Ranking Engine
11. Cache Layer
12. Retry Handler
13. Failure Logger
14. Auto-Update Monitor
15. Discovery Audit

## 2. User Strategy Intake

**Boundary:** Consumes Stage 2 interpretation/intake support; bot-specific intake remains Stage 16.
**Source / Authority:** Stage 2

1. Free-Text Parser
2. Voice Input
3. File Upload
4. Strategy Template
5. Clarification Loop
6. Ambiguity Detector
7. Intent Classifier
8. Priority Setter
9. Version Tracker
10. User Intent Persistence Contract
11. Intake Audit

## 3. Concept Decomposition

**Boundary:** Bot-strategy decomposition only; Stage 2 remains general semantic/cognitive authority.
**Source / Authority:** Stage 2

1. Sentence Splitter
2. Concept Extractor
3. Atomic Concept Mapper
4. Ontology Linker
5. Dependency Graph
6. Conflict Pre-Check
7. Confidence Scorer
8. Concept Versioning
9. Storage
10. Retrieval
11. Review Gate
12. Decomposition Audit

## 4. Concept Library

**Boundary:** Bot-specific concept library; no general knowledge authority.
**Source / Authority:** Stage 16

1. Concept Storage
2. Search Index
3. Category Tree
4. Tag System
5. Reuse Tracker
6. Version History
7. Deprecation Marker
8. Export/Import
9. Library Audit

# B — Deep Analysis

## 5. Code Analyzer

**Boundary:** Bot artifact analysis only; general cognition/evidence verification remains upstream.
**Source / Authority:** Stage 2 / Stage 11

1. Language Detector
2. AST Parser
3. Logic Flow Mapper
4. Entry Point Finder
5. Dependency Extractor
6. API Call Detector
7. Side Effect Scanner
8. Complexity Calculator
9. Documentation Extractor
10. Comment Analyzer
11. Dead Code Detector
12. Anti-Pattern Detector
13. Code Smell Reporter
14. Refactor Suggester
15. Quality Score
16. Comparison with Original
17. Version Diff
18. Analysis Report
19. Analysis Sandbox Boundary
20. Code Analyzer Audit

## 6. License Compliance

**Boundary:** Bot-specific license compliance; consumes foundational infrastructure.
**Source / Authority:** Stage 1 / Stage 16

1. License Detector
2. SPDX Identifier
3. Compatibility Checker
4. Commercial Use Check
5. Attribution Requirements
6. Copyleft Detector
7. Patent Clause Parser
8. Conflict Reporter
9. Recommendation Engine
10. License Audit

## 7. Dependency Auditor

**Boundary:** Bot-specific dependency audit; consumes foundational/security services.
**Source / Authority:** Stage 1 / Stage 16

1. Dependency Tree Builder
2. Version Extractor
3. Vulnerability Scanner
4. CVE Matcher
5. Outdated Check
6. License Scanner
7. Transitive Analyzer
8. Conflict Detector
9. Fix Suggester
10. Dependency Audit

## 8. Security Scanner

**Boundary:** Bot-specific security scanning; shared security authority remains foundational/central.
**Source / Authority:** Stage 16

1. Secret Detector
2. Key Leak Scanner
3. Injection Checker
4. XSS Detector
5. Auth Bypass Detector
6. Crypto Weakness Checker
7. Permission Analyzer
8. Network Security Check
9. Input Validation Check
10. Output Encoding Check
11. Rate Limit Check
12. Error Leak Checker
13. Security Score
14. Report Generator
15. Security Audit

## 9. Overfitting Detector

**Boundary:** Consumes evidence/reasoning outputs; no independent evidence or decision authority.
**Source / Authority:** Stage 11 / Stage 13

1. Train-Test Splitter
2. Cross-Validator
3. Walk-Forward Engine
4. In-Sample/Out-of-Sample Comparator
5. Parameter Sensitivity
6. Curve Fit Analyzer
7. Robustness Score
8. Overfitting Report
9. Warning Generator
10. Overfitting Audit

## 10. Look-Ahead Detector

**Boundary:** Bot-specific leakage checks; authoritative evidence/signal processing remains upstream.
**Source / Authority:** Stage 11 / Stage 15

1. Timestamp Validator
2. Data Leakage Scanner
3. Future Data Detector
4. Candle Lag Checker
5. Refresh Timing Check
6. Execution Timing Check
7. Bias Reporter
8. Confidence Score
9. Warning Generator
10. Look-Ahead Audit

## 11. Reproducibility Checker

**Boundary:** Bot-specific reproducibility validation.
**Source / Authority:** Stage 16

1. Environment Capture
2. Seed Tracker
3. Data Snapshot
4. Determinism Validator
5. Repeat Test Runner
6. Result Comparator
7. Variance Analyzer
8. Reproducibility Score
9. Reproducibility Audit

# C — Expert Council

## 12. Parameter Research Engine

**Boundary:** Consumer only; requests parameter research and consumes approved Stage 12/13 outputs.
**Source / Authority:** Stage 12 / Stage 13

1. Parameter Identifier
2. Range Finder
3. Historical Sweep
4. Regime-Aware Search
5. Timeframe-Aware Search
6. Instrument-Aware Search
7. Optimal Value Finder
8. Confidence Interval
9. Cross-Validation
10. Sensitivity Analysis
11. Robustness Check
12. Recommendation Builder
13. Source Citation
14. Report Generator
15. Comparison Matrix
16. Version Tracker
17. Parameter Audit
18. Re-Calibration Trigger
19. Deprecation Tracker
20. Parameter Audit Log

## 13. Historical Case Study

**Boundary:** Consumes research/reasoning for bot case studies.
**Source / Authority:** Stage 12 / Stage 13

1. Case Selector
2. Similarity Matcher
3. Regime Filter
4. Timeframe Filter
5. Outcome Extractor
6. Statistic Aggregator
7. Pattern Finder
8. Failure Case Finder
9. Success Case Finder
10. Case Report Builder
11. Visualization Data
12. Case Study Audit

## 14. Statistical Validation

**Boundary:** Bot-specific statistical testing.
**Source / Authority:** Stage 16

1. Win Rate Calculator
2. R:R Analyzer
3. Expectancy Calculator
4. Sharpe Ratio
5. Sortino Ratio
6. Max Drawdown
7. Recovery Factor
8. Profit Factor
9. Sample Size Check
10. Confidence Interval
11. Significance Test
12. Statistical Report
13. Statistical Audit

## 15. Expert Opinion with Evidence

**Boundary:** Assembles attributed inputs; no evidence verification or final decision authority.
**Source / Authority:** Stage 11 / Stage 13

1. Opinion Builder
2. Evidence Linker
3. Source Chain
4. Confidence Scorer
5. Alternative View
6. Counter Argument
7. Weakness Disclosure
8. Assumption List
9. Reasoning Summary
10. Opinion Audit

## 16. Precision Determination

**Boundary:** Bot-specific precision handling.
**Source / Authority:** Stage 16

1. Numeric Extractor
2. Unit Normalizer
3. Rounding Rules
4. Decimal Precision
5. Tolerance Setting
6. Cross-Reference
7. Precision Validator
8. Precision Report
9. Precision Audit
10. Version Tracker

## 17. Critical Review

**Boundary:** Critical review of bot artifacts; consumes cognition/evidence/decision capabilities.
**Source / Authority:** Stage 2 / Stage 11 / Stage 13

1. Hypothesis Challenger
2. Assumption Tester
3. Evidence Stress Test
4. Logic Checker
5. Gap Finder
6. Bias Detector
7. Alternative Explanation
8. Severity Classifier
9. Rejection Reason
10. Modification Suggester
11. Approval Gate
12. Critical Review Audit

## 18. Alternative Generator

**Boundary:** Bot-specific alternative generation.
**Source / Authority:** Stage 16

1. Variation Builder
2. Parameter Variation
3. Concept Variation
4. Regime Variation
5. Comparison Engine
6. Ranking Engine
7. Trade-off Reporter
8. Recommendation Builder
9. Alternative Audit

# D — Dialogue & Design

## 19. Design Dialogue Engine

**Boundary:** Consumes cognitive dialogue capabilities; owns bot-design dialogue state.
**Source / Authority:** Stage 2

1. Intent Interpreter
2. Question Generator
3. Clarification Loop
4. Context Tracker
5. Reference Resolver
6. Misunderstanding Detector
7. Confirmation Loop
8. Multi-Turn Memory
9. User Preference Tracker
10. Dialogue State Machine
11. Response Builder
12. Explanation Generator
13. Dialogue Log
14. Dialogue Audit
15. Dialogue Recovery

## 20. Interactive Refinement Loop

**Boundary:** Bot-specific iterative refinement.
**Source / Authority:** Stage 16

1. Draft Builder
2. User Feedback Collector
3. Diff Analyzer
4. Iteration Tracker
5. Convergence Detector
6. Max Iteration Guard
7. Version Snapshot
8. Rollback Point
9. Refinement Log
10. Refinement Audit

## 21. Multi-Concept Composition

**Boundary:** Consumes cognitive/reasoning capabilities; composition remains bot-specific.
**Source / Authority:** Stage 2 / Stage 13

1. Concept Selector
2. Compatibility Checker
3. Combination Engine
4. Conflict Resolver
5. Priority Setter
6. Weight Assigner
7. Composite Evaluator
8. Result Comparator
9. Composition Report
10. Rollback Handler
11. Version Tracker
12. Composition Audit

## 22. Conflict Resolution Between Concepts

**Boundary:** Bot concept conflict resolution only; general contradiction/reasoning authority remains upstream.
**Source / Authority:** Stage 2 / Stage 13

1. Conflict Detector
2. Severity Classifier
3. Root Cause Finder
4. Alternative Suggester
5. Priority Resolver
6. User Consultation
7. Resolution Tracker
8. Post-Resolution Validator
9. Conflict Log
10. Conflict Audit

## 23. Bot Specification Freeze

**Boundary:** Bot specification lifecycle only.
**Source / Authority:** Stage 16

1. Spec Builder
2. Completeness Checker
3. Consistency Checker
4. Freeze Trigger
5. Version Snapshot
6. Change Lock
7. Unlock Request Handler
8. Freeze Report

## 24. Pre-Decision Dialogue Loop

**Boundary:** Pre-decision preparation only; Stage 13 owns final decision.
**Source / Authority:** Stage 13

1. Decision Draft
2. User Consultation
3. Test Plan Builder
4. Test Executor
5. Result Presenter
6. Agreement Tracker
7. Signature Gate
8. Decision Log
9. Dialogue Audit
10. Rollback Handler

# E — Bot DNA

## 25. DNA Fingerprint

**Boundary:** Bot identity fingerprint.
**Source / Authority:** Stage 16

1. Trait Extractor
2. Hash Generator
3. Uniqueness Validator
4. Collision Detector
5. Fingerprint Storage
6. Lookup Engine
7. Comparison Engine
8. DNA Audit

## 26. DNA Library

**Boundary:** Bot DNA library.
**Source / Authority:** Stage 16

1. DNA Storage
2. Category Tree
3. Tag System
4. Search Index
5. Version History
6. Lineage Tracker
7. Export/Import
8. DNA Audit

## 27. DNA Combination

**Boundary:** Bot DNA combination.
**Source / Authority:** Stage 16

1. DNA Selector
2. Compatibility Checker
3. Trait Blender
4. Conflict Resolver
5. Result Validator
6. New DNA Generator
7. Combination Log
8. Lineage Tracker
9. Rollback
10. Combination Audit

## 28. DNA Mutation

**Boundary:** Bot DNA mutation.
**Source / Authority:** Stage 16

1. Mutation Point Selector
2. Mutation Type
3. Rate Controller
4. Fitness Predictor
5. Mutation Executor
6. Result Validator
7. Lineage Tracker
8. Rollback
9. Mutation Log
10. Mutation Audit

# F — Construction

## 29. Code Generator

**Boundary:** Bot construction.
**Source / Authority:** Stage 16

1. Language Selector
2. Template Engine
3. Parameter Injector
4. Naming Convention
5. Comment Injector
6. Safety Checker
7. Static Analyzer
8. Unit Test Generator
9. Documentation Generator
10. Output Formatter
11. Lint Integration
12. Build Integrator
13. Version Tagger
14. Diff Generator
15. Review Gate
16. Export Handler
17. Code Review
18. Quality Score
19. Generated-Code Provenance
20. Version Tracker
21. Rollback
22. Comparison Engine
23. Benchmark
24. Performance Check
25. Code Generator Audit

## 30. Pine ↔ MQL5 ↔ Python Translator

**Boundary:** Artifact translation/construction only; live deployment/execution remains Stage 14.
**Source / Authority:** Stage 16 / Stage 14

1. Pine Parser
2. MQL5 Parser
3. Python Parser
4. IR
5. Semantic Mapper
6. Function Mapping
7. Indicator Translation
8. Order Logic Translation
9. Error Handling Translation
10. Result Validator
11. Diff Comparator
12. Fidelity Test
13. Translation Log
14. Translation Audit
15. Rollback

## 31. Security Sandbox

**Boundary:** Bot construction sandbox; no production execution authority.
**Source / Authority:** Stage 16

1. Isolated Runtime
2. Resource Limiter
3. Network Blocker
4. File Access Control
5. System Call Filter
6. Timeout Enforcer
7. Output Capturer
8. Behavior Logger
9. Escape Detector
10. Sandbox Report
11. Sandbox Audit
12. Recovery Handler

## 32. Cross-Stage Integration

**Boundary:** Bot-side integration/coordination; authority stays with source stages.
**Source / Authority:** Cross-stage

1. Stage Registry
2. Capability Map
3. API Contract
4. Data Mapper
5. Event Bus
6. Conflict Detector
7. Version Negotiation
8. Compatibility Check
9. Integration Test
10. Integration Log
11. Failure Handler
12. Recovery Handler
13. Integration Audit
14. Health Monitor
15. Authority Contract Validator

## 33. Massive Source Access Layer

**Boundary:** Consumer only; Stage 12 owns source-access infrastructure/authority.
**Source / Authority:** Stage 12

1. Source Registry
2. API Key Manager
3. Rate Limiter
4. Quota Tracker
5. Cache Layer
6. Failover
7. Health Check
8. Cost Tracker
9. Usage Logger
10. Access Audit
11. Privacy Guard

## 34. Web-Wide Authorization

**Boundary:** Authorization policy is centralized; individual bots cannot self-authorize web access.
**Source / Authority:** Central / Stage 12

1. Domain Whitelist
2. robots.txt Checker
3. Terms of Service Checker
4. Legal Guard
5. Permission Tracker
6. Consent Logger
7. Access Audit
8. Revocation Handler
9. Compliance Reporter
10. Authorization Audit

# G — UI Designer

## 35. Dynamic UI Generator

**Boundary:** Bot-specific UI generation; financial controls route to Stage 14.
**Source / Authority:** Stage 16 / Stage 14

1. Layout Builder
2. Widget Selector
3. Field Generator
4. Validation Injector
5. Responsive Adapter
6. Preview Engine
7. Code Generator (UI)
8. Save per Bot
9. Load Handler
10. Edit Mode
11. Version Tracker
12. UI Audit
13. Theme Hook
14. Accessibility
15. Localization
16. Error Display
17. Loading State
18. Empty State
19. Help Tooltip
20. Execution-UI Boundary Guard

## 36. Theme Engine

**Boundary:** Bot UI theme only.
**Source / Authority:** Stage 16

1. Color Palette
2. Font Manager
3. Icon Set
4. Spacing System
5. Dark/Light Mode
6. Custom Theme Builder
7. Preview
8. Save Theme
9. Export/Import
10. Theme Audit

## 37. Settings Panel Builder

**Boundary:** Bot settings UI only.
**Source / Authority:** Stage 16

1. Field Type Detector
2. Bounds Validator
3. Default Injector
4. Tooltip Generator
5. Group Organizer
6. Conditional Display
7. Change Tracker
8. Validation Engine
9. Save Handler
10. Reset Handler
11. Settings Audit

## 38. Execute Button + Input

**Boundary:** UI is Native; any financial command routes to Stage 14.
**Source / Authority:** Stage 16 / Stage 14

1. Execute Button
2. Command Input Field
3. Voice Command
4. Command Parser
5. Permission Check
6. Confirmation Dialog
7. Execution Tracker
8. Result Display
9. Error Display
10. Internal-External Action Router
11. Execution Audit

## 39. Save per Bot Number

**Boundary:** Per-bot persistence only.
**Source / Authority:** Stage 16

1. Bot Number Key
2. UI Snapshot
3. Settings Snapshot
4. Version Tag
5. Storage Handler
6. Retrieval Engine
7. Conflict Detector
8. Migration Handler
9. Save Audit

# H — Testing Lab

## 40. Backtest Engine

**Boundary:** Bot-specific testing; consumes upstream decision/reasoning where required.
**Source / Authority:** Stage 13 / Stage 16

**الوظيفة: Bot-specific Testing فقط.**

1. Data Loader
2. Timeframe Selector
3. Symbol Selector
4. Entry Simulator
5. Exit Simulator
6. Slippage Model
7. Spread Model
8. Commission Model
9. Latency Model
10. Partial Fill Model
11. Trade Logger
12. Equity Curve Builder
13. Statistic Calculator
14. Report Generator
15. Visualization Data
16. Compare Engine
17. Benchmark
18. Save/Load
19. Validation Provenance Binding
20. Version Tracker

## 41. Walk-Forward

**Boundary:** Bot-specific validation; no decision authority.
**Source / Authority:** Stage 13

**الوظيفة: Bot-specific Testing فقط.**

1. Window Splitter
2. IS/OOS Partitioner
3. Optimization Loop
4. Validation Loop
5. Parameter Roll
6. Result Merger
7. Consistency Check
8. Walk-Forward Report
9. Warning Generator
10. Walk-Forward Audit
11. Version Tracker
12. Comparison Engine

## 42. Out-of-Sample

**Boundary:** Bot-specific validation; no decision authority.
**Source / Authority:** Stage 13

**الوظيفة: Bot-specific Testing فقط.**

1. OOS Splitter
2. IS Tester
3. OOS Tester
4. Performance Comparator
5. Overfit Detector
6. Consistency Score
7. OOS Report
8. Warning Generator
9. OOS Audit
10. Version Tracker

## 43. Stress Multi-Regime

**Boundary:** Consumes market regimes/signals and reasoning; no parallel market authority.
**Source / Authority:** Stage 15 / Stage 13

**الوظيفة: Bot-specific Testing فقط.**

1. Regime Detector
2. Regime Splitter
3. Per-Regime Tester
4. Transition Tester
5. Volatility Tester
6. Liquidity Tester
7. News Event Tester
8. Result Aggregator
9. Weakness Reporter
10. Stress Report
11. Stress Audit
12. Recovery Handler

## 44. Monte Carlo

**Boundary:** Bot testing only.
**Source / Authority:** Stage 16

**الوظيفة: Bot-specific Testing فقط.**

1. Random Seed Manager
2. Trade Shuffler
3. Slippage Randomizer
4. Spread Randomizer
5. Outcome Simulator
6. Distribution Builder
7. Percentile Calculator
8. Drawdown Analyzer
9. Confidence Interval
10. Monte Carlo Report
11. Monte Carlo Audit

## 45. Adversarial Scenarios

**Boundary:** Bot testing only.
**Source / Authority:** Stage 16

**الوظيفة: Bot-specific Testing فقط.**

1. Scenario Builder
2. Attack Simulator
3. Flash Crash Simulator
4. Broker Disconnect
5. Data Feed Failure
6. Slippage Spike
7. Spread Spike
8. Behavior Analyzer
9. Report Generator
10. Adversarial Audit

## 46. MT5 Conversion Fidelity

**Boundary:** Conversion fidelity is validated before handoff; deployment/execution remains Stage 14.
**Source / Authority:** Stage 14

**الوظيفة: Bot-specific Testing فقط.**

1. Pine Result Snapshot
2. MQL5 Result Snapshot
3. Trade Comparator
4. Entry Comparator
5. Exit Comparator
6. PnL Comparator
7. Diff Reporter
8. Fidelity Score
9. Warning Generator
10. Conversion Audit

## 47. Paper Trading Gate

**Boundary:** Paper-trading request/gate only; execution authority remains Stage 14.
**Source / Authority:** Stage 14

1. Demo Account Connector
2. Duration Tracker
3. Trade Logger
4. Performance Comparator
5. Deviation Detector
6. Promotion Recommender
7. Report Generator
8. Paper Trading Audit
9. Version Tracker
10. Recovery Handler

# I — Library

## 48. Storage Metadata

**Boundary:** Bot metadata; consumes foundational storage.
**Source / Authority:** Stage 1 / Stage 16

1. Schema Builder
2. Metadata Writer
3. Version Tagger
4. Source Tagger
5. License Tagger
6. Author Tagger
7. Date Tagger
8. Hash Generator
9. Storage Index
10. Storage Audit

## 49. Retrieval

**Boundary:** Bot retrieval; consumes foundational storage.
**Source / Authority:** Stage 1 / Stage 16

1. Query Parser
2. Lookup Engine
3. Version Resolver
4. Dependency Resolver
5. UI Resolver
6. Settings Resolver
7. Assembler
8. Retrieval Report
9. Retrieval Audit
10. Cache Layer

## 50. Modification Engine

**Boundary:** Bot artifact modification only.
**Source / Authority:** Stage 16

1. Change Detector
2. Diff Builder
3. Impact Analyzer
4. Validation
5. Test Runner
6. Version Bump
7. Change Log
8. Rollback Point
9. Modification Audit
10. Save Handler
11. Approval Gate

## 51. Re-Execution Engine

**Boundary:** Bot artifact/test re-execution only; financial execution remains Stage 14.
**Source / Authority:** Stage 16 / Stage 14

1. Environment Loader
2. Parameter Loader
3. Execution Runner
4. Result Capture
5. Compare with Original
6. Deviation Detector
7. Report Generator
8. Re-Execution Audit
9. Version Tracker
10. Recovery Handler

## 52. Duplicate Detection

**Boundary:** Bot-library duplicate detection.
**Source / Authority:** Stage 16

1. Fingerprint Calculator
2. Similarity Comparator
3. Threshold Check
4. Family Clusterer
5. Lineage Mapper
6. Duplicate Reporter
7. Merge Suggester
8. Duplicate Audit

## 53. Reputation System

**Boundary:** Bot-specific reputation consumer; authoritative source/signal assessment remains upstream.
**Source / Authority:** Stage 11 / Stage 15

1. Score Calculator
2. Source Weight
3. Author Weight
4. Test Weight
5. Age Weight
6. Community Weight
7. Reputation Storage
8. Ranking Engine
9. Reputation Report
10. Reputation Audit

# J — Bot Director & Engines

## 54. Bot Director Engine

**Boundary:** Bot Director/orchestration only.
**Source / Authority:** Stage 16

**الوظيفة المصححة: Coordinator / Planner — لا Decision Authority.**

1. Intent Interpreter
2. Plan Builder
3. Engine Selector
4. Task Dispatcher
5. Result Aggregator
6. Conflict Resolver
7. Priority Manager
8. State Machine
9. Decision Logger (تسجيل قرار Stage 13)
10. Director Audit
11. Health Tracker
12. Recovery Handler
13. Escalation Handler
14. Authority Check
15. Handshake Handler
16. Stage Authority Router
17. Director Report

## 55. Internal Engines Layer

**Boundary:** Sub-orchestrator/consumer only; no parallel decision, execution, or signal authority.
**Source / Authority:** Stage 13 / Stage 14 / Stage 15

**الوظيفة المصححة: Consumer / Sub-Orchestrator فقط.**

- Decision → Stage 13
- Execution → Stage 14
- لا Decision Authority مستقلة.
- لا Execution Authority مستقلة.

1. Signal Engine
2. Decision Engine Interface
3. Risk Engine
4. Execution Engine Interface
5. Position Manager
6. Monitoring Engine
7. Learning Engine
8. Conflict Handler
9. State Store
10. Log Store
11. Health Tracker
12. Recovery Handler
13. Engine Registry
14. Engine Test
15. Engine Audit
16. Version Tracker
17. Engine Report
18. Engine Capability Contract
19. Deprecation Tracker
20. Engine Audit Log

## 56. Standalone Mode

**Boundary:** Monitor + alert + safe pause request only.
**Source / Authority:** Stage 16 / Stage 14

**الوظيفة: Monitor + Alert + Safe Pause فقط.**

1. Amar Availability Check
2. Fallback Planner
3. Monitor Mode
4. Alert Handler
5. Safe Pause Requester
6. Notification Handler
7. Handshake Retry
8. Recovery Handler
9. Standalone Log
10. Bounded-Mode Safety Contract

## 57. Handshake مع Amar

**Boundary:** Consumes Amar/core cognitive interfaces; no authority escalation.
**Source / Authority:** Stage 2 / Core

1. Request Builder
2. Response Parser
3. Auth Verifier
4. State Sync
5. Version Negotiation
6. Error Handler
7. Retry Handler
8. Handshake Log
9. Handshake Audit
10. Recovery Handler

## 58. Engine Army Coordination

**Boundary:** Coordinates bot engines only; cannot replace source-stage authorities.
**Source / Authority:** Stage 16

1. Engine Registry
2. Capability Map
3. Task Allocator
4. Load Balancer
5. Conflict Detector
6. Result Aggregator
7. Priority Setter
8. Health Tracker
9. Failover Handler
10. Coordination Log
11. Recovery Handler
12. Coordination Audit
13. Performance Tracker
14. Optimization Loop

# K — Live & Monitor

## 59. Live Monitoring

**Boundary:** Consumes live market/signal outputs; monitoring remains bot-specific.
**Source / Authority:** Stage 15

1. Market Feed
2. Signal Watcher
3. Position Watcher
4. Price Watcher
5. Regime Watcher
6. News Watcher
7. Alert Trigger
8. State Store
9. Monitor Log
10. Recovery Handler
11. Failover
12. Monitor Audit

## 60. Health Score

**Boundary:** Bot health monitoring.
**Source / Authority:** Stage 16

1. Metric Collector
2. Weight Assigner
3. Score Calculator
4. Threshold Checker
5. Degradation Detector
6. Health Report
7. History Tracker
8. Health Audit

## 61. Kill Switch

**Boundary:** Requests kill/stop action; Stage 14 executes.
**Source / Authority:** Stage 14

**الوظيفة: Bot Request → Stage 14 ينفّذ.**

1. Trigger Definer
2. Manual Trigger
3. Auto Trigger
4. Kill Request Builder
5. Notification Sender
6. Audit Logger
7. Recovery Handler
8. Kill Request Contract

## 62. Alerts

**Boundary:** Bot alerts only.
**Source / Authority:** Stage 16

1. Alert Type Definer
2. Condition Builder
3. Priority Setter
4. Channel Selector
5. Rate Limiter
6. Deduplicator
7. Delivery Tracker
8. Alert Log
9. Alert Audit
10. Recovery Handler

# L — Evolution & Governance

## 63. Auto-parameter Adjustment

**Boundary:** Test → shadow → approval → Stage 14; no direct live parameter executor.
**Source / Authority:** Stage 14

**الوظيفة: Test → Shadow → Approval → Stage 14.**

1. Parameter Monitor
2. Trigger Definer
3. Adjustment Calculator
4. Boundary Check
5. Test Runner
6. Shadow Runner
7. Approval Gate
8. Adjustment Log
9. Rollback Point
10. Shadow-to-Live Promotion Contract

## 64. Retirement & Inheritance

**Boundary:** Bot lifecycle inheritance; position handoff/execution remains Stage 14.
**Source / Authority:** Stage 16 / Stage 14

1. Retirement Trigger
2. Performance Check
3. Knowledge Extractor
4. Inheritance Builder
5. Handover to New Bot
6. Archive Handler
7. Retirement Log
8. Retirement Audit

## 65. Audit Trail

**Boundary:** Bot audit trail.
**Source / Authority:** Stage 16

1. Event Collector
2. Immutable Store
3. Hash Chain
4. Version Tracker
5. Query Engine
6. Report Builder
7. Audit Log
8. Integrity Verifier

## 66. Rollback

**Boundary:** Artifact rollback is Stage 16; execution rollback is Stage 14.
**Source / Authority:** Stage 16 / Stage 14

**الوظيفة: Artifact Native / Execution Stage 14.**

1. Snapshot Builder
2. Version Resolver
3. Artifact Rollback Trigger
4. Restore Engine
5. Validation
6. Post-Rollback Test
7. Rollback Log
8. Rollback Domain Classifier

# M — Advanced Learning

## 67. Reinforcement Learning

**Boundary:** Learning proposal/evaluation; deployment/execution remains downstream.
**Source / Authority:** Stage 13 / Stage 14

1. Environment Builder
2. State Space
3. Action Space
4. Reward Function
5. Policy Network
6. Value Network
7. Training Loop
8. Evaluation
9. Model Save/Load
10. Version Tracker
11. Safety Guard
12. RL Audit
13. Rollback
14. Deployment Requester
15. Monitoring
16. Failure Handler
17. Recovery
18. Explanation
19. RL Report
20. RL Audit

## 68. Genetic Algorithms

**Boundary:** Evolution method is bot-specific; deployment remains bounded.
**Source / Authority:** Stage 16

1. Population Initializer
2. Fitness Function
3. Selection
4. Crossover
5. Mutation
6. Generation Loop
7. Convergence Check
8. Save/Load
9. Version Tracker
10. GA Audit
11. Safety Guard
12. Rollback
13. Report
14. Deployment Requester
15. GA Audit

## 69. Bayesian Optimization

**Boundary:** Bot-specific optimization.
**Source / Authority:** Stage 16

1. Prior Builder
2. Acquisition Function
3. Posterior Updater
4. Parameter Space
5. Trial Runner
6. Convergence Check
7. Save/Load
8. Version Tracker
9. BO Audit
10. Safety Guard
11. Report
12. BO Audit

# N — Portfolio & Risk

## 70. Portfolio Optimization

**Boundary:** Bot portfolio proposal/consumption only.
**Source / Authority:** Stage 13

**الوظيفة: Bot Proposal / Consumption فقط.**

1. Asset Universe
2. Return Estimator
3. Risk Estimator
4. Optimizer
5. Constraint Handler
6. Rebalance Trigger
7. Performance Tracker
8. Version Tracker
9. Portfolio Audit
10. Report
11. Rollback
12. Recovery
13. Benchmark
14. Comparison

## 71. Capital Allocation

**Boundary:** Bot allocation proposal; authorization/enforcement downstream.
**Source / Authority:** Stage 13 / Stage 14

**الوظيفة: Bot Proposal / Consumption فقط.**

1. Capital Pool
2. Bot Selector
3. Weight Calculator
4. Constraint Checker
5. Rebalance Logic
6. Performance Tracker
7. Allocation Log
8. Allocation Audit
9. Rollback
10. Version Tracker

## 72. Risk Parity

**Boundary:** Bot risk proposal; enforcement downstream.
**Source / Authority:** Stage 13 / Stage 14

**الوظيفة: Bot Proposal / Consumption فقط.**

1. Risk Estimator
2. Weight Calculator
3. Constraint Handler
4. Rebalance Trigger
5. Performance Tracker
6. Version Tracker
7. Risk Parity Audit
8. Report

## 73. Kelly Criterion

**Boundary:** Bot sizing proposal; enforcement downstream.
**Source / Authority:** Stage 13 / Stage 14

**الوظيفة: Bot Proposal / Consumption فقط.**

1. Win Rate Input
2. R:R Input
3. Kelly Calculator
4. Fractional Multiplier
5. Bounds Checker
6. Approval Gate
7. Kelly Log
8. Kelly Audit

## 74. Drawdown Protection

**Boundary:** Protection logic/request; enforcement Stage 14.
**Source / Authority:** Stage 14

**الوظيفة: Logic Native / Enforcement Stage 14.**

1. Drawdown Tracker
2. Threshold Checker
3. Protection Request Builder
4. Recovery Trigger
5. Notification Sender
6. Drawdown Log
7. Protection Audit
8. Version Tracker
9. Rollback
10. Protection Escalation Contract

## 75. Equity Curve Management

**Boundary:** Bot equity analysis.
**Source / Authority:** Stage 16

1. Equity Tracker
2. Curve Analyzer
3. Trend Detector
4. Anomaly Detector
5. Adjustment Suggester
6. Version Tracker
7. Curve Report
8. Curve Audit
9. Rollback
10. Recovery

# O — Advanced Market Analysis

## 76. Correlation Matrix

**Boundary:** Consumes market relationship data; no parallel market intelligence authority.
**Source / Authority:** Stage 15

1. Asset Selector
2. Window Selector
3. Correlation Calculator
4. Matrix Builder
5. Regime Comparator
6. Anomaly Detector
7. Version Tracker
8. Matrix Report
9. Correlation Audit

## 77. Position Sizing

**Boundary:** Sizing proposal; application/enforcement Stage 14.
**Source / Authority:** Stage 14

**الوظيفة: Sizing Proposal — التطبيق عبر Stage 14.**

1. Account Balance Reader
2. Risk Percent Setter
3. SL Calculator
4. Lot Calculator
5. Bounds Checker
6. Volatility Adjuster
7. Version Tracker
8. Sizing Authority Boundary

## 78. Market Microstructure

**Boundary:** Consumes microstructure inputs from Stage 15.
**Source / Authority:** Stage 15

1. Order Book Reader
2. Depth Analyzer
3. Spread Analyzer
4. Imbalance Detector
5. Liquidity Scorer
6. Version Tracker
7. Microstructure Report
8. Microstructure Audit
9. Regime Comparator
10. Historical Reference
11. Real-Time Tracker

## 79. Order Flow Analysis

**Boundary:** Consumes order-flow/market-intelligence inputs.
**Source / Authority:** Stage 15

1. Tick Reader
2. Volume Reader
3. Delta Calculator
4. Imbalance Detector
5. Absorption Detector
6. Exhaustion Detector
7. Signal Generator
8. Version Tracker
9. Order Flow Report
10. Order Flow Audit
11. Historical Reference
12. Regime Comparator
13. Real-Time Tracker
14. Failure Handler

## 80. Volume Profile

**Boundary:** Consumes volume-profile market intelligence.
**Source / Authority:** Stage 15

1. Data Aggregator
2. POC Calculator
3. Value Area Finder
4. HVN/LVN Detector
5. Signal Generator
6. Version Tracker
7. Volume Report
8. Volume Audit
9. Regime Comparator

# P — Macro & Events

## 81. VWAP Strategies

**Boundary:** Bot VWAP strategy; market inputs from Stage 15.
**Source / Authority:** Stage 15

1. VWAP Calculator
2. Anchor Selector
3. Deviation Bands
4. Signal Generator
5. Regime Comparator
6. Version Tracker
7. VWAP Report
8. VWAP Audit

## 82. Intermarket Analysis

**Boundary:** Consumes intermarket intelligence.
**Source / Authority:** Stage 15

1. Market Selector
2. Correlation Tracker
3. Lead-Lag Detector
4. Signal Generator
5. Version Tracker
6. Intermarket Report
7. Intermarket Audit
8. Regime Comparator
9. Historical Reference

## 83. Sentiment Analysis

**Boundary:** Consumes research/sentiment inputs; no source authority.
**Source / Authority:** Stage 12 / Stage 15

1. Source Selector
2. Text Collector
3. Sentiment Extractor
4. Aggregator
5. Time-Weighted Score
6. Regime Comparator
7. Signal Generator
8. Version Tracker
9. Sentiment Report
10. Sentiment Audit

# Q — Advanced Execution

## 84. Economic Calendar

**Boundary:** Consumes calendar data; pause request only, Stage 14 controls execution.
**Source / Authority:** Stage 12 / Stage 14

**الوظيفة: Pause Requester — لا ينفذ Pause.**

1. Source Selector
2. Event Fetcher
3. Impact Classifier
4. Time Tracker
5. Alert Trigger
6. Trading Pause Requester
7. Calendar Store
8. Event-to-Execution Boundary

## 85. News Impact

**Boundary:** Consumes news/market intelligence.
**Source / Authority:** Stage 12 / Stage 15

1. News Fetcher
2. Relevance Filter
3. Impact Predictor
4. Pre-News State
5. Post-News State
6. Signal Generator
7. News Report
8. News Audit

## 86. Slippage Modeling

**Boundary:** Consumes execution-cost modeling; no broker authority.
**Source / Authority:** Stage 14

1. Historical Slippage Reader
2. Slippage Predictor
3. Model Calibrator
4. Simulation Injector
5. Version Tracker
6. Slippage Report
7. Slippage Audit
8. Regime Comparator

## 87. Latency Simulation

**Boundary:** Bot latency testing.
**Source / Authority:** Stage 16

1. Latency Reader
2. Distribution Builder
3. Simulation Injector
4. Version Tracker
5. Latency Report
6. Latency Audit

## 88. Broker Rules

**Boundary:** Broker rules are reference/consumer data; Stage 14 owns broker/execution authority.
**Source / Authority:** Stage 14

1. Rule Extractor
2. Symbol Spec Reader
3. Margin Calculator
4. Swap Calculator
5. Trading Hours
6. Rule Validator
7. Rule Store
8. Broker Audit

# R — Bot Architecture

## 89. Session Management

**Boundary:** Bot session logic.
**Source / Authority:** Stage 16

1. Session Definer
2. Time Zone Handler
3. Session Filter
4. Overlap Detector
5. Behavior Adapter
6. Version Tracker
7. Session Report
8. Session Audit

## 90. Weekend Gap Handling

**Boundary:** Bot weekend-gap logic.
**Source / Authority:** Stage 16

1. Gap Detector
2. Historical Gap Reader
3. Position Handler
4. Pre-Weekend Logic
5. Post-Weekend Logic
6. Gap Report
7. Gap Audit

## 91. Partial Fills

**Boundary:** Monitoring only; fill ownership Stage 14.
**Source / Authority:** Stage 14

**الوظيفة: مراقبة فقط — Ownership لـStage 14.**

1. Fill Detector
2. Partial Handler
3. Retry Logic
4. Position Tracker
5. Version Tracker
6. Fill Event Ownership Contract

## 92. Bot Memory

**Boundary:** Runtime bot memory consumes core cognitive context; remains bot-specific.
**Source / Authority:** Stage 2 / Core

**الوظيفة: Runtime Memory — منفصل عن Long-Term Memory.**

1. Short-Term Store
2. Context Linker
3. Recall Engine
4. Relevance Filter
5. Conflict Detector
6. Memory Decay
7. Memory Report
8. Memory Audit
9. Version Tracker

## 93. Signal Fusion

**Boundary:** Bot-specific signal fusion; handoff to Stage 13 for decision.
**Source / Authority:** Stage 15 / Stage 13

**الوظيفة: Bot-specific Fusion — Handoff لـStage 13.**

1. Signal Collector
2. Weight Assigner
3. Conflict Resolver
4. Consensus Builder
5. Confidence Scorer
6. Version Tracker
7. Fusion Report
8. Fusion Audit
9. Regime Comparator
10. Signal-to-Decision Handoff Contract

# S — Anomaly & Regime

## 94. Anomaly Detection

**Boundary:** Consumes anomaly intelligence; no parallel market authority.
**Source / Authority:** Stage 15

1. Baseline Builder
2. Deviation Detector
3. Anomaly Classifier
4. Severity Scorer
5. Alert Trigger
6. Version Tracker
7. Anomaly Report
8. Anomaly Audit
9. Recovery Handler

## 95. Regime Detection

**Boundary:** Consumes regime intelligence; no parallel market authority.
**Source / Authority:** Stage 15

1. Feature Extractor
2. Regime Classifier
3. Transition Detector
4. Confidence Scorer
5. Regime Store
6. Version Tracker
7. Regime Report
8. Regime Audit
9. Alert Trigger
10. Recovery Handler

## 96. Bot Explainability

**Boundary:** Assembles explainability from attributed evidence/reasoning; no independent decision trace authority.
**Source / Authority:** Stage 11 / Stage 13

**الوظيفة: Consumer/Assembler — لا Decision Trace مستقل.**

1. Decision Tracer (يتتبع قرار Stage 13)
2. Evidence Linker
3. Reasoning Summary
4. Confidence Explainer
5. Alternative Disclosure
6. Counterfactual Reporter
7. Explanation Builder
8. Version Tracker
9. Explainability Report
10. Explainability Provenance Contract

# T — Operations

## 97. Capital Protection

**Boundary:** Protection logic/request; enforcement Stage 14.
**Source / Authority:** Stage 14

**الوظيفة: Logic Native / Enforcement Stage 14.**

1. Capital Floor
2. Daily Loss Limit
3. Weekly Loss Limit
4. Monthly Loss Limit
5. Auto-Pause Request
6. Notification
7. Protection Report
8. Protection Audit
9. Version Tracker
10. Capital Protection Enforcement Contract

## 98. Emergency Protocols

**Boundary:** Emergency request only; Stage 14 executes.
**Source / Authority:** Stage 14

**الوظيفة: Request فقط — لا Execution.**

1. Emergency Detector
2. Emergency Request Builder
3. Notification
4. Recovery Planner
5. Emergency Log
6. Emergency Audit
7. Recovery Handler
8. Emergency Boundary Contract

## 99. Live Adjustments

**Boundary:** Proposal only; execution/authorization downstream.
**Source / Authority:** Stage 14

**الوظيفة: Proposal Only.**

1. Change Detector
2. Impact Analyzer
3. Proposal Builder
4. Approval Gate
5. Validation
6. Rollback Point
7. Adjustment Log
8. Live Adjustment Audit
9. Version Tracker
10. Adjustment Proposal Contract

## 100. Bot Retirement

**Boundary:** Lifecycle logic is Stage 16; positions handoff/execution Stage 14.
**Source / Authority:** Stage 16 / Stage 14

**الوظيفة: Logic Native / Positions Handoff لـStage 14.**

1. Retirement Trigger
2. Performance Check
3. Graceful Shutdown
4. Position Handoff Requester
5. Knowledge Extractor
6. Retirement Log
7. Retirement Audit
8. Retirement Execution Boundary

# U — Bot Identity & Collaboration

## 101. Bot Identity & Signature

**Boundary:** Bot identity/signature.
**Source / Authority:** Stage 16

1. ID Generator
2. Signature Builder
3. Uniqueness Checker
4. Signature Store
5. Verification Engine
6. Identity Audit

## 102. Bot Long-Term Memory

**Boundary:** Long-term bot memory consumes core persistence/cognition interfaces.
**Source / Authority:** Stage 2 / Core

**الوظيفة: Long-Term Memory — منفصل عن #99.**

1. Memory Store
2. Indexer
3. Recall Engine
4. Relevance Filter
5. Decay Handler
6. Conflict Detector
7. Memory Report
8. Memory Audit
9. Version Tracker
10. Recovery Handler

## 103. Bot Internal Deliberation Loop

**Boundary:** Bot internal deliberation produces a proposal; Stage 13 owns decision.
**Source / Authority:** Stage 13

**الوظيفة: Proposal → Stage 13.**

1. Proposal Builder
2. Critic
3. Risk Checker
4. Alternative Generator
5. Vote Collector
6. Consensus Builder
7. Final Proposal
8. Deliberation Log
9. Deliberation Audit
10. Deliberation-to-Decision Boundary

## 104. Bot Authority Matrix

**Boundary:** Bot-specific authority matrix, bounded by global stage authority.
**Source / Authority:** Stage 16

1. Role Definer
2. Permission Mapper
3. Boundary Enforcer
4. Escalation Handler
5. Audit Logger
6. Authority Report
7. Authority Audit
8. Version Tracker

## 105. Bot-to-Bot Protocol

**Boundary:** Bot-to-bot protocol.
**Source / Authority:** Stage 16

1. Message Format
2. Auth Verifier
3. State Sync
4. Priority Handler
5. Conflict Resolver
6. Retry Handler
7. Protocol Log
8. Protocol Audit
9. Version Tracker
10. Recovery Handler

## 106. Bot Team Coordination

**Boundary:** Bot team coordination; no cross-stage authority transfer.
**Source / Authority:** Stage 16

1. Team Registry
2. Role Assignment
3. Task Allocator
4. State Share
5. Conflict Resolver
6. Leader Election
7. Coordination Log
8. Recovery Handler
9. Version Tracker
10. Coordination Audit
11. Performance Tracker

# V — Bot Role & Evolution

## 107. Bot Role Assignment

**Boundary:** Bot role assignment.
**Source / Authority:** Stage 16

1. Role Definer
2. Capability Matcher
3. Assignment Logic
4. Rotation Handler
5. Conflict Resolver
6. Role Audit
7. Version Tracker

## 108. Bot Learning Loop

**Boundary:** Bot learning loop consumes evaluation and downstream approval/execution boundaries.
**Source / Authority:** Stage 13 / Stage 14

1. Outcome Collector
2. Performance Analyzer
3. Pattern Detector
4. Adjustment Suggester
5. Validation
6. Learning Log
7. Rollback Point
8. Recovery Handler
9. Version Tracker
10. Learning Report
11. Learning Audit

## 109. Bot Fitness & Evolution

**Boundary:** Bot fitness/evolution; deployment remains bounded.
**Source / Authority:** Stage 16

1. Fitness Function
2. Population Builder
3. Selection Logic
4. Crossover Engine
5. Mutation Engine
6. Generation Loop
7. Convergence Check
8. Lineage Tracker
9. Version Tracker
10. Evolution Report
11. Evolution Audit

## 110. Bot Performance Attribution

**Boundary:** Bot performance attribution.
**Source / Authority:** Stage 16

1. Trade Logger
2. Factor Splitter
3. Contribution Calculator
4. Ranking Engine
5. Report Builder
6. Version Tracker
7. Attribution Audit
8. Recovery Handler

## 111. Bot Life & Lifecycle Governance

**Boundary:** Bot lifecycle governance.
**Source / Authority:** Stage 16

1. Lifecycle State Machine
2. Transition Rules
3. Approval Gate
4. Audit Logger
5. Version Tracker
6. Retirement Hook
7. Archive Handler
8. Lifecycle Report
9. Lifecycle Audit
10. Recovery Handler

---


---

# الجزء الرابع — Cleanup, Boundary & Authority Rules

## 4.1 Net Cleanup (9 item-level removals/merges)

1. Source Crawler → merged into Discovery Engine.
2. Multi-Source Research → removed; Stage 12 remains the Research Authority.
3. Deliberation Loop → merged into Bot Internal Deliberation Loop.
4. Authority Matrix → merged into Bot Authority Matrix.
5. Learning Loop → merged into Bot Learning Loop.
6. Bot Conflict Resolution → merged into Conflict Resolution Between Concepts.
7. MT5 Converter → merged into Pine ↔ MQL5 ↔ Python Translator as artifact construction/validation.
8. Multi-Broker Abstraction → merged into Cross-Stage Integration as interface/artifact capability.
9. Multi-Account → merged into Cross-Stage Integration as account-routing interface.

## 4.2 Explicit Authority Corrections

1. **Discovery Engine:** Consumer only; Stage 12 owns discovery/source authority.
2. **Parameter Research Engine:** Consumer only; requests research and consumes approved Stage 12/13 outputs.
3. **MT5 conversion capability:** Artifact construction/validation only; deployment/execution belongs to Stage 14.
4. **Multi-broker capability:** Artifact/interface consumption only; broker authority belongs to Stage 14.
5. **Massive Source Access Layer:** Consumer only; Stage 12 owns source access.
6. **Web-Wide Authorization:** Central authorization policy/service; individual bots cannot grant web authorization to themselves.
7. **Execute Button + Input:** UI Native; any financial command routes to Stage 14.
8. **Broker Rules:** Reference/consumer only; broker/execution authority belongs to Stage 14.

## 4.3 Global Boundary

**Stage 16 = Bot Construction + Coordination + Consumption.**

Stage 16 may build, specialize, test, persist, coordinate, simulate, and consume. It may not create parallel authority for cognition, evidence, research, decision, authorization, execution, broker integration, or market-signal processing.

**Proposal ≠ Decision.**  
**Request ≠ Execution.**  
**Signal ≠ Decision.**  
**Execution does not occur inside Stage 16.**

## 4.4 Final Status

- **Status:** Living Document — Editable
- **Revision:** v2 (Cleaned)
- **Items:** 111
- **Additions:** ~1241 (content-derived count after removing 109 additions from the 9 removed/merged item shells)
- **Modification:** Allowed — Add / Edit / Delete without constitutional re-approval
- **Previous Version:** STAGE16-BOT-INTELLIGENCE.md
- **Code:** ❌
- **Execution:** ❌

**Previous Version remains intact. This v2 is a living design reference and is not a constitutional freeze.**
