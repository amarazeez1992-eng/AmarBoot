# AMAR Module Manifest

## Purpose
المرجع الموحد لتسجيل وحدات AMAR ومسؤولياتها واعتمادياتها وحالتها وإصدارها.

## قواعد المشروع
- أضف ولا تهدم.
- لا حذف أو إعادة هيكلة للوحدات القديمة دون اعتماد صريح.
- فصل واجهة المستخدم عن منطق التداول.
- المرحلة الحالية Demo-only.
- لا تتم إضافة أي بوت جديد ضمن هذه المرحلة؛ AMAR GRID هو البوت المعتمد الحالي.
- واجهة المستخدم تعرض الحالة ولا تتخذ قرارات تداول.
- كل وحدات AI/Guardian/Security/Supervisor الحالية مراقبة وتحليلية ولا تمنح صلاحية تداول حقيقي.

## B1-B14
B1 App Shell — مكتمل  
B2 Design System — مكتمل  
B3 Room Navigation — مكتمل  
B4 Living UI/Motion — مكتمل  
B5 Data Fabric — مكتمل  
B6 Intelligence Foundation — مكتمل  
B7 Decision Engine — مكتمل  
B8 Safety / Execution Boundary — مكتمل Demo  
B9 Intelligence Runtime Integration — مكتمل Demo  
B10 Demo Event Runtime — مكتمل Demo  
B11 Event / State Expansion — مكتمل Demo  
B12 Runtime Monitoring — مكتمل Demo  
B13 Memory Foundation — مكتمل Demo  
B14 Knowledge Foundation — مكتمل Demo

## B15 — Analytics — مكتمل التنفيذ الأولي
- `AmarAnalytics` تحليلات deterministic وread-only على دورات runtime.
- قياس success/failure rate، latency، decision distribution، risk gate، execution mode، وconsecutive failures.
- نافذة عينات bounded لمنع نمو الذاكرة بلا حدود.

## B16 — Explainability — مكتمل التنفيذ الأولي
- `AmarDecisionExplanation` سجل immutable يربط السياق بالقرار والثقة والتفسير وبوابة المخاطر وحد التنفيذ.
- `AmarExplainabilityService` يولد trace قابلًا للقراءة دون تغيير القرار أو التنفيذ.

## B17 — Guardian — مكتمل التنفيذ الأولي
- `AmarGuardian` سياسة fail-closed مستقلة.
- تراقب faulted runtime، failures، risk gate، وexecution boundary.
- لا تستطيع تمكين LIVE أو تنفيذ الأوامر.

## B18 — Security Runtime — مكتمل التنفيذ الأولي
- `AmarSecurityRuntime` يفصل DEMO وREAD_ONLY عن LIVE.
- emergency lock، live activation requirement، execution authorization، وcredential access كلها مغلقة افتراضيًا.
- لا يوجد مسار LIVE فعلي في هذه المرحلة.

## B19 — Audit — مكتمل التنفيذ الأولي
- `AmarAuditRecord` structured immutable-ish event record.
- `AmarAuditLog` bounded thread-safe log.
- تصفية مفاتيح secrets/passwords/tokens قبل التخزين.

## B20 — Supervisor — مكتمل التنفيذ الأولي
- `AmarSupervisor` طبقة مراقبة وتوصية read-only.
- يجمع health + Guardian + Security + Explainability + Analytics.
- التوصيات: STOP_AND_REVIEW_SECURITY / HOLD_AND_REVIEW_GUARDIAN / STOP_AND_REVIEW_RUNTIME / OBSERVE.
- لا ينفذ أي تداول ولا يغير قرارات النظام.

## إضافات معتمدة مستقبلًا
### AI Agent
- `AmarAgentContract` يحدد provider/tool boundary مستقل.
- الـ Agent مستقبلي وقابل لتعدد المزودين، ولا يملك تنفيذ تداول مباشر.

### AMAR Trading Library
- `AmarTradingLibrary` عقد مستقل لفهرسة البوتات والمؤشرات والاستراتيجيات والحزم.
- التنفيذ الحالي in-memory فقط؛ التخزين الدائم والفهرسة المتقدمة لاحقًا خلف repository boundary.

### اقتراحات معمارية معتمدة
- Clock abstraction للاختبارات والمحاكاة deterministic.
- Event correlation IDs + monotonic sequence numbers كاتجاه observability/audit.
- Repository boundaries للتخزين الدائم والاسترجاع.
- Contract/event testing وidempotency.
- Timeouts + backoff/jitter + circuit breaker للموصلات الخارجية المستقبلية.
- Structured observability وhealth heartbeat.
- Centralized runtime configuration.
- تشديد DEMO/LIVE isolation، encrypted persistence، key management، schema migrations، وaudit integrity مستقبلًا.

## المسار الرسمي
`Market Data → B5 → B6 → B7 → B8 → B9 → B10 → B11 → B12 → B13 → B14 → B15 Analytics → B16 Explainability → B17 Guardian → B18 Security Runtime → B19 Audit → B20 Supervisor → UI/Services`

## حالة الإكمال
**B1 إلى B20: مكتمل كطبقة تأسيس وتشغيل Demo قابلة للبناء والتوسع، مع حدود واضحة للمراحل المستقبلية.**

لا يعني ذلك اكتمال MT5/Broker Adapter أو تفعيل التداول الحقيقي. `liveTrading` يبقى `false`، ولا يتم تنفيذ Broker/MT5 من هذه الطبقة.
