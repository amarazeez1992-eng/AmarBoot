package com.personal.gridbot.amaros.environment

import com.personal.gridbot.BuildConfig

enum class AmarEnvironment {
    DEBUG,
    PROD;

    companion object {
        fun current(): AmarEnvironment =
            if (BuildConfig.DEBUG) DEBUG else PROD
    }
}
