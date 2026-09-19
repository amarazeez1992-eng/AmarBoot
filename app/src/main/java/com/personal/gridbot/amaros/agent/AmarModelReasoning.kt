package com.personal.gridbot.amaros.agent

/**
 * Adapts the canonical AmarModelProvider into the existing Agent reasoning contract.
 * It does not create a second Agent path.
 */
class AmarModelReasoning(
    private val modelProvider: AmarModelProvider,
    private val fallback: AmarReasoningProvider = AmarLocalReasoning()
) : AmarReasoningProvider {

    @Volatile
    private var modelReady = false

    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse {
        return runCatching {
            if (!modelReady) {
                val info = modelProvider.load("")
                if (!info.loaded) {
                    return@runCatching null
                }
                modelReady = true
            }

            val generation = modelProvider.generate(
                AmarGenerationRequest(
                    systemPrompt = """
                        أنت AMAR AI Agent.
                        افهم طلب المستخدم باللغة الطبيعية ولا تعتمد على قائمة أسئلة ثابتة.
                        أجب مباشرة وبوضوح وباللغة المناسبة لطلب المستخدم.
                        استخدم السياق والأدلة التي يمررها لك النظام، ولا تخترع مصادر أو أرقاماً.
                        لا تنفذ أوامر تداول ولا تدّعي تنفيذها.
                        إذا كانت المعلومة الحالية تحتاج إلى بحث خارجي ولم تصل أدلة موثوقة، صرّح بذلك بوضوح.
                    """.trimIndent(),
                    userPrompt = context.userText,
                    context = buildString {
                        appendLine("executionAllowed=${context.executionAllowed}")
                        appendLine("brokerAccessAllowed=${context.brokerAccessAllowed}")
                        appendLine("requestedSourceCount=${context.requestedSourceCount}")
                        appendLine("maximumSourceCount=${context.maximumSourceCount}")
                    },
                    maxTokens = 768,
                    temperature = 0.2
                )
            )

            AmarAgentResponse(
                answer = generation.text,
                actions = context.tools.map { it.id }
            )
        }.getOrNull() ?: fallback.respond(context)
    }
}
