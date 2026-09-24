package com.personal.gridbot.amaros.environment

import com.personal.gridbot.BuildConfig

enum class AmarEnvironment {
    DEV,
    DEBUG,
    PROD;

    companion object {
        fun current(): AmarEnvironment =
            if (BuildConfig.DEBUG) DEBUG else PROD
    }
}
