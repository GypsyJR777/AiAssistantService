package com.github.gypsyjr777.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName

@ConfigMapping(prefix = "assistant")
interface AssistantToolConfig {
    @WithName("config.path")
    fun path(): String?

    @WithName("assistants")
    fun assistants(): List<String>
}
