package com.github.gypsyjr777.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName
import io.smallrye.config.WithParentName

@ConfigMapping(prefix = "bot")
interface BotConfig {
    @WithParentName
    fun getBot(): Map<String, BotParam>?

    interface BotParam {
        @WithName("token")
        fun token(): String
        @WithName("name")
        fun name(): String
    }
}
