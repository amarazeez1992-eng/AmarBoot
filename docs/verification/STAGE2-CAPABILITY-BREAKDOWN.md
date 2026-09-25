# Stage 2 — Capability Breakdown

Status: Draft Breakdown — For Implementation Reference
Capabilities: 72
Internal Additions: 397
Phases: 163 Trading / 151 Awareness / 83 Deep
Note: Subject to consolidation during implementation.

> Draft only. This document is an implementation-reference decomposition of the 72 Stage 2 capabilities. It is not constitutional closure and does not authorize implementation. Consolidation may remove true duplicates during implementation.

## Phase 1 — Trading (163 Internal Additions)

### 1. Intent Understanding — 6
1. تصنيف نوع الطلب
2. استخراج النية الأساسية
3. استخراج النوايا الفرعية
4. تحديد أولوية النيات
5. كشف تغير النية
6. بناء نموذج النية

### 2. Semantic & Literal Understanding — 7
1. تحليل الكلمات
2. تحليل تركيب الجملة
3. استخراج المعنى الحرفي
4. استخراج المعنى المقصود
5. كشف الغموض الدلالي
6. تحليل المعنى المجازي
7. إنتاج بدائل المعنى

### 3. Context Awareness — 7
1. تحديد الموضوع النشط
2. ربط الرسالة بالسياق السابق
3. تحديد المهمة الحالية
4. تحديد المرحلة المسؤولة
5. حل الأصل المالي
6. حل الإطار الزمني
7. بناء السياق النشط

### 4. Temporal Awareness — 6
1. تحديد الزمن
2. ترتيب الأحداث
3. حساب عمر المعلومة
4. حل التعبيرات الزمنية
5. كشف التعارض الزمني
6. فصل التوقع عن الواقع

### 5. Goal Awareness — 6
1. استخراج الهدف النهائي
2. استخراج الأهداف الفرعية
3. ترتيب الأولويات
4. كشف تعارض الأهداف
5. كشف انحراف الهدف
6. بناء Goal Model

### 6. Knowledge-State Awareness — 6
1. تصنيف المعرفة
2. تمييز المعرفة عن الاستنتاج
3. تمييز الفرضية عن الحقيقة
4. تتبع المصدر
5. تتبع عمر المعرفة
6. بناء Knowledge State

### 7. Knowledge-Gap Detection — 5
1. استخراج المعلومات المطلوبة
2. مقارنة المطلوب بالمتاح
3. تصنيف الفجوة
4. تحديد حرجة الفجوة
5. توليد طلب البيانات الناقصة

### 8. Uncertainty Awareness — 5
1. اكتشاف عدم اليقين
2. تحديد مصدر عدم اليقين
3. تصنيف مستوى الحسم
4. تحديد ما يقلل عدم اليقين
5. تحديث الحالة

### 9. Evidence-Need Awareness — 5
1. كشف الادعاء
2. تحديد درجة الحاجة للإثبات
3. تحديد نوع الدليل المطلوب
4. ربط الادعاء بالفرضية
5. توليد Evidence/Research Request

### 10. Structural Analysis — 6
1. تفكيك المشكلة
2. تحديد المكونات
3. تحديد العلاقات
4. تحديد التبعيات
5. تحديد الأولويات الهيكلية
6. بناء Structural Model

### 11. Pattern Analysis — 5
1. كشف التكرار
2. كشف التشابه
3. كشف الاستثناءات
4. مقارنة الأنماط
5. بناء Pattern Model

### 12. Causal Candidate Analysis — 6
1. استخراج الأحداث
2. اقتراح أسباب محتملة
3. بناء سلاسل التأثير
4. فصل الارتباط عن السببية
5. مقارنة الأسباب
6. تصنيف السببية كمرشح

### 13. Consequence Analysis — 7
1. النتائج المباشرة
2. النتائج غير المباشرة
3. الآثار الجانبية
4. قابلية التراجع
5. النتائج الدائمة
6. تحديد الأطراف المتأثرة
7. بناء Consequence Model

