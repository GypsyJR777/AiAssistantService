package com.github.gypsyjr777.controller

import com.github.gypsyjr777.ai.config.AssistantConfig
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

@Path("/search")
class OllamaSearchController(
    val config: AssistantConfig,
) {
    @GET()
    fun index() = config.llm
}
