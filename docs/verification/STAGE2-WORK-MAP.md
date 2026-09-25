# Stage 2 — Work Map

Status: Living Document — Editable
Stage: 2 — Central AI + Cognitive Intelligence
Total Capabilities: 72
Status: Implementation Pending
Execution: Not Started

## 1. المتفق عليه والمعتمد

### أ. الطبقتان
- الطبقة 1: AmarStageTwoEngine — مغلقة.
- الطبقة 2: Cognitive Intelligence — تصميم.
- الدمج بين الطبقتين إضافي؛ لا استبدال ولا حذف للطبقة 1.

### ب. الأرقام
- 72 Capability.
- 14 Engine.
- 3 Gates.
- 397 إضافة داخلية.

> ملاحظة: الـ397 Addition مأخوذة من وثيقة Capability Breakdown، وهي تفصيل تنفيذ مرجعي قابل للتجميع أثناء التنفيذ وليست 397 متطلبًا دستوريًا مستقلًا.

### ج. الحدود
- لا قرار نهائي → Stage 13.
- لا تنفيذ أو تفويض تنفيذ → Stage 14.
- لا تحقق/اعتماد للأدلة → Stage 11.

Stage 2 يستطيع الفهم والتحليل والاستدلال والفرضيات والمحاكاة والمراجعة الذاتية ضمن حدود التصميم، لكنه لا يستولي على سلطة المراحل الأخرى.

## 2. خارطة العمل — ترتيب التنفيذ

### المرحلة الأولى — إكمال الناقص
26 Capability = Partial. يوجد أساس فعلي في الكود، لكنه لا يغطي كامل Capability أو كامل إضافاتها الداخلية. تُستكمل الفجوات داخل الأساس الموجود عند بدء التنفيذ.

### المرحلة الثانية — إكمال Extend-Real
2 Capability = Extend-Real:
1. Intent Understanding
2. Internal Critique
التوسعة محدودة نسبيًا مقارنة بالبناء من الصفر.

### المرحلة الثالثة — البناء من الصفر
18 Capability = New-Hidden. توجد أسماء أو إشارات غير مباشرة، لكن الفحص لم يثبت تنفيذًا فعليًا يحقق نطاقها؛ لذلك تُعامل كجديدة عند التنفيذ.

### ملاحظة تصحيحية
كان Collision Audit السابق يصف القائمة بأنها 45 Capability، لكن العد الفعلي للقائمة المفحوصة كان 46. هذا المستند يعتمد الأدلة المصدرية: 26 Partial + 2 Extend-Real + 18 New-Hidden = 46. لا توجد حاليًا Duplicate-Real مثبتة ضمن هذه المجموعة.

## 3. جدول Capability Work Map

