package com.personal.gridbot.amaros.agent

/**
 * Compatibility boundary for provider implementations.
 * The canonical request/response/tool types live in AmarAgentCore.kt.
 * This file intentionally does not redeclare them, avoiding duplicate JVM/Kotlin types.
 */
interface AmarAgentProvider {
    suspend fun respond(request: AmarAgentRequest): AmarAgentResponse
}

/** Legacy tool contract retained under a distinct name until all integrations migrate. */
interface AmarLegacyAgentTool {
    val name: String
    suspend fun execute(input: Map<String, String>): Map<String, String>
}
