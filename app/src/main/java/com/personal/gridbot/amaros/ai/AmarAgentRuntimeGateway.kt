package com.personal.gridbot.amaros.ai

/**
 * Single runtime entry point for user requests reaching the AMAR Agent.
 *
 * UI/voice command handling is separated from the canonical Agent Engine,
 * while every non-command request enters AmarAiAgentEngine exactly once.
 */
class AmarAgentRuntimeGateway(
    private val agentEngine: AmarAiAgentEngine
) {
    data class Result(
        val answer: String,
        val status: String
    )

    suspend fun ask(request: String): Result {
        val text = request.trim()
        require(text.isNotEmpty()) { "طلب AMAR AI فارغ." }

        val local = AmarAiActionEngine.route(text)
        if (local.handled) {
            return Result(local.response, "تم تنفيذ أمر الواجهة")
        }

        val result = agentEngine.ask("", "", text)
        return Result(result.answer, "Agent: جاهز")
    }
}