### 14. Information Sufficiency Analysis — 5
1. تعريف متطلبات المهمة
2. جرد المعلومات المتاحة
3. مقارنة المتطلبات بالمتاح
4. تصنيف الحالة
5. توليد فجوات الكفاية

### 15. Hypothesis & Counter-Hypothesis Reasoning — 6
1. توليد الفرضية الأساسية
2. توليد الفرضية المضادة
3. توليد البدائل
4. مقارنة الفرضيات
5. تتبع الافتراضات
6. إنتاج Competing Hypotheses Model

### 16. Hypothesis Falsification — 5
1. تحديد شروط التفنيد
2. توليد اختبارات التفنيد
3. البحث عن نقاط الضعف
4. تسجيل ما يضعف الفرضية
5. تصنيف النتيجة

### 17. Counterfactual Reasoning — 5
1. تحديد المتغير المراد تغييره
2. بناء الحالة البديلة
3. إعادة حساب النتائج
4. مقارنة الواقع بالبديل
5. حفظ السيناريو كافتراضي

### 18. Assumption Detection — 5
1. استخراج الافتراضات الصريحة
2. استخراج الافتراضات الضمنية
3. تصنيف الافتراضات
4. تحدي الافتراض
5. ربط الافتراض بالنتيجة

### 19. Proof-Bound Cognition — 8
1. تصنيف OBSERVED
2. تصنيف FACT_FROM_AUTHORIZED_SOURCE
3. تصنيف INTERPRETATION
4. تصنيف INFERENCE
5. تصنيف HYPOTHESIS
6. تصنيف ASSUMPTION
7. تصنيف SIMULATION / FORECAST
8. تصنيف UNKNOWN / UNVERIFIED

### 20. Contradiction Detection & Handling — 10
1. اكتشاف التناقض
2. تصنيف التناقض
3. تحديد موقع التعارض
4. مقارنة الطرفين
5. تحدي الطرفين
6. توليد الحجة المضادة
7. تحديد الحاجة إلى دليل
8. اختبار التناقض
9. تحديد الحالة Accept/Reject/Defer
10. تسجيل Contradiction Record

### 21. Reasoning Chain Validation — 6
1. فحص المقدمات
2. فحص الافتراضات
3. فحص الروابط المنطقية
4. كشف المقدمات المفقودة
5. كشف القفزات غير المدعومة
6. تصنيف السلسلة

### 22. Cross-Reference Resolution — 5
1. اكتشاف المرجع
2. تحديد المرشحين
3. مطابقة المرجع بالسياق
4. كشف المرجع غير المحسوم
5. إنتاج Resolved Reference

### 23. Market Context Understanding — 6
1. تحديد الأصل
2. تحديد الإطار الزمني
3. تفسير حالة السوق
4. تفسير مصطلحات السوق
5. ربط عناصر السياق
6. تمييز مصدر السياق

### 24. Risk Language Detection — 6
1. كشف كلمات المخاطرة
2. كشف لغة Stop Loss
3. كشف لغة الرافعة
4. كشف لغة التعرض
5. تصنيف Risk Intent
6. تمييز الإشارة عن التفويض

### 25. Strategy Reference Resolution — 5
1. اكتشاف اسم الاستراتيجية
2. حل الاختصار
3. حل المرجع التاريخي
4. مطابقة المرجع بالكيان
5. إبقاء المرجع غير المحسوم

### 26. Position State Awareness — 6
1. تحديد وجود المركز
2. تحديد الأصل
3. تفسير اتجاه المركز
4. تفسير حالة المركز
5. ربط المركز بالسياق
6. تمييز المعرفة عن التحكم

### 27. Source Credibility Assessment — 5
1. تحليل خصائص المصدر
2. تحليل ملاءمة المصدر
3. كشف مؤشرات الضعف
4. إنتاج Credibility Assessment
5. فصل التقييم عن التحقق

### 28. Input Bias Detection — 6
1. كشف Framing
2. كشف Anchoring
3. كشف الانتقائية
4. كشف الافتراضات المنحازة
5. فصل الإشارة عن الحقيقة
6. إنتاج Bias Signals