| # | Capability | المرحلة | الحكم | الملف الموجود | الإضافات الموجودة | الإضافات الناقصة | الأولوية |
|---:|---|---|---|---|---|---|---|
| 1 | Intent Understanding | 2 | Extend-Real | AmarIntentUnderstanding.kt | 4/6 | 5–6 | P1 |
| 2 | Semantic & Literal Understanding | 3 | New-Hidden | لا يوجد | 0/7 | 1–7 | P1 |
| 3 | Context Awareness | 1 | Partial | AmarAgentOrchestrator.kt; AmarAgentSession.kt | 4/7 | 4–7 | P1 |
| 4 | Temporal Awareness | 1 | Partial | AmarAgentOrchestrator.kt; evidence freshness | 2/6 | 4–6 | P2 |
| 5 | Goal Awareness | 1 | Partial | AmarAgentPlanner.kt | 1/6 | 2–6 | P1 |
| 6 | Knowledge-State Awareness | 1 | Partial | AmarKnowledgeEngine.kt; AmarReasoning.kt | 3/6 | 3,5–6 | P1 |
| 7 | Knowledge-Gap Detection | 1 | Partial | AmarAgentCore.kt; AmarSourceVerifier.kt | 3/5 | 3–5 | P1 |
| 8 | Uncertainty Awareness | 1 | Partial | AmarReasoning.kt; AmarAgentCore.kt | 3/5 | 2,4 | P1 |
| 9 | Evidence-Need Awareness | 1 | Partial | AmarAgentPlanner.kt; Stage2 hardening | 3/5 | 3,5 | P1 |
| 10 | Structural Analysis | 1 | Partial | AmarAgentPlanner.kt; AmarAgentOrchestrator.kt | 2/6 | 3–6 | P1 |
| 11 | Pattern Analysis | 1 | Partial | AmarReasoning.kt; evidence utilities | 2/5 | 3–5 | P2 |
| 12 | Causal Candidate Analysis | 3 | New-Hidden | لا يوجد | 0/6 | 1–6 | P1 |
| 13 | Consequence Analysis | 3 | New-Hidden | لا يوجد | 0/7 | 1–7 | P1 |
| 14 | Information Sufficiency Analysis | 1 | Partial | AmarSourceVerifier.kt; AmarAgentCore.kt | 3/5 | 4–5 | P1 |
| 15 | Hypothesis & Counter-Hypothesis Reasoning | 1 | Partial | AmarReasoning.kt; AmarAgentCritic.kt | 3/6 | 3–6 | P1 |
| 16 | Hypothesis Falsification | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P1 |
| 17 | Counterfactual Reasoning | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P2 |
| 18 | Assumption Detection | 1 | Partial | AmarAgentCritic.kt; AmarReasoning.kt | 2/5 | 3–5 | P1 |
| 19 | Proof-Bound Cognition | 1 | Partial | AmarReasoning.kt; Stage2 hardening | 4/8 | 5–8 | P1 |
| 20 | Contradiction Detection & Handling | — | خارج الـ46 المفحوصة | AmarReasoning.kt; verification | غير مثبت كاكتمال | لا يُحذف | P1 |
| 21 | Reasoning Chain Validation | 1 | Partial | AmarAgentCritic.kt; AmarReasoning.kt | 4/6 | 4–6 | P1 |
| 22 | Cross-Reference Resolution | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P2 |
| 23 | Market Context Understanding | 1 | Partial | MarketAnalyzer.kt; AmarStageTwoEngine.kt | 4/6 | 4,6 | P1 |
| 24 | Risk Language Detection | 1 | Partial | AmarIntentUnderstanding.kt; AmarRiskEngine.kt | 2/6 | 2–6 | P1 |
| 25 | Strategy Reference Resolution | 1 | Partial | AmarAgentPlanner.kt | 2/5 | 2–5 | P2 |
| 26 | Position State Awareness | 1 | Partial | trading/MT5 context components | 3/6 | 4–6 | P1 |
| 27 | Source Credibility Assessment | — | خارج الـ46 المفحوصة | AmarSourceVerifier.kt; source quality | غير مثبت كاكتمال | لا يُحذف | P1 |
| 28 | Input Bias Detection | 3 | New-Hidden | لا يوجد | 0/6 | 1–6 | P2 |
| 29 | User-State Modeling | 3 | New-Hidden | لا يوجد | 0/7 | 1–7 | P1 |
| 30 | Emotional Signal Interpretation | 3 | New-Hidden | لا يوجد | 0/6 | 1–6 | P2 |
| 31 | Tone Shift Detection | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P2 |
| 32 | Emoji & Symbol Context Interpretation | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P3 |
| 33 | Implicit Meaning Detection | 1 | Partial | AmarIntentUnderstanding.kt; AmarReasoning.kt | 2/6 | 3–6 | P1 |
| 34 | Hint Interpretation | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P3 |
| 35 | Silence Interpretation | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P3 |
| 36 | Attention & Relevance Awareness | 1 | Partial | AmarReasoning.kt; retrieval relevance | 2/5 | 3–5 | P2 |
| 37 | Relationship Awareness | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P2 |
| 38 | Continuity Awareness | 1 | Partial | AmarAgentSession.kt; AmarAgentOrchestrator.kt | 3/5 | 3–5 | P1 |
| 39 | Cognitive State Persistence | 1 | Partial | AmarAgentSession.kt; memory components | 3/6 | 3–6 | P1 |
| 40 | Cognitive Position Revision | 1 | Partial | session/reasoning state | 2/5 | 3–5 | P1 |
| 41 | Theory-of-Mind Modeling | 3 | New-Hidden | لا يوجد | 0/7 | 1–7 | P2 |
| 42 | Communication-Style Awareness | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P2 |
| 43 | User-Context Modeling | 1 | Partial | AmarAgentContext; AmarAgentSession.kt | 2/5 | 3–5 | P1 |
| 44 | Empathy Modeling | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P3 |
| 45 | Multi-Perspective Analysis | 1 | Partial | AmarStageTwoEngine.kt | 3/5 | 3–5 | P1 |
| 46 | Knowledge Synthesis | 1 | Partial | AmarKnowledgeEngine.kt; AmarReasoning.kt | 2/6 | 3–6 | P1 |
| 47 | Mental Model Construction | 3 | New-Hidden | لا يوجد | 0/6 | 1–6 | P1 |
| 48 | Sarcasm Detection | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P3 |
| 49 | Irony Detection | 3 | New-Hidden | لا يوجد | 0/4 | 1–4 | P3 |
| 50 | Humor Recognition | 3 | New-Hidden | لا يوجد | 0/4 | 1–4 | P3 |
| 51 | Cultural Context Interpretation | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P3 |
| 52 | Multi-Language Switching | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P2 |
| 53 | Code-Switching Detection | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P2 |
| 54 | Pause Detection | 3 | New-Hidden | لا يوجد | 0/4 | 1–4 | P3 |
| 55 | Hesitation Detection | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P3 |
| 56 | Confirmation Signal Detection | 1 | Partial | orchestration/session components | 2/5 | 3–5 | P1 |
| 57 | Correction Detection | 1 | Partial | session/state update components | 2/5 | 3–5 | P1 |
| 58 | Meta-Communication Understanding | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P2 |
| 59 | Recursive Deep Thinking | 1 | Partial | AmarAgentCritic.kt; AmarReasoning.kt | 2/5 | 2–5 | P2 |
| 60 | Second-Order Thinking | 1 | Partial | reasoning/consequence basis | 1/5 | 2–5 | P2 |
| 61 | Internal Critique | 2 | Extend-Real | AmarAgentCritic.kt | 4/6 | 5–6 | P1 |
| 62 | Blind-Spot Detection | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P2 |
| 63 | Cognitive Bias Guarding | 1 | Partial | AmarAgentCritic.kt; reasoning | 1/7 | 2–7 | P2 |
| 64 | Scenario Branching & Mental Simulation | 1 | Partial | Stage2 planning/simulation boundaries | 2/7 | 3–7 | P2 |
| 65 | Failure & Recovery Simulation | 1 | Partial | recovery components | 1/6 | 2–6 | P2 |
| 66 | Forecasting & Prediction Awareness | 3 | New-Hidden | لا يوجد Cognitive implementation | 0/6 | 1–6 | P2 |
| 67 | Meta-Cognition | 1 | Partial | AmarAgentCritic.kt | 2/7 | 3–7 | P2 |
| 68 | Awareness Quality & Depth Assessment | 3 | New-Hidden | لا يوجد | 0/5 | 1–5 | P3 |
| 69 | Ignorance & Knowledge-Boundary Awareness | 1 | Partial | AmarAgentCore.kt; AmarReasoning.kt | 3/6 | 2,4–6 | P1 |
| 70 | Cognitive Provenance & Audit Continuity | 1 | Partial | AmarAgentSession.kt; provenance components | 3/6 | 3–6 | P1 |
| 71 | Concept Mapping | 3 | New-Hidden | لا يوجد | 0/6 | 1–6 | P2 |
| 72 | Contradiction Synthesis | 1 | Partial | AmarReasoning.kt; contradiction handling | 2/6 | 3–6 | P1 |

