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
B13 Memory Foundation — مكتمل Demo + durable repository  
B14 Knowledge Foundation — مكتمل Demo + durable repository

## B15 — Analytics — مكتمل
- `AmarAnalytics` deterministic, bounded, read-only runtime analytics.
- success/failure rate، latency، decision distribution، risk gate، execution mode، consecutive failures.
- عينات bounded لمنع النمو غير المحدود.

## B16 — Explainability — مكتمل
- immutable decision trace يربط السياق والقرار والثقة والتفسير والمخاطر وحد التنفيذ.
- لا يغير القرار ولا ينفذ التداول.

## B17 — Guardian — مكتمل
- fail-closed policy مستقلة.
- faulted runtime، failures، risk gate، execution boundary.
- لا تستطيع تمكين LIVE أو تنفيذ الأوامر.

## B18 — Security Runtime — مكتمل
- DEMO/READ_ONLY/LIVE موحدة مع `core.AmarOperatingMode`.
- emergency lock وauthorization وcredential access مغلقة افتراضيًا.
- لا يوجد مسار LIVE فعلي في هذه المرحلة.

## B19 — Audit — مكتمل
- structured bounded audit log.
- secret/token/password/credential redaction.
- SHA-256 chained integrity verification.

## B20 — Supervisor — مكتمل
- read-only supervisory snapshot يجمع health + Guardian + Security + Explainability + Analytics.
- لا ينفذ التداول ولا يغير القرارات.

## إضافات معتمدة ومُنفذة في هذه المرحلة
### AI Agent
- `AmarAgentContract` provider/tool boundary مستقل.
- لا يملك تنفيذ تداول مباشر.

### AMAR Trading Library
- catalog للبوتات والمؤشرات والاستراتيجيات والحزم.
- in-memory repository + durable Android SharedPreferences repository.

### Runtime hardening
- `AmarClock` للاختبارات والمحاكاة deterministic.
- event `correlationId` + monotonic `sequence`.
- repository boundaries للـMemory/Knowledge/Library.
- contract/event/unit tests وتشغيلها داخل CI قبل بناء APK.
- bounded idempotency store.
- operation timeouts + exponential backoff + jitter + coroutine-cancellation safety.
- circuit breaker primitive.
- centralized `AmarRuntimeConfig` لهذه السياسات.
- structured audit integrity + secret redaction.

## التحقق
- CI يجب أن يمر عبر `testDebugUnitTest` ثم `assembleDebug` ثم artifact upload.
- لا يعتبر B1-B20 مثبتًا نهائيًا بعد أي تغيير إلا بعد نجاح الاختبارات والبناء.

## المسار الرسمي
`Market Data → B5 → B6 → B7 → B8 → B9 → B10 → B11 → B12 → B13 → B14 → B15 Analytics → B16 Explainability → B17 Guardian → B18 Security Runtime → B19 Audit → B20 Supervisor → UI/Services`

## حالة الإكمال
**B1 إلى B20: مكتملة كطبقة تأسيس وتشغيل Demo، مع الاختبارات والعقود والتخزين الدائم والأساسات الأمنية/الرصدية اللازمة للتوسع.**

هذا لا يعني اكتمال MT5/Broker Adapter أو تفعيل التداول الحقيقي. `liveTrading` يبقى `false`، ولا يتم تنفيذ Broker/MT5 من هذه الطبقة.
