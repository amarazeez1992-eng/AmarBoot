package com.personal.gridbot.amaros.security

/** Pure security validation rules shared by network-bound configuration code. */
object AmarSecurityContracts {
    private const val MAX_BEARER_TOKEN_LENGTH = 4096

    fun isAllowedSyncEndpoint(endpoint: String): Boolean {
        val normalized = endpoint.trim()
        return normalized.startsWith("https://") ||
            normalized.startsWith("http://10.") ||
            normalized.startsWith("http://192.168.")
    }

    fun requireSyncConfiguration(endpoint: String, token: String) {
        require(isAllowedSyncEndpoint(endpoint)) { "Sync endpoint must use HTTPS or an explicitly local private-network HTTP address" }
        require(token.isNotBlank()) { "Sync bearer token is required" }
        require(token.length <= MAX_BEARER_TOKEN_LENGTH) { "Sync bearer token is too long" }
    }
}