## 4. تفصيل الإضافات

المصدر الحرفي لقائمة الـ397 Addition هو STAGE2-CAPABILITY-BREAKDOWN.md. أدناه خريطة العمل التنفيذية: المنجز هو ما ثبت source-level في Gap Analysis؛ الناقص هو ما لم يثبت تنفيذه؛ والمطلوب هو استكماله دون إنشاء مسار موازٍ عندما يوجد أساس قابل للتوسعة.

### Phase 1 — Trading
1 Intent: المنجز 1–4؛ الناقص 5–6.
2 Semantic/Literal: لا تنفيذ مثبت؛ المطلوب 1–7.
3 Context: المنجز 1–3؛ الناقص 4–7.
4 Temporal: المنجز 1–3؛ الناقص 4–6.
5 Goal: المنجز 1؛ الناقص 2–6.
6 Knowledge State: المنجز 1،2،4؛ الناقص 3،5،6.
7 Knowledge Gap: المنجز 1،2؛ الناقص 3–5.
8 Uncertainty: المنجز 1،3، جزئي من 5؛ الناقص 2،4،5.
9 Evidence Need: المنجز 1،2،4؛ الناقص 3،5.
10 Structural: المنجز 1،2؛ الناقص 3–6.
11 Pattern: المنجز 1–2 جزئيًا؛ الناقص 3–5.
12 Causal Candidate: لا تنفيذ مثبت؛ المطلوب 1–6.
13 Consequence: لا تنفيذ مثبت؛ المطلوب 1–7.
14 Sufficiency: المنجز 2–3؛ الناقص 1،4–5.
15 Hypothesis: المنجز 1–2،5؛ الناقص 3،4،6.
16 Falsification: لا تنفيذ مثبت؛ المطلوب 1–5.
17 Counterfactual: لا تنفيذ مثبت؛ المطلوب 1–5.
18 Assumption: المنجز 1،4؛ الناقص 2،3،5.
19 Proof-Bound: المنجز 1–4؛ الناقص 5–8.
20 Contradiction: خارج الـ46؛ لا يُحذف؛ يحتاج Gap Audit مستقلًا لكل 10 إضافات.
21 Reasoning Chain: المنجز 1–3؛ الناقص 4–6.
22 Cross-Reference: لا تنفيذ مثبت؛ المطلوب 1–5.
23 Market Context: المنجز 1–3،5؛ الناقص 4،6.
24 Risk Language: المنجز 1،6 بشكل غير مباشر؛ الناقص 2–5.
25 Strategy Reference: المنجز 1،5 جزئيًا؛ الناقص 2–4.
26 Position State: المنجز 1–3؛ الناقص 4–6.
27 Source Credibility: خارج الـ46؛ لا يُحذف؛ يحتاج Gap Audit مستقلًا لكل 5 إضافات.
28 Input Bias: لا تنفيذ مثبت؛ المطلوب 1–6.

