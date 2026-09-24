# AMAR AI — STAGE 1
# FOUNDATION
## STAGE1_SPECIFICATION.md

**Status:** Approved Specification  
**Constitutional Source:** `docs/governance/AMAR-AI-MASTER-CONSTITUTION.md`

**Evidence rule:** Every Acceptance Criterion must be supported by a Commit hash, CI Run ID, and Test name. Without all three, the criterion is not treated as proven evidence.

**Closure rule:** An Item may be **Covered** when its required evidence-backed requirements are present. An Item may remain **Partial** and be moved to the Stage 1 Parking Lot with a documented reason; Partial status alone does not prevent constitutional Stage 1 closure.

| Item | Purpose | Required Code | Required Tests | Acceptance Criteria | Evidence Required |
|---|---|---|---|---|---|
| 1. Project Architecture | تثبيت بنية معمارية واضحة ومعزولة وقابلة للاختبار والتحديث والاستبدال. | الحدود والوحدات والعقود المطلوبة فعليًا، دون تبعيات غير مسموحة. | فحوص الحدود والاستبدالية حيث تنطبق. | الحدود محددة وقابلة للتحقق، ولا يوجد اعتماد حرج غير مبرر. | Commit hash + CI Run ID + Test name لكل معيار. |
| 2. Application Core | توفير النواة التطبيقية التي تنظم دورة التشغيل والمسؤوليات الأساسية. | مكونات Core وlifecycle الأساسية. | إنشاء وتشغيل Core وlifecycle وحالات الفشل الأساسية. | Core يعمل وقابل للاختبار ومسؤولياته محددة. | Commit hash + CI Run ID + Test name لكل معيار. |
| 3. Module System | تنظيم الوحدات بما يسمح بالتوسع دون كسر الحدود. | Modules/Packages وعقود التكامل المطلوبة. | حدود التكامل والاستقلالية والفشل عند الحاجة. | كل Module محدد المسؤولية وقابل للاختبار والتحديث والاستبدال حيث ينطبق. | Commit hash + CI Run ID + Test name لكل معيار. |
| 4. Configuration System | إدارة الإعدادات مركزيًا وبشكل قابل للتحكم والاختبار. | Configuration وقيم افتراضية وفصل الأسرار. | القراءة والقيم الافتراضية والقيم غير الصالحة وعدم تسريب الأسرار. | الإعدادات قابلة للتحقق ولا تحتوي أسرارًا ثابتة. | Commit hash + CI Run ID + Test name لكل معيار. |
| 5. Environment Management | فصل بيئات التشغيل ومنع خلط credentials. | تمثيل DEV/DEBUG/PROD وربط Configuration بالبيئة. | فصل البيئات ومحاولة خلط credentials. | البيئة محددة والخلط ممنوع ومختبر. | Commit hash + CI Run ID + Test name لكل معيار. |
| 6. Data Models | توفير نماذج بيانات واضحة وثابتة وقابلة للتحقق. | Models/Entities وقواعد validation عند الحاجة. | إنشاء وvalidation والقيم الحدية وserialization عند الاستخدام. | النماذج تمثل البيانات المطلوبة وسلوكها الحدّي قابل للاختبار. | Commit hash + CI Run ID + Test name لكل معيار. |
| 7. Service Layer | فصل منطق الخدمات عن الواجهات والبنية التحتية المباشرة. | Services ومسؤولياتها وإدارة الفشل عند الحاجة. | السلوك الطبيعي وفشل dependencies وsafe recovery حيث ينطبق. | الخدمات قابلة للاختبار ومسؤولياتها واضحة. | Commit hash + CI Run ID + Test name لكل معيار. |
| 8. API Layer | توفير طبقة API منظمة وآمنة. | API clients/interfaces وrequest/response models وerror handling. | request/response والفشل والtimeout والقيود الأمنية عند الحاجة. | التفاعل قابل للاختبار ولا يتحول الفشل إلى نجاح وهمي ولا تُكشف الأسرار. | Commit hash + CI Run ID + Test name لكل معيار. |
| 9. Storage Foundation | توفير تخزين محلي موثوق مع سلامة بيانات قابلة للتحقق. | Storage/Room وDAO واستراتيجية migration/versioning. | CRUD وmigration وإعادة الفتح وسلامة البيانات. | البيانات تبقى سليمة عبر migrations والفشل له سلوك آمن. | Commit hash + CI Run ID + Test name لكل معيار. |
| 10. Logging Foundation | توفير Logging موحد وقابل للاختبار ويحمي البيانات الحساسة. | AmarLogger وAmarLogLevel وAmarLogEvent وAmarLogSink وredaction. | المستويات وcontext/timestamp وtag وredaction وunit execution. | Logger يعمل والمستويات صحيحة والقيم الحساسة لا تظهر. | Commit hash + CI Run ID + Test name لكل معيار. |
| 11. Error Handling | إدارة الأخطاء بصورة صريحة وقابلة للتتبع والاسترداد. | Failure representation ومعالجة/propagation/recovery. | الأخطاء المتوقعة وغير المتوقعة وsafe failure عند الحاجة. | كل failure حرج قابل للرصد ولا يوجد نجاح وهمي. | Commit hash + CI Run ID + Test name لكل معيار. |
| 12. Versioning | إدارة App/Schema/Contract versions ومنع عدم التوافق الصامت. | App version وSchema version وContract version عند الانطباق وآلية mismatch. | matching وmismatch والسلوك عند عدم التوافق. | كل version مطلوب له مصدر واضح وmismatch لا يمر بصمت. | Commit hash + CI Run ID + Test name لكل معيار. |
| 13. Build System | ضمان بناء المشروع بصورة قابلة للتكرار والتحقق. | Gradle/Kotlin/Android configuration والاعتمادات. | build وunit tests وregression وartifact generation حيث ينطبق. | المشروع يبنى والاختبارات المطلوبة تمر والفشل قابل للتشخيص. | Commit hash + CI Run ID + Test name لكل معيار. |
| 14. Android Foundation | توفير أساس Android مستقر ومتوافق. | Application/Activity وAndroid configuration وlifecycle/background foundation عند الحاجة. | build وstartup/lifecycle والتوافق والسلوك الخلفي عند الحاجة. | APK/build صالح والأساس يعمل ضمن SDK المعتمد. | Commit hash + CI Run ID + Test name لكل معيار. |
| 15. Security Foundation | حماية credentials والبيانات الحساسة وفرض القيود الأمنية الأساسية. | Security contracts/runtime وsecure storage وauthorization/safety gates والحماية. | credential protection وunauthorized access وsecure storage وsafety behavior. | الأسرار محمية والوصول غير المصرح مرفوض والفشل الأمني لا يتحول لنجاح. | Commit hash + CI Run ID + Test name لكل معيار. |
| 16. Testing Foundation | توفير أساس اختبار مستمر قابل للتتبع. | Test infrastructure وCI integration. | unit/integration/regression/failure/security tests حسب الانطباق. | الاختبارات المطلوبة تعمل في CI ونتائجها قابلة للتتبع ولا تُخفى failures. | Commit hash + CI Run ID + Test name لكل معيار. |

## Stage 1 Global Acceptance

1. مراجعة Items الـ16 وتسجيل الحالة النهائية لكل Item.
2. كل Acceptance Criterion مثبت بـCommit hash + CI Run ID + Test name، أو يُسجل بوضوح كـPartial/غير مثبت.
3. الفجوات المعتمدة تُغلق أو تُنقل إلى Parking Lot مع سبب موثق.
4. Partial Item لا يمنع إغلاق Stage 1 إذا كان منقولًا إلى Parking Lot بسبب موثق.
5. إعداد Constitutional Closure Record.
6. Main Re-check بعد الدمج.
7. توقيع الإغلاق الدستوري.

**هذه الوثيقة مواصفة Stage 1 ولا تستبدل الدستور. عند التعارض، يسود `AMAR-AI-MASTER-CONSTITUTION.md`.**
