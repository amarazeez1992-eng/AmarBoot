# AMAR Module Manifest

## Purpose
المرجع الموحد لتسجيل وحدات AMAR ومسؤولياتها واعتمادياتها وحالتها وإصدارها.

## قواعد المشروع
- أضف ولا تهدم.
- لا حذف أو إعادة هيكلة للوحدات القديمة دون اعتماد صريح.
- فصل واجهة المستخدم عن منطق التداول.
- المرحلة الحالية Demo-only.
- لا تتم إضافة أي بوت جديد ضمن هذه المرحلة؛ AMAR GRID هو البوت المعتمد الحالي.
- واجهة المستخدم تعرض الحالة ولا تتخذ قرار التداول.

## الوحدات الأساسية B1-B4
| الوحدة | المسؤولية | الحالة |
|---|---|---|
| B1 App Shell | الغلاف العام ومساحة التطبيق | مكتمل |
| B2 Design System | الهوية البصرية والـ tokens والأسطح | مكتمل |
| B3 Room Navigation | الغرف والتنقل والعقود | مكتمل |
| B4 Living UI/Motion | الحركة والحالة الحية ومحرك العرض | مكتمل |

## B5 — Data Fabric — مكتمل
- `MarketSnapshot` يوحد bid/ask/spread/timeframe/timestamp.
- `AccountSnapshot` يوحد balance/equity/margin/free-margin/drawdown.
- `PositionSnapshot` و`OrderSnapshot` يوحدان حالة التداول للعرض والتحليل.
- `BotRuntimeSnapshot` و`RiskSnapshot` يفصلان حالة AMAR GRID عن العرض.
- `AmarDataSnapshot` هو العقد المجمع.
- `DemoDataProvider` مصدر deterministic متدرج ولا ينفذ تداولًا.

## B6 — Intelligence Foundation — مكتمل
- `Evidence` يمثل الأدلة.
- `EvidenceFusion` يدمج الأدلة ويولد `MarketContext`.
- `MarketAnalyzer` ينتج تحليلًا فقط.
- `DecisionValidation` يمنع التنفيذ في DEMO.
- `DemoIntelligencePipeline` يربط B5 → B6 دون broker calls.

## B7 — Decision Engine — مكتمل
- `DecisionEngine` يحول `MarketContext` إلى `DecisionProposal` منظم.
- القرار يتضمن الاتجاه والدرجة والثقة والتفسير.
- القرار غير قابل للتنفيذ ذاتيًا (`executable=false`).
- `DecisionPipeline` يربط البيانات → التحليل → التحقق → القرار.

## B8 — Safety / Execution Boundary — مكتمل للمرحلة Demo
- `AmarRiskGate` بوابة مستقلة للمخاطر والإيقاف الطارئ والسحب والقرار المحايد.
- `AmarExecutionBoundary` يمنع التنفيذ الحقيقي في DEMO ويفصل القرار عن Broker Adapter.
- لكل طلب `requestId` لمنع الغموض وتحسين التتبع.
- `AmarFoundationCycle` يربط B5 → B6 → B7 → B8 في دورة واحدة قابلة للاستهلاك من الواجهة والخدمات.
- لا يوجد Broker Adapter أو MT5 execution في هذه المرحلة.

## المسار الرسمي
`Market Data → B5 Data Fabric → B6 Intelligence → Evidence Fusion → B7 Decision Engine → Decision Validation → B8 Risk/Execution Boundary → UI/Services`

## حالة الإكمال
**B1 إلى B8: مكتمل كطبقة تأسيس Demo قابلة للبناء والتوسع.**

المقصود بـ "مكتمل" هنا هو اكتمال العقود، الفصل المعماري، تدفق البيانات، التحليل، القرار، بوابات السلامة، والتكامل البرمجي. لا يعني ذلك تفعيل التداول الحقيقي أو اكتمال MT5/Broker Adapter؛ ذلك مرحلة لاحقة منفصلة.

## حدود المرحلة
لا LIVE trading، لا broker execution، لا استراتيجيات/بوتات جديدة، ولا قرارات تداول داخل UI. يمكن إعادة تصميم الواجهة مستقبلًا دون كسر B5-B8.
