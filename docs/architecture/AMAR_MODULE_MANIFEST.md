# AMAR Module Manifest

## Purpose
المرجع الموحد لتسجيل وحدات AMAR ومسؤولياتها واعتمادياتها وحالتها وإصدارها.

## قواعد المشروع
- أضف ولا تهدم.
- فصل واجهة المستخدم عن منطق التداول.
- BOT 1 هو البوت الحقيقي المعتمد؛ BOT 2–4 خزائن إعداد فقط.
- UI لا يصنع حالة تداول وهمية؛ runtime/MT5 هو مصدر الحالة الفعلية.
- التداول الحقيقي لا يُفعل بمجرد اختيار حساب أو إدخال بيانات.
- أي فشل في الصلاحيات أو المخاطر أو الأمان أو الاتصال أو صلاحية الأمر = BLOCK.

## B1-B20 — Foundation
B1-B20 — مكتمل وفق الوحدات السابقة؛ B20 Supervisor مراقب/استشاري وليس منفذ Broker.

## B21-B25 — Simulation / API / Adapter Architecture
B21 Testing Engine — مكتمل  
B22 Simulation Engine — Paper/Demo only  
B23 AMAR GRID Integration — planner دون تنفيذ وسيط  
B24 Trading Application API — حدود API  
B25 Broker Adapter Architecture — contract/boundary

## B26-B33 — MT5 / Bridge / BOT1 Boundaries
B26 MT5 Adapter — حدود اتصال  
B27 Local Self-hosted Bridge — scaffold  
B28 Execution Safety Boundary — fail-closed  
B29 MT5 Read-Only Live Connection — runtime boundary، دون تنفيذ حي تلقائي  
B30 BOT1 Live State Mapping — contract  
B31 Secure Command Channel — HMAC/TTL/replay/idempotency/scope + receiver-side signature verification  
B32 Runtime Integration — scaffold  
B33 BOT1 Integration Boundary — تنفيذ آمن

## B34 — AMAR BOT RUNTIME PROFESSIONAL LAYER — منفذ
- Identity/config/state machine: OFF/STARTING/RUNNING/STOPPING/REBUILDING/CLOSING/ERROR/EMERGENCY_LOCK.
- Command lifecycle مع انتقالات مغلقة: CREATED → VALIDATED → ACCEPTED → EXECUTING → ACKNOWLEDGED → VERIFIED.
- Desired vs Actual reconciliation: MATCHED/DRIFT/UNKNOWN/ERROR.
- Fail-closed Execution Policy بجميع بوابات permission/risk/security/connector/idempotency.
- Persistent Bot Vault: إضافة/تعديل/حذف/تفريغ BOT؛ BOT 1 محمي من الحذف.
- عشر خانات Strategy لكل Bot، مع حفظ/تحرير/حذف/تفريغ وعلامة `✓` للحفظ.
- Strategy metadata: risk/rebuild/entry/notes.
- UI يعرض طلب الأمر ولا يزيف runtime state.

## B35 — BOT 1 REAL EA INTEGRATION — قيد التنفيذ / غير مصرح Live
تم العثور على المصدر الحقيقي `Grid_Martingale_Basket_AMAR_v3.mq5` وفحصه. المصدر يتضمن Grid/Pending/Tracking/Basket/Individual/Enhancement/Trailing/Profile engines. fileciteturn1342file0L19-L68

حالة B35 الحالية: **SOURCE AUDITED / INTEGRATION NOT YET VERIFIED**.
المتبقي الإلزامي قبل أي Live authorization:
- إدخال المصدر إلى المستودع تحت مسار MT5 ثابت؛
- إصلاح عزل الصفقات: الدوال الحالية تعتبر الصفقات اليدوية managed عندما `ManageManualPositions=true`، بينما `CloseAll` وBasket قد يغلقانها؛ يجب فصل `BOT1_ONLY` عن `MANUAL_MANAGED` قبل الربط الحي. fileciteturn1342file0L76-L121
- منع BuildGrid من استخدام `CloseAll` العالمي كأثر جانبي لإعادة البناء؛
- التحقق من stop-level/price validity وtrade retcodes؛
- ربط command ACK + post-command verification + reconciliation؛
- Compile واختبار سلوك فعلي في MT5 Demo.

## B36 — AMAR AI SUPERVISOR — منفذ كطبقة استشارية
- Supervisor proposal model موجود ويمنع `executable=true`.
- AI proposal لا يمتلك broker/bridge execution capability.
- المسار الإلزامي: AI Analysis → Proposal → Decision → Simulation → Risk Policy → Execution Policy → Security Gateway → User Approval → MT5.
- لا يوجد Direct AI Trading.

## Bot Vault
- BOT 1–4 افتراضيًا؛ BOT 2–4 Vault only.
- يمكن إضافة Bots جديدة كخانات إعداد.
- Delete محظور لـ BOT 1 من طبقة Vault.
- Reset/تفريغ يمسح الإعداد المحفوظ فقط ولا يلمس MT5.
- كل Bot يملك حتى 10 Strategies.

## CI Gate
الترتيب الإلزامي: Python bridge tests → Android SDK validation → Gradle model → clean → testDebugUnitTest → assembleDebug → APK checksum → artifact upload.

لا يعتبر B35 أو المشروع Live-ready إلا بعد نجاح CI وCompile/Runtime verification في MT5 Demo.

## Non-negotiable safety
`liveTrading=false` افتراضيًا. لا توجد صلاحية تداول حقيقي لمجرد إدخال credentials. Emergency Lock وRisk/Security/Permission gates تفشل إلى BLOCK.