## Phase 2 — Awareness (151 Internal Additions)

### 29. User-State Modeling — 7
1. استخراج إشارات الحالة
2. تصنيف الحالة المحتملة
3. تقدير عدم اليقين
4. دمج التاريخ
5. كشف تغير الحالة
6. تحديث النموذج
7. تمييز المرصود عن المستنتج

### 30. Emotional Signal Interpretation — 6
1. استخراج مؤشرات الانفعال
2. تصنيف الإشارة العاطفية
3. دمج عدة إشارات
4. قياس عدم اليقين
5. كشف تغير الإشارة
6. تمييز الإشارة عن الشعور الحقيقي

### 31. Tone Shift Detection — 5
1. بناء خط أساس للنبرة
2. تحليل النبرة الحالية
3. مقارنة النبرتين
4. تصنيف اتجاه التغير
5. تسجيل Tone Shift

### 32. Emoji & Symbol Context Interpretation — 5
1. التعرف على الرمز
2. استخراج المعاني المحتملة
3. ربط الرمز بالنص
4. ربطه بالنبرة
5. تحديد المعنى المرجح مع عدم اليقين

### 33. Implicit Meaning Detection — 6
1. استخراج المحتوى الصريح
2. اكتشاف الفجوة بين الصريح والمقصود
3. توليد الاستنتاجات الضمنية
4. توليد بدائل التفسير
5. اختبار التفسير بالسياق
6. تصنيف النتيجة كInference/Hypothesis

### 34. Hint Interpretation — 5
1. استخراج التلميح
2. ربطه بالسياق
3. توليد المعنى المحتمل
4. توليد البدائل
5. تصنيف الثقة المعرفية

### 35. Silence Interpretation — 5
1. اكتشاف فترة الصمت
2. مقارنة نمط التفاعل
3. توليد تفسيرات محتملة
4. تحديد Unknown عند غياب الدعم
5. تسجيل Silence Signal

### 36. Attention & Relevance Awareness — 5
1. تحديد العناصر المهمة
2. حساب الصلة
3. ترتيب الإشارات
4. كشف تغير الاهتمام
5. بناء Attention Model

### 37. Relationship Awareness — 5
1. اكتشاف الكيانات
2. تحديد علاقة الكيان بالكيان
3. ربط الأحداث بالكيانات
4. ربط الأهداف بالحالات
5. بناء Relationship Model

### 38. Continuity Awareness — 5
1. استرجاع السياق السابق
2. مطابقة الماضي بالحاضر
3. كشف الانقطاع
4. دمج السياق
5. إنتاج Continuity Model

### 39. Cognitive State Persistence — 6
1. استقبال الحالة السابقة
2. دمج المعلومات الجديدة
3. مقارنة القديم بالجديد
4. تحديد التغييرات
5. إصدار Version جديدة
6. حفظ Provenance

### 40. Cognitive Position Revision — 5
1. تحديد الموقف السابق
2. كشف المعلومات الجديدة المؤثرة
3. إعادة التقييم
4. تحديد سبب التغيير
5. إصدار Revised Position

### 41. Theory-of-Mind Modeling — 7
1. نمذجة ما قد يعرفه المستخدم
2. نمذجة ما قد يريده
3. نمذجة ما قد يتوقعه
4. نمذجة الافتراضات المحتملة
5. توليد بدائل للحالة الذهنية
6. تتبع عدم اليقين
7. تحديث Mind Model

### 42. Communication-Style Awareness — 5
1. اكتشاف نمط اللغة
2. اكتشاف مستوى التفصيل
3. اكتشاف التفضيلات التواصلية
4. كشف تغير الأسلوب
5. بناء Communication Style Model

### 43. User-Context Modeling — 5
1. دمج User State
2. دمج Intent
3. دمج Goal
4. دمج Communication Style
5. إنتاج User-Context Model

