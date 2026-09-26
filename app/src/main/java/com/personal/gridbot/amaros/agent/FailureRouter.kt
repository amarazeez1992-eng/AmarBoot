package com.personal.gridbot.amaros.agent

enum class FailureRoute { RETRY, FALLBACK, BLOCK, TERMINATE }
data class FailureRouteDecision(val route: FailureRoute, val reason: String)

class FailureRouter {
    fun route(failure: Throwable, retryAvailable: Boolean, fallbackAvailable: Boolean): FailureRouteDecision {
        if (failure.message.isNullOrBlank()) return FailureRouteDecision(FailureRoute.BLOCK, "unclassified_failure")
        return when {
            failure is IllegalArgumentException -> FailureRouteDecision(FailureRoute.BLOCK, "invalid_input")
            retryAvailable -> FailureRouteDecision(FailureRoute.RETRY, "retry_available")
            fallbackAvailable -> FailureRouteDecision(FailureRoute.FALLBACK, "fallback_available")
            else -> FailureRouteDecision(FailureRoute.TERMINATE, "no_recovery_path")
        }
    }
}
