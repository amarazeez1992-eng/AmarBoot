# AMAR AI — MASTER HANDOVER DOCUMENT

## Last Updated: 2026-09-22

---

## 1. هوية المشروع

- **Repository:** amarazeez1992-eng/AmarBoot
- **Current main:** 7509eaf7a21f54a8752b356d7d4447d8ecb1de44
- **Stage:** 11 — AMAR INTELLIGENCE 99
- **Item:** 3 — Evidence Engine
- **Point:** 12 CLOSED / 13 NEXT

---

## 2. الحالة الرسمية

### Stage 11 — 30 Item

| # | Item | الحالة |
|---|------|--------|
| 1 | Intelligence Core | ✅ |
| 2 | Confidence Engine | ✅ |
| 3 | Evidence Engine | 🟡 (الحالي) |
| 4-30 | ... | ⏸ |

### Item 3 — 24 Point

| # | Point | الحالة |
|---|-------|--------|
| 1 | Evidence Intake | ✅ CLOSED |
| 2 | Source Quality | ✅ CLOSED |
| 3 | Authority | ✅ CLOSED |
| 4 | Freshness | ✅ CLOSED |
| 5 | Independence | ✅ CLOSED |
| 6 | Duplicate | ✅ CLOSED |
| 7 | Fingerprint | ✅ CLOSED |
| 8 | Tampering | ✅ CLOSED |
| 9 | Uniqueness | ✅ CLOSED |
| 10 | Quality Score | ✅ CLOSED |
| 11 | Status Classification | ✅ CLOSED |
| 12 | Evidence Ranking | ✅ CLOSED |
| 13 | Evidence Explanation | ⏭️ التالي |
| 14-24 | ... | ⏸ |

### Article 15 — Steps 1-4

| Step | الحالة |
|------|--------|
| Step 1 — Rules | ✅ CLOSED |
| Step 2 — Evidence Intake | ✅ CLOSED |
| Step 3 — Question Relevance | ✅ CLOSED |
| Step 4 — Evidence Selection | ✅ CLOSED |

---

## 3. القواعد غير القابلة للتفاوض

1. لا إغلاق بدون Evidence.
2. لا تحويل NOT TRIGGERED إلى SUCCESS.
3. لا اعتبار Mergeable = Ready.
4. لا اعتبار Design = Implementation.
5. لا اعتبار CI تاريخي = إثبات للحالة الحالية.
6. كل إصلاح يعالج Root Cause.
7. لا Authority موازية.
8. لا Migration بدون تصميم.
9. لا Hardcoded Answers.
10. لا تخمين.
11. الدليل الحالي هو مصدر الحقيقة.
12. معيار الدقة: 100%.

---

## 4. البروتوكول السبعي (لكل نقطة)

1. تدقيق العمل الحالي (Audit).
2. تقييم الفائدة (Value Assessment).
3. تحديد الفجوات (Gap Analysis).
4. اقتراح التحسين (Improvement Proposal).
5. الاعتماد (Approval).
6. التنفيذ (Implementation).
7. الإغلاق الدستوري (Constitutional Closure).

---

## 5. دورة التحقق الكاملة

```
Root Cause → Minimal Fix → Compile → Unit Tests
→ Integration Tests → Build → Static → Runtime
→ Regression → Audit → Closure
```

---

## 6. دورة CI

### Pre-Merge:
- Stage One
- Stage Two
- Stage 11
- Stage 10
- CodeQL

### Post-Merge:
- Stage 8
- Stage 10
- Stage 11
- CodeQL
- Build APK
- Final APK Closure

### القاعدة:
- `NOT TRIGGERED — PATH-GATED` ≠ `SUCCESS`.
- `docs-only` PRs → Path-Gated.

---

## 7. الملفات المحمية

- `RelevantCandidate.kt`
- `AmarEvidenceFreshnessAnalyzer.kt`
- `ResearchFinding.kt`
- `AmarEvidenceQualityUpstreamState.kt`
- `AmarEvidenceQualityScoreEngine.kt`
- `MIN_RELEVANCE_SCORE`
- `AmarLocalReasoning.kt`
- `AmarSourceVerifier.kt`

---

## 8. صلاحيات المشرف المطلقة

**سارية حتى Stage 100:**

