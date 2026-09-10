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

## B9 — Intelligence Runtime Integration — مكتمل Demo
- `AmarRuntimeController` يملك دورة B1-B8 ويعرض أحدث Telemetry/Context/Decision/Risk/Execution كـ `StateFlow`.
- مصدر التشغيل الافتراضي `DemoDataProvider` فقط.
- لا يملك B9 صلاحية تنفيذ تداول حقيقي.
- أي واجهة مستقبلية تستطيع استهلاك الحالة دون معرفة تفاصيل B5-B8.

## B10 — Demo Event Runtime — مكتمل Demo
- `AmarRuntimeTicker` يشغل دورة B9 بفاصل مضبوط وقابل للتغيير.
- كل دورة تنشر أحداثًا قابلة للرصد عبر `AmarEventBus`.
- الإيقاف والإعادة آمنان عبر `Job` واحد فقط.
- الحد الأدنى للفاصل 250ms لمنع حلقات التشغيل غير المنضبطة.
- لا يوجد broker/network execution.

## B11 — Event / State Expansion — مكتمل Demo
- `AmarEventBus` typed ويغطي بدء الدورة واكتمالها وفشلها وتغير صحة runtime، مع الإبقاء على أحداث الغرف والنظام والإيقاف الطارئ.
- `RuntimeState` يحمل health إلى جانب Telemetry/Context/Decision/Risk/Execution.
- أحداث runtime تحمل بيانات تشغيلية قابلة للتدقيق مثل cycle number، duration، decision، confidence، risk gate، وexecution mode.
- لا يمنح B11 أي صلاحية تنفيذ؛ الأحداث مراقبة فقط.

## B12 — Runtime Monitoring — مكتمل Demo
- `AmarRuntimeHealth` عقد موحد لصحة التشغيل.
- `AmarRuntimeMonitor` يراقب liveness، عدد الدورات، الإخفاقات المتتالية والإجمالية، آخر نجاح، زمن الدورة، والخطأ الأخير.
- حالات الصحة: `STARTING`, `HEALTHY`, `DEGRADED`, `FAULTED`, `STOPPED`.
- runtime لا يتوقف بالكامل بسبب استثناء دورة واحدة؛ يتم احتواء الخطأ وتحويله إلى حالة مراقبة قابلة للرصد.
- الواجهة تعرض صحة runtime فقط ولا تتخذ قرارات تداول.

## B13 — Memory Foundation — مكتمل Demo
- `AmarMemoryRecord` عقد ذاكرة immutable وnamespaced.
- `AmarMemoryRepository` حد التخزين القابل للاستبدال.
- `InMemoryAmarMemoryRepository` تنفيذ deterministic آمن للاختبار وDemo.
- `AmarMemoryService` طبقة وصول تتحقق من البيانات وتطبع tags/importance.
- لا يتم ربط B13 مباشرة بواجهة المستخدم أو التنفيذ التداولي.

## B14 — Knowledge Foundation — مكتمل Demo
- `AmarKnowledgeItem` عقد معرفة immutable مع source/version/confidence/tags.
- `AmarKnowledgeRepository` حد التخزين والبحث القابل للاستبدال.
- `InMemoryAmarKnowledgeRepository` بحث deterministic بسيط للـ Demo والاختبار.
- `AmarKnowledgeService` طبقة نشر/استرجاع موحدة.
- المعرفة منفصلة عن ذاكرة runtime وعن قرار التداول.

## المسار الرسمي
`Market Data → B5 Data Fabric → B6 Intelligence → Evidence Fusion → B7 Decision Engine → Decision Validation → B8 Risk/Execution Boundary → B9 Runtime → B10 Event Runtime → B11 Event/State → B12 Monitoring → B13 Memory → B14 Knowledge → UI/Services`

## حالة الإكمال
**B1 إلى B14: مكتمل كطبقة تأسيس وتشغيل Demo قابلة للبناء والتوسع.**

المقصود بـ "مكتمل" هو اكتمال العقود والتدفق والتشغيل التجريبي الآمن. لا يعني تفعيل التداول الحقيقي أو اكتمال MT5/Broker Adapter؛ ذلك مرحلة لاحقة منفصلة.

## حدود المرحلة
لا LIVE trading، لا broker execution، لا استراتيجيات/بوتات جديدة، ولا قرارات تداول داخل UI. يمكن تطوير التخزين الدائم، الفهرسة، والاسترجاع المتقدم لاحقًا خلف حدود B13/B14 دون كسر المستهلكين.