### 44. Empathy Modeling — 5
1. تحديد الحالة المحتملة
2. توليد أساليب رد
3. محاكاة أثر الرد
4. مقارنة أساليب الرد
5. إنتاج Communication Adaptation Candidate

### 45. Multi-Perspective Analysis — 5
1. توليد المنظورات
2. تحليل كل منظور
3. مقارنة المنظورات
4. كشف تعارض المنظورات
5. إنتاج Perspective Set

### 46. Knowledge Synthesis — 6
1. تجميع عناصر المعرفة
2. توحيد المفاهيم المتشابهة
3. ربط العلاقات
4. حفظ Provenance
5. حفظ الاختلافات
6. إنتاج Synthesized Knowledge Model

### 47. Mental Model Construction — 6
1. استخراج المفاهيم
2. استخراج العلاقات
3. تحديد التبعيات
4. تكوين النموذج
5. اختبار النموذج
6. تحديث النموذج

### 48. Sarcasm Detection — 5
1. تحليل المعنى الحرفي
2. تحليل السياق
3. تحليل النبرة
4. توليد Sarcasm Candidate
5. تسجيل عدم اليقين

### 49. Irony Detection — 4
1. مقارنة الحرفي بالسياقي
2. تحليل السياق السابق
3. توليد Irony Candidate
4. الحفاظ على البدائل

### 50. Humor Recognition — 4
1. كشف إشارات المزاح
2. تحليل السياق
3. تصنيف Humor Signal
4. الحفاظ على عدم اليقين

### 51. Cultural Context Interpretation — 5
1. تحديد التعبير الثقافي
2. استرجاع المعنى السياقي
3. تمييز العرف عن الفرد
4. توليد البدائل الثقافية
5. إنتاج Cultural Interpretation

### 52. Multi-Language Switching — 5
1. كشف اللغة الحالية
2. تتبع لغة المحادثة
3. حفظ المعنى أثناء التحول
4. دمج المصطلحات المشتركة
5. إنتاج Unified Meaning Model

### 53. Code-Switching Detection — 5
1. تجزئة النص حسب اللغة
2. كشف اللهجة
3. كشف المصطلحات التقنية
4. ربط المقاطع معنويًا
5. إنتاج Code-Switch Map

### 54. Pause Detection — 4
1. قياس مدة التوقف
2. مقارنة التوقف بالنمط المعتاد
3. تصنيف Pause Signal
4. ربط التوقف بالسياق

### 55. Hesitation Detection — 5
1. كشف التكرار اللغوي
2. كشف التصحيح المتكرر
3. تحليل التوقيت المتاح
4. تجميع إشارات التردد
5. إنتاج Hesitation Signal

### 56. Confirmation Signal Detection — 5
1. كشف عبارات التأكيد
2. تحليل التأكيد ضمن السياق
3. تمييز التأكيد الجزئي
4. تمييز التأكيد عن التفويض
5. إنتاج Confirmation Candidate

### 57. Correction Detection — 5
1. مقارنة البيان الجديد بالقديم
2. تحديد العنصر المصحح
3. تحديد نوع التصحيح
4. تحديث الحالة المتأثرة
5. تسجيل Correction Event

### 58. Meta-Communication Understanding — 5
1. كشف تعليمات التواصل
2. فصل التعليمات عن المحتوى
3. كشف تعليق المستخدم على الفهم
4. تحديث نموذج التواصل
5. حماية السلطة

## Phase 3 — Deep (83 Internal Additions)

### 59. Recursive Deep Thinking — 5
1. إعادة التحليل
2. توسيع سلسلة التفكير
3. فحص النتائج الوسيطة
4. تحديد نقطة التوقف
5. إنتاج Deeper Reasoning State

### 60. Second-Order Thinking — 5
1. استخراج النتيجة الأولى
2. اشتقاق أثر النتيجة
3. توسيع سلسلة النتائج
4. كشف التأثيرات غير المباشرة
5. بناء Higher-Order Model

