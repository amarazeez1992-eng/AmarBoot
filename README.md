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

> **Protected baseline:** `mt5/Experts/Grid_Martingale_Basket_v2.mq5` version 2.01, SHA-1 `1125cabef6b4a4b1d5eb2c5deaa4b5e7458479ad25`. It remains unchanged by application/AI hardening work.

## B36 — AI Supervisor
AI يقدم تحليلًا واقتراحات فقط:

`AI → Proposal → Decision → Simulation → Risk Policy → Execution Policy → Security Gateway → User Approval → MT5`

لا يوجد Direct AI Trading.

## Safety

`liveTrading=false` هو الوضع الآمن الافتراضي. إدخال بيانات الحساب لا يمنح صلاحية تداول. Emergency Lock وأي فشل في permission/risk/security/connector/idempotency يؤدي إلى **BLOCK**.

## Open Source

AmarBoot is now an open-source repository. AMAR-owned source code is licensed under the MIT License.
Third-party software, scripts, APIs, data, trademarks, and documentation remain subject to their own licenses and terms.

Before reusing TradingView, GitHub, LuxAlgo, bot, or strategy material, check provenance and the original license. Public visibility is not blanket permission to copy proprietary content.

See:
- `LICENSE`
- `SECURITY.md`
- `CONTRIBUTING.md`
- `CODE_OF_CONDUCT.md`
- `docs/OPEN_SOURCE_POLICY.md`
- `docs/security/AMAR_SECURITY_AUDIT_2026-09-12.md`

## Security

The repository uses a fail-closed execution model. Queue acceptance is never treated as broker execution success.
Execution requires authenticated/scoped commands, idempotency/replay controls, ACK, runtime read-back,
reconciliation, and audit evidence.

Public-repository security controls include Dependabot configuration, CodeQL scanning, and local-secret exclusions.

## CI

Workflow البناء يحتوي على Python bridge tests، Android SDK validation، Gradle validation، unit tests، assembleDebug، APK checksum وartifact upload.

لا نعتبر التغيير ناجحًا حتى تظهر نتيجة CI الفعلية بنجاح؛ غياب run ليس Pass.
