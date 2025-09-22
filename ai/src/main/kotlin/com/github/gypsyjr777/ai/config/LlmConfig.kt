package com.github.gypsyjr777.ai.config

import kotlinx.serialization.Serializable

@Serializable
data class LlmConfig(
    val address: String,
    val platform: Platform = Platform.OLLAMA,
    val temperature: Double = 1.0,
    val logRequests: Boolean = false,
    val logResponse: Boolean = false,
    val modelName: String,
    val timeout: Long = 60,
    val assistants: List<String> = listOf("com.github.gypsyjr777.ai.assistant.BasicSearchAssistant"),
    val apiKey: String? = null,
)