### Phase 2 — Awareness
29 User-State: لا تنفيذ مثبت؛ المطلوب 1–7.
30 Emotional: لا تنفيذ مثبت؛ المطلوب 1–6.
31 Tone Shift: لا تنفيذ مثبت؛ المطلوب 1–5.
32 Emoji/Symbol: لا تنفيذ مثبت؛ المطلوب 1–5.
33 Implicit Meaning: المنجز 1 جزئيًا؛ الناقص 2–6.
34 Hint: لا تنفيذ مثبت؛ المطلوب 1–5.
35 Silence: لا تنفيذ مثبت؛ المطلوب 1–5.
36 Attention/Relevance: المنجز 2؛ الناقص 1،3–5.
37 Relationship: لا تنفيذ مثبت؛ المطلوب 1–5.
38 Continuity: المنجز 1–2؛ الناقص 3–5.
39 Cognitive Persistence: المنجز 1؛ الناقص 2–6.
40 Position Revision: المنجز 1؛ الناقص 2–5.
41 Theory of Mind: لا تنفيذ مثبت؛ المطلوب 1–7.
42 Communication Style: لا تنفيذ مثبت؛ المطلوب 1–5.
43 User Context: المنجز 1–2؛ الناقص 3–5.
44 Empathy: لا تنفيذ مثبت؛ المطلوب 1–5.
45 Multi-Perspective: المنجز 1–2؛ الناقص 3–5.
46 Knowledge Synthesis: المنجز 1؛ الناقص 2–6.
47 Mental Model: لا تنفيذ مثبت؛ المطلوب 1–6.
48 Sarcasm: لا تنفيذ مثبت؛ المطلوب 1–5.
49 Irony: لا تنفيذ مثبت؛ المطلوب 1–4.
50 Humor: لا تنفيذ مثبت؛ المطلوب 1–4.
51 Cultural: لا تنفيذ مثبت؛ المطلوب 1–5.
52 Multi-Language: لا تنفيذ مثبت؛ المطلوب 1–5.
53 Code-Switching: لا تنفيذ مثبت؛ المطلوب 1–5.
54 Pause: لا تنفيذ مثبت؛ المطلوب 1–4.
55 Hesitation: لا تنفيذ مثبت؛ المطلوب 1–5.
56 Confirmation: المنجز 1–2؛ الناقص 3–5.
57 Correction: المنجز 1 جزئيًا؛ الناقص 2–5.
58 Meta-Communication: لا تنفيذ مثبت؛ المطلوب 1–5.

### Phase 3 — Deep
59 Recursive: المنجز 1؛ الناقص 2–5.
60 Second-Order: المنجز 1؛ الناقص 2–5.
61 Internal Critique: المنجز 1–4؛ الناقص 5–6.
62 Blind-Spot: لا تنفيذ مثبت؛ المطلوب 1–5.
63 Cognitive Bias Guarding: المنجز 1 جزئيًا؛ الناقص 2–7.
64 Scenario/Simulation: المنجز 1–2؛ الناقص 3–7.
65 Failure/Recovery: المنجز 1 جزئيًا؛ الناقص 2–6.
66 Forecasting: لا تنفيذ Cognitive مثبت؛ المطلوب 1–6.
67 Meta-Cognition: المنجز 1–2؛ الناقص 3–7.
68 Awareness Quality: لا تنفيذ مثبت؛ المطلوب 1–5.
69 Ignorance/Boundary: المنجز 1،3؛ الناقص 2،4–6.
70 Provenance: المنجز 1–2؛ الناقص 3–6.
71 Concept Mapping: لا تنفيذ مثبت؛ المطلوب 1–6.
72 Contradiction Synthesis: المنجز 1–2 جزئيًا؛ الناقص 3–6.

