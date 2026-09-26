package com.personal.gridbot.amaros.agent

class AmarModelTaskClassifier {
    fun classify(context: AmarAgentContext): AmarModelTask {
        val text = context.userText.lowercase()
        return when {
            text.isBlank() -> AmarModelTask.UNKNOWN
            listOf("compare", "comparison", "multi-factor", "multiple factors", "قارن", "مقارنة", "عدة عوامل").any(text::contains) ->
                AmarModelTask.MULTI_FACTOR_ANALYSIS
            listOf("generate", "write", "draft", "اكتب", "أنشئ", "صياغة").any(text::contains) ->
                AmarModelTask.TEXT_GENERATION
            else -> AmarModelTask.SIMPLE_EXPLANATION
        }
    }
}
