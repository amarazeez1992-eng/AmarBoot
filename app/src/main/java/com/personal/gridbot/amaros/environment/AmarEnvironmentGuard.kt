package com.personal.gridbot.amaros.environment

object AmarEnvironmentGuard {
    fun requireEnvironment(required: AmarEnvironment) {
        val current = AmarEnvironment.current()
        check(current == required) {
            "Environment mismatch: required=$required, current=$current"
        }
    }
}
