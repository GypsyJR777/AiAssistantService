package com.github.gypsyjr777.ai.config

data class LlmConfig(
    val address: String,
    val temperature: Double = 1.0,
    val logRequests: Boolean = false,
    val logResponse: Boolean = false,
    val modelName: String?,
    val timeout: Long = 60
)