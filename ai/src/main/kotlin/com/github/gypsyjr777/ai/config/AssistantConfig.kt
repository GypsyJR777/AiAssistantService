package com.github.gypsyjr777.ai.config

import kotlinx.serialization.Serializable

@Serializable
data class AssistantConfig(
    val llm: Map<String, LlmConfig>,
    val tools: Map<String, ToolConfig>?,
)
