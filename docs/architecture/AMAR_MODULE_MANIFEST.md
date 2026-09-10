# AMAR Module Manifest

## Purpose
المرجع الموحد لتسجيل وحدات AMAR ومسؤولياتها واعتمادياتها وحالتها وإصدارها.

## قواعد المشروع
- أضف ولا تهدم.
- لا حذف أو إعادة هيكلة للوحدات القديمة دون اعتماد صريح.
- فصل واجهة المستخدم عن منطق التداول.
- لا تداول حقيقي في المرحلة الحالية.
- لا تتم إضافة أي بوت جديد ضمن هذه المرحلة.
- واجهة المستخدم تعرض الحالة ولا تتخذ قرار التداول.

## الوحدات المعتمدة في مرحلة التأسيس

| الوحدة | المسؤولية | الاعتماديات | الحالة | الإصدار |
|---|---|---|---|---|
| AMAR App Shell | الغلاف العام ومساحة الغرف | Compose | قيد التأسيس | 1.0-alpha |
| Room Navigation | اختيار الغرفة والتنقل الداخلي | App State | قيد التأسيس | 1.0-alpha |
| Aurora Design System | الهوية البصرية Aurora/Glass/Neon | Material 3 | قيد التأسيس | 1.0-alpha |
| Motion Engine | مستويات الحركة OFF إلى CINEMATIC | Compose Animation | قيد التأسيس | 1.0-alpha |
| Centralized State | حالة التطبيق العامة | Kotlin | قيد التأسيس | 1.0-alpha |
| Event Bus | فصل الوحدات عبر الأحداث | Kotlin Flow | قيد التأسيس | 1.0-alpha |
| Feature Flags | تفعيل وتعطيل الوحدات | Kotlin | قيد التأسيس | 1.0-alpha |
| Operating Modes | SIMULATION/DEMO/READ_ONLY/LIVE/EMERGENCY | Kotlin | قيد التأسيس | 1.0-alpha |
| Legacy Grid UI | الواجهة الحالية المحفوظة | Compose | موجودة ومحفوظة | 1.x |
| B5 Data Fabric | توحيد السوق والحساب والمراكز والأوامر والمخاطر ومصدر DEMO | Kotlin | منفذ | 1.0 |
| B6 Intelligence Foundation | الأدلة ودمجها والتحليل وبوابة التحقق الآمنة | B5 Data Fabric | منفذ | 1.0 |

## B5 — Data Fabric
- `MarketSnapshot` يوحد bid/ask/spread/timeframe/timestamp.
- `AccountSnapshot` يوحد balance/equity/margin/free-margin/drawdown.
- `PositionSnapshot` و`OrderSnapshot` يوحدان حالة التداول للعرض والتحليل.
- `BotRuntimeSnapshot` و`RiskSnapshot` يفصلان حالة AMAR GRID عن العرض.
- `AmarDataSnapshot` هو العقد المجمع.
- `DemoDataProvider` مصدر متدرج deterministic للاختبار، ولا ينفذ تداولًا.

## B6 — Intelligence Foundation
- `Evidence` يمثل أدلة الاتجاه والزخم والتقلب والبنية والحجم والجلسة والنظام.
- `EvidenceFusion` يدمج الأدلة ضمن نطاقات آمنة ويولد `MarketContext`.
- `MarketAnalyzer` ينتج تحليلًا فقط ولا يصدر تنفيذًا.
- `DecisionValidation` بوابة سلامة تمنع التنفيذ في DEMO.
- `DemoIntelligencePipeline` يربط B5 → B6 دون broker calls أو trade execution.

## حدود المرحلة
هذه المرحلة لا تضيف بوتات أو استراتيجيات تداول جديدة، ولا تفعل LIVE trading. الواجهة الحالية هي Baseline UI v1 ويمكن إعادة تصميمها مستقبلًا دون كسر B5/B6 أو طبقات التداول.