- إضافة إضافات استراتيجية تلقائياً.
- توسيع نطاق Points/Steps.
- اقتراح تحسينات معمارية.
- رفض التبسيط.
- تعديل الدستور (إذا زاد الكفاءة).
- معيار: دقة 100%.

**الحدود:**
- لا Authority موازية.
- لا تعديل Points مغلقة.
- الإضافات قبل القفل، لا بعده.

---

## 9. الميزات المعتمدة (خارطة الطريق)

### 9.1 Agent Settings / Control Panel

**الموقع:** Stages 31-40.

**الميزات:**
- لوحة تحكم كاملة.
- إعدادات المخاطرة (قابلة للتغيير).
- إعدادات البحث.
- إعدادات التحليل.
- إعدادات التنفيذ.
- Presets: Conservative / Balanced / Aggressive / Custom.
- إضافة/حذف إعدادات.

**المبدأ:** Article 17.

---

### 9.2 Discussion Before Execution

**الموقع:** Stages 40-50.

**الميزات:**
- Agent يناقش قبل تنفيذ أي أمر كبير.
- يعرض المخاطر.
- يقترح بدائل.
- يطلب موافقة صريحة.

**مثال:**
```
User: "فعل بوت نظام التتبع الشبكي"

Agent:

· تحليل السوق.
· عرض نقاط القوة والضعف.
· اقتراح بدائل.
· طلب موافقة.

User: "نعم" → تنفيذ.
```

**المبدأ:** Article 18.

---

### 9.3 Multimodal Support

**الموقع:** Stage 11 → Items 22-25.

**Item 22 — Multimodal Engine:**
- تكلم صوتي (صوت بصوت).
- Speech-to-Text.
- Text-to-Speech.
- Real-time streaming.
- Multi-language (عربي + إنجليزي).
- تحليل الصور.
- تحليل المقاطع.

**Item 23 — Screen Understanding:**
- مشاركة الشاشة.
- تحليل المحتوى.
- سؤال Agent.

**Item 24 — Unified Multimodal Workspace:**
- مشاركة الكاميرا.
- التقاط صور.
- تحليل فوري.
- دمج الصوت + الصورة + النص.

**Item 25 — File & APK Analysis:**
- APK (فحص أمني).
- TXT, PDF, DOCX, MD.
- CSV, JSON, XML, Excel.
- Kotlin, Java, Python, JS, SQL.
- ZIP, TAR.
- Images, Videos.

**المبدأ:** Articles 19-20.

---

### 9.4 Full Trade Cycle (Stage 100)

**الموقع:** Stages 50-100 (بعد MT5 Integration).

**التسلسل:**
1. بحث عن صفقة.
2. توصية.
3. فتح الصفقة.
4. مراقبة.
5. إغلاق (TP/SL).
6. إبلاغ.

**الشروط:**
- Risk Settings (User-Controlled).
- Discussion Before Execution.
- MT5 Integration.

---

## 10. Articles الدستورية المطلوبة

| Article | الموضوع |
|---------|---------|
| 17 | User Configurable Agent |
| 18 | Discussion Before Execution |
| 19 | Multimodal Support |
| 20 | File Analysis Framework |
| 21 | Full Trade Cycle (يُضاف لاحقاً) |

---

## 11. المراحل الحرجة

| Stage | التركيز |
|-------|---------|
| 11 | Evidence Engine (الآن) |
| 12-20 | Research + Analysis |
| 21-30 | Recommendation + Decision |
| 31-40 | Agent Settings |
| 41-50 | Risk + Execution + Discussion |
| 51-60 | MT5 Integration |
| 61-70 | Multimodal (Items 22-25) |
| 71-99 | Refinement |
| 100 | Full Agent |

---

## 12. كيف تستأنف العمل

1. اقرأ `AMAR-HANDOVER.md`.
2. افحص `main` الحالي.
3. تحقق من `docs/verification/`.
4. تحقق من آخر `POINTx-CLOSURE.md`.
5. استأنف من النقطة التالية.

---

## 13. المرجعيات

- **الدستور:** `docs/governance/AMAR-AI-MASTER-CONSTITUTION.md`
- **التحقق:** `docs/verification/`
- **الهندسة:** `docs/engineering/`
- **الأخطاء:** `docs/engineering/corrections/`

---

## 14. القاعدة الذهبية

> **لا تقل "تم" لأن الخطة تقول تم.**
> **الدليل الحالي هو مصدر الحقيقة.**

---

## END OF HANDOVER