### 61. Internal Critique — 6
1. اختبار المقدمات
2. اختبار الافتراضات
3. اختبار الروابط
4. توليد الحجة المضادة
5. البحث عن فشل الاستنتاج
6. إنتاج Critique Record

### 62. Blind-Spot Detection — 5
1. البحث عن المعلومات المفقودة
2. البحث عن منظور مفقود
3. البحث عن افتراض مفقود
4. اختبار ما تم تجاهله
5. تسجيل Blind-Spot Candidates

### 63. Cognitive Bias Guarding — 7
1. كشف Anchoring
2. كشف Confirmation Bias
3. كشف Overconfidence
4. كشف Availability Bias
5. كشف Recency Bias
6. مقارنة الفرضية المضادة
7. إنتاج Bias Risk State

### 64. Scenario Branching & Mental Simulation — 7
1. تعريف الحالة الأساسية
2. تحديد المتغيرات
3. توليد الفروع
4. محاكاة كل فرع
5. مقارنة الفروع
6. كشف نقاط الفشل
7. تمييز المحاكاة عن الواقع

### 65. Failure & Recovery Simulation — 6
1. تعريف حالة الفشل
2. توليد مسار الفشل
3. تحليل الأثر
4. توليد مسار التعافي
5. مقارنة مسارات التعافي
6. بناء Failure/Recovery Model

### 66. Forecasting & Prediction Awareness — 6
1. تحديد الحالة الحالية
2. استخراج العوامل المؤثرة
3. توليد Forecast
4. تحديد شروط التوقع
5. تمثيل عدم اليقين
6. تمييز Forecast عن Fact

### 67. Meta-Cognition — 7
1. مراقبة عملية التفكير
2. تقييم جودة التفكير
3. مراقبة عدم اليقين
4. اكتشاف الخطأ الذاتي
5. اقتراح التصحيح
6. إعادة التقييم بعد التصحيح
7. تسجيل Meta-Cognitive State

### 68. Awareness Quality & Depth Assessment — 5
1. تقييم اكتمال الوعي
2. تقييم عمق الوعي
3. تقييم الاتساق
4. تحديد نقاط الضعف
5. إنتاج Awareness Assessment

### 69. Ignorance & Knowledge-Boundary Awareness — 6
1. تحديد ما هو مجهول
2. تحديد ما لا يمكن استنتاجه حاليًا
3. تمييز نقص البيانات عن تناقضها
4. تحديد حدود القدرة الحالية
5. تحديد ما يحتاج جهة خارجية
6. إنتاج Knowledge Boundary Model

### 70. Cognitive Provenance & Audit Continuity — 6
1. تسجيل أصل التغيير
2. تسجيل النسخة السابقة
3. تسجيل النسخة الجديدة
4. تسجيل سبب Revision
5. تتبع Provenance عبر الزمن
6. إنتاج Cognitive Evolution Record

### 71. Concept Mapping — 6
1. استخراج المفاهيم
2. تصنيف المفاهيم
3. ربط المفاهيم
4. ربط التبعيات
5. تجريد الخريطة
6. إنتاج Concept Map

### 72. Contradiction Synthesis — 6
1. تجميع التناقضات المرتبطة
2. تصنيف مجموعات التعارض
3. مقارنة التناقضات
4. محاولة التوفيق
5. حفظ التعارض غير المحسوم
6. إنتاج Contradiction Synthesis Model

## Accounting

- Phase 1 — Trading: 163 internal additions
- Phase 2 — Awareness: 151 internal additions
- Phase 3 — Deep: 83 internal additions
- Total: 397 internal additions
- Capabilities: 72

## Implementation Boundary

This breakdown does not add authority to Stage 2. Stage 2 remains bounded by the Stage 2 Cognitive Intelligence design:
- Evidence verification → Stage 11
- External research authority → Stage 12
- Final decision authority → Stage 13
- Authorization and execution → Stage 14
- Signals and market intelligence processing → Stage 15
- Bot construction/coordination/consumption → Stage 16

Internal additions remain subject to consolidation during implementation. True duplicates may be merged or removed only through the project's normal owner-approved process.
