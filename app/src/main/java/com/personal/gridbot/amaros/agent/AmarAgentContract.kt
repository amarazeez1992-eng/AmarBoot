package com.personal.gridbot.amaros.agent

/** Future AI Agent boundary. Provider-agnostic and incapable of direct execution. */
data class AmarAgentRequest(val sessionId: String, val instruction: String, val context: Map<String, String> = emptyMap())
data class AmarAgentResponse(val sessionId: String, val message: String, val confidence: Double = 0.0, val traceId: String? = null)

interface AmarAgentProvider {
    suspend fun respond(request: AmarAgentRequest): AmarAgentResponse
}

interface AmarAgentTool {
    val name: String
    suspend fun execute(input: Map<String, String>): Map<String, String>
}
