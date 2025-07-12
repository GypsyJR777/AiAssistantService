package com.github.gypsyjr777.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName

@ConfigMapping(prefix = "ollama")
interface OllamaConfig {
    @WithName("host")
    fun host(): String?

    @WithName("port")
    fun port(): Int?
}