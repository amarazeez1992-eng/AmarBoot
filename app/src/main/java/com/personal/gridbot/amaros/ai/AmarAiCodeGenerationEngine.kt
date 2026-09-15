package com.personal.gridbot.amaros.ai

/**
 * Draft-only code-generation request envelope. It never executes, installs, publishes,
 * or claims that generated code compiled successfully. Actual generation is delegated
 * to the configured reasoning provider.
 */
object AmarAiCodeGenerationEngine {
    data class Draft(
        val language: String,
        val requirement: String,
        val context: String,
        val instruction: String,
        val validationPlan: List<String>,
        val status: String = "DRAFT_ONLY"
    )

    fun request(language: String, requirement: String, context: String = ""): Draft {
        val normalized = language.trim().ifBlank { "auto-detect" }
        require(requirement.isNotBlank()) { "Code-generation requirement must not be blank" }
        return Draft(
            language = normalized,
            requirement = requirement.trim(),
            context = context.trim(),
            instruction = buildInstruction(normalized, requirement.trim(), context.trim()),
            validationPlan = listOf(
                "تحليل المتطلب واللغة والسياق",
                "فحص الصياغة والبنية والاعتماديات",
                "اختبارات الوحدة والحالات الحدية",
                "Build/Compile عند توفر بيئة اللغة",
                "فحص الأمان والأداء والتوافق",
                "مقارنة السلوك الفعلي بالمتطلب"
            )
        )
    }

    private fun buildInstruction(language: String, requirement: String, context: String): String =
        buildString {
            append("Generate complete $language code for the following requirement.\n")
            append("Requirement: $requirement\n")
            if (context.isNotBlank()) append("Context: $context\n")
            append("Return code plus concise explanation, assumptions, and validation steps.\n")
            append("Never claim compilation or runtime success without evidence.")
        }
}