## 5. ترتيب التنفيذ الداخلي

### Phase 1 — Partial
P1 أولًا: #3 Context, #5 Goal, #6 Knowledge State, #7 Knowledge Gap, #8 Uncertainty, #9 Evidence Need, #10 Structural, #14 Sufficiency, #15 Hypothesis, #18 Assumption, #19 Proof-Bound, #21 Reasoning Chain, #23 Market Context, #24 Risk Language, #26 Position State, #33 Implicit Meaning, #38 Continuity, #39 Cognitive Persistence, #40 Position Revision, #43 User Context, #45 Multi-Perspective, #46 Knowledge Synthesis, #56 Confirmation, #57 Correction, #69 Knowledge Boundary, #70 Provenance.
P2 بعد الأساس: #4 Temporal, #11 Pattern, #25 Strategy Reference, #36 Attention, #59 Recursive, #60 Second-Order, #63 Bias Guarding, #64 Simulation, #65 Failure/Recovery, #67 Meta-Cognition.

### Phase 2 — Extend-Real
1. #1 Intent Understanding — بعد Context/Goal.
2. #61 Internal Critique — بعد Reasoning Chain/Hypothesis foundations.

### Phase 3 — New-Hidden
P1: #2 Semantic/Literal, #12 Causal Candidate, #13 Consequence, #16 Falsification, #22 Cross-Reference, #29 User-State, #47 Mental Model.
P2: #17 Counterfactual, #28 Input Bias, #30 Emotional, #31 Tone Shift, #37 Relationship, #41 Theory-of-Mind, #42 Communication Style, #52 Multi-Language, #53 Code-Switching, #58 Meta-Communication, #62 Blind-Spot, #66 Forecast.
P3: #32 Emoji/Symbol, #34 Hint, #35 Silence, #44 Empathy, #48 Sarcasm, #49 Irony, #50 Humor, #51 Cultural, #54 Pause, #55 Hesitation, #68 Awareness Quality, #71 Concept Mapping.

## 6. قواعد التنفيذ
1. ملف واحد لكل Capability عند بدء تنفيذها.
2. اختبار لكل Capability.
3. لا لمس الطبقة 1.
4. لا لمس الاختبارات القديمة.
5. كل Addition جديدة تُختبر.
6. لا إنشاء Stub قبل إثبات الحاجة من Gap Analysis.
7. لا إنشاء implementation موازٍ لملف قائم إذا كان التوسع الآمن ممكنًا.
8. لا نقل سلطة من Stage 11–16 إلى Stage 2.
9. Missing Data ≠ Contradiction.
10. Evidence ≠ Confidence ≠ Decision ≠ Execution.
11. كل Capability تحافظ على حالات عدم اليقين والحدود المعرفية المناسبة.
12. أي Duplicate حقيقي يُعالج فقط بعد إثباته source-level وبموافقة المالك ضمن مسار التنفيذ.

## 7. Definition of Done
- كود.
- اختبار مباشر يغطي السلوك والإخفاقات الأساسية.
- CI أخضر.
- لا Regression.
- توثيق التنفيذ.
- مطابقة حدود Stage 2.
- عدم تعديل أو كسر الطبقة القديمة أو اختبارات Stage 2 المغلقة.

الإغلاق الدستوري للـStage 2 لا ينتج تلقائيًا من اكتمال عدد من الملفات؛ يحتاج Evidence قابلًا للتحقق وفق قواعد المشروع.

## 8. Footer
Last Updated: 2026-09-25
Next Action: Begin Phase 1 — Extend-Real

## 9. Authority / Source
- STAGE2-COGNITIVE-INTELLIGENCE.md
- STAGE2-CAPABILITY-BREAKDOWN.md
- Source-level Collision/Gap Audit على main.
- هذا الملف Work Map تشغيلي قابل للتعديل، وليس بديلًا عن Master Constitution أو وثيقة التصميم الأصلية.