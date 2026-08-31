# Grid Trade Bot - تطبيق شخصي

بوت تداول شبكي حقيقي (Grid Trading) لحساب MT5 عبر MetaApi.cloud، بواجهة Compose بنفس فكرة التطبيق الأصلي.

## خطوات التشغيل (من الموبايل بالكامل، بدون لابتوب)

### 1) رفع المشروع على GitHub
- افتح تطبيق/موقع GitHub من الموبايل.
- أنشئ Repository جديد (خاص Private أفضل).
- ارفع كل هذي الملفات والمجلدات (Add file → Upload files) بنفس الهيكلية.

### 2) تفعيل البناء التلقائي
- بعد الرفع، روح لتبويب **Actions** بالمستودع.
- فعّل الـ Workflow (إذا طلب تفعيل).
- كل مرة ترفع تعديل، البناء يشتغل تلقائيًا ويطلع ملف APK جاهز تحت "Artifacts".

### 3) تحميل الـ APK وتثبيته
- من صفحة الـ Actions run، نزّل `GridTradeBot-debug-apk` (ملف zip فيه الـ APK).
- فكه وثبّته على جوالك (فعّل "تثبيت من مصادر غير معروفة" إذا طلب أندرويد ذلك).

### 4) قبل التشغيل - عبّي بيانات MetaApi
افتح ملف:
`app/src/main/java/com/personal/gridbot/network/MetaApiClient.kt`

وعدّل هذي السطرين ببياناتك الحقيقية من metaapi.cloud بعد ربط حساب JustMarkets Demo:

```kotlin
var authToken: String = "PUT_YOUR_METAAPI_TOKEN_HERE"
var accountId: String = "PUT_YOUR_METAAPI_ACCOUNT_ID_HERE"
```

⚠️ لا تشارك الـ Token مع أي أحد، ولا ترفعه على مستودع عام (Public).

## تحذير
هذا كود أولي (Starter) لبوت تداول حقيقي. اختبره على حساب Demo فترة كافية، وراقب السجلات (`bot_logs`) قبل أي تفكير باستخدامه بحساب حقيقي. التداول الآلي فيه مخاطر مالية حقيقية.
