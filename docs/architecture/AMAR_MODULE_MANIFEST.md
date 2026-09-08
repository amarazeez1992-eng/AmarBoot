# AMAR Module Manifest

## Purpose
المرجع الموحد لتسجيل وحدات AMAR ومسؤولياتها واعتمادياتها وحالتها وإصدارها.

## قواعد المشروع
- أضف ولا تهدم.
- لا حذف أو إعادة هيكلة للوحدات القديمة دون اعتماد صريح.
- فصل واجهة المستخدم عن منطق التداول.
- لا تداول حقيقي في المرحلة الحالية.
- لا تتم إضافة أي بوت جديد ضمن هذه المرحلة.

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

## حدود هذه المرحلة
هذه المرحلة تأسيس معماري وواجهة فقط. لا تضيف بوتات أو استراتيجيات تداول جديدة، ولا تفعل LIVE trading.
