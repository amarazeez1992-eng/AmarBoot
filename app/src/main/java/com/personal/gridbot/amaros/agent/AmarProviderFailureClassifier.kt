package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.TimeoutCancellationException
import java.io.IOException

enum class AmarProviderFailureClass {
    TIMEOUT, TRANSIENT, PERMANENT, MALFORMED_RESULT, UNKNOWN
}

class AmarProviderFailureClassifier {
    fun classify(error: Throwable): AmarProviderFailureClass = when (error) {
        is TimeoutCancellationException -> AmarProviderFailureClass.TIMEOUT
        is IOException -> AmarProviderFailureClass.TRANSIENT
        is IllegalArgumentException,
        is SecurityException -> AmarProviderFailureClass.PERMANENT
        is AmarMalformedProviderResultException -> AmarProviderFailureClass.MALFORMED_RESULT
        else -> AmarProviderFailureClass.UNKNOWN
    }
}

class AmarMalformedProviderResultException(message: String) : IllegalStateException(message)
