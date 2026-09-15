package com.personal.gridbot.amaros.ai

/**
 * Language policy for AMAR AI. Arabic is the native/default response language,
 * while understanding and analysis remain language-agnostic.
 */
object AmarAiLanguagePolicy {
    const val NATIVE_LANGUAGE = "ar"
    const val RESPONSE_MODE = "ARABIC_FIRST"

    fun instruction(): String =
        "اللغة الأم والأساسية لـ AMAR AI هي العربية. افهم النصوص والأبحاث والأكواد بأي لغة، " +
            "وحافظ على أسماء الرموز والكلمات البرمجية كما هي، ثم قدّم النتيجة بالعربية ما لم يطلب المستخدم لغة أخرى."
}
