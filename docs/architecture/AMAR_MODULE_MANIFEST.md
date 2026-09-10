# AMAR Module Manifest

## Purpose
المرجع الموحد لتسجيل وحدات AMAR ومسؤولياتها واعتمادياتها وحالتها وإصدارها.

## قواعد المشروع
- أضف ولا تهدم.
- لا حذف أو إعادة هيكلة للوحدات القديمة دون ضرورة هندسية حقيقية.
- فصل واجهة المستخدم عن منطق التداول.
- BOT 1 هو البوت الحقيقي المعتمد: `Grid_Martingale_Basket_v2`.
- BOT 2–4 خزائن/ملفات إعداد فقط، وليست محركات تداول فعلية.
- واجهة المستخدم تعرض الحالة ولا تنشئ حالة تداول وهمية.
- التداول الحقيقي لا يُفعل بمجرد إدخال بيانات الحساب.
- أي فشل في الصلاحيات/المخاطر/الأمان/الاتصال/صلاحية الأمر = BLOCK.

## B1-B20 — Foundation
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
B15 Analytics — مكتمل  
B16 Explainability — مكتمل  
B17 Guardian — مكتمل  
B18 Security Runtime — مكتمل  
B19 Audit — مكتمل  
B20 Supervisor — مكتمل  

## B21-B25 — Simulation / API / Adapter Architecture
B21 Testing Engine — مكتمل  
B22 Simulation Engine — مكتمل Paper/Demo only  
B23 AMAR GRID Integration — مكتمل كـplanner، دون تنفيذ وسيط  
B24 Trading Application API — مكتمل كحدود API  
B25 Broker Adapter Architecture — مكتمل كـcontract/boundary  

## B26-B33 — MT5 / Bridge / BOT1 Boundaries
B26 MT5 Adapter — معماري/حدود اتصال  
B27 Local Self-hosted Bridge — scaffold منفذ  
B28 Execution Safety Boundary — منفذ fail-closed  
B29 MT5 Read-Only Live Connection — حدود runtime منفذة، لا تنفيذ حي تلقائي  
B30 BOT1 Live State Mapping — contract منفذ  
B31 Secure Command Channel — HMAC/TTL/replay/idempotency/scope منفذ  
B32 Runtime Integration — scaffold منفذ  
B33 BOT1 Integration Boundary — منفذ كحد تنفيذ آمن  

## B34 — AMAR BOT RUNTIME PROFESSIONAL LAYER — منفذ
- Bot Identity: ID/Magic/Version/Strategy ID.
- Configuration and runtime state model.
- State machine: OFF/STARTING/RUNNING/STOPPING/REBUILDING/CLOSING/ERROR/EMERGENCY_LOCK.
- Command lifecycle: Validate → Accept → Execute → Acknowledge → Verify → Audit.
- Desired vs Actual reconciliation: MATCHED/DRIFT/UNKNOWN/ERROR.
- Execution/Position/Pending/Grid/Basket/Trailing/Martingale/Risk boundaries.
- Event/Audit/Telemetry boundaries.
- Persistent Bot Vault.
- BOT 1: ten independent strategy slots with save/edit/delete/reset behavior.
- Strategy metadata: risk profile, rebuild rule, entry rule and metadata notes.

## B35 — BOT 1 REAL EA INTEGRATION — غير مكتمل عمدًا
الحدود والـcontroller والـprotocol موجودة، لكن المصدر الأصلي `Grid_Martingale_Basket_v2.mq5` غير موجود في المستودع الحالي. لذلك لا يجوز ادعاء اكتمال الربط الفعلي أو Compile/Runtime verification في MT5 قبل إدخال المصدر واختباره.

عند توفر المصدر، يجب التحقق من:
- symbol + magic isolation لكل عملية؛
- عدم لمس صفقات/أوامر غير BOT1؛
- ACK + post-command verification؛
- reconciliation؛
- emergency lock/fail-closed؛
- Compile وسلوك فعلي في MT5.

## B36 — AMAR AI SUPERVISOR — معماري/حدود منفذة
AI ليس منفذ Broker مباشرًا. المسار المعتمد:
`AI Agent → Analysis/Proposal → Decision Engine → Simulation → Risk Policy → Execution Policy → Security Gateway → User Approval → MT5`

الأدوار: تحليل BOT1، الأداء والسحب، جودة التنفيذ، anomaly detection، تحليل السياق، تقييم الاستراتيجيات، مقترحات optimization عبر simulation، التقارير والشرح والمراقبة.

## Bot Vault
- BOT 1–4 كخانات إعداد.
- BOT 2–4 لا تُعتبر Bots فعلية.
- كل Bot يملك 10 Strategy Slots.
- الحفظ دائم عبر Android storage.
- الاستراتيجية المحفوظة تظهر بعلامة `✓` خضراء.
- التطبيع يمنع أكثر من 10 استراتيجيات ويزيل التكرار.
- Reset يمسح ملف الإعدادات المحفوظ فقط ولا يغلق MT5 أو الصفقات الحقيقية.

## CI Gate
الترتيب الإلزامي:
`Python bridge tests → Android SDK validation → Gradle model → clean → testDebugUnitTest → assembleDebug → APK checksum → artifact upload`

لا يعتبر أي تغيير مكتملًا حتى يمر CI بنجاح.

## Non-negotiable safety
`liveTrading=false` افتراضيًا. لا توجد صلاحية تداول حقيقي لمجرد اختيار حساب أو إدخال credentials. كل تنفيذ حي يمر عبر صلاحيات ومخاطر وأمان وقناة أوامر وتحقق لاحق.
