# AMAR BOT — Grid Trade Bot

مشروع تداول شخصي مبني على Kotlin/Jetpack Compose مع طبقة BOT 1 runtime، MT5 bridge architecture، security boundaries وBot Vault.

## الحالة الحالية

- **BOT 1**: البوت الحقيقي المعتمد، مع runtime identity/state/config و10 strategy slots.
- **BOT 2–4**: خزائن إعداد فقط؛ لا توجد محركات تداول مستقلة لها بعد.
- **Bot Vault**: حفظ دائم، إضافة/تعديل/حذف/تفريغ Bots، وحفظ/تحرير/حذف/تفريغ Strategies.
- **Security**: Fail-closed، TTL، replay/idempotency، scope validation وreceiver-side HMAC verification.
- **AI Supervisor**: استشاري فقط؛ لا يملك صلاحية تنفيذ Broker مباشرة.
- **Bot Lab**: واجهة 1 هي الواجهة النشطة الوحيدة، مع الحفاظ على مسار BOT1 التشغيلي والتحقق منه. واجهة 2 متقاعدة.
- **MT5**: الربط الحي ما زال gated حتى اكتمال Compile وDemo runtime verification.

## B34 — Runtime
المسار التشغيلي المعتمد:

`UI → Command → Validate → Accept → Execute → ACK → Verify → Reconcile → Audit`

الحالة الفعلية لا تُصنع من زر في الواجهة؛ يجب أن تأتي من runtime/MT5.

## B35 — MT5 EA
تم فحص مصدر EA الموجود ضمن ملفات المشروع. كود البوت الأساسي لا يُعدَّل ضمن تحسينات التطبيق والواجهة. قبل أي Live authorization يجب تثبيت المصدر في مسار MT5 بالمستودع، ثم تنفيذ:

1. symbol + magic isolation؛
2. فصل BOT1-only عن manual-managed؛
3. منع global close أثناء grid rebuild؛
4. broker stop-level/volume/price validation؛
5. retcode handling؛
6. ACK + read-back + reconciliation؛
7. MetaEditor compile؛
8. Demo runtime tests.

## B36 — AI Supervisor
AI يقدم تحليلًا واقتراحات فقط:

`AI → Proposal → Decision → Simulation → Risk Policy → Execution Policy → Security Gateway → User Approval → MT5`

لا يوجد Direct AI Trading.

## Safety

`liveTrading=false` هو الوضع الآمن الافتراضي. إدخال بيانات الحساب لا يمنح صلاحية تداول. Emergency Lock وأي فشل في permission/risk/security/connector/idempotency يؤدي إلى **BLOCK**.

## CI

Workflow البناء يحتوي على Python bridge tests، Android SDK validation، Gradle validation، unit tests، assembleDebug، APK checksum وartifact upload.

لا نعتبر التغيير ناجحًا حتى تظهر نتيجة CI الفعلية بنجاح؛ غياب run ليس Pass.
