package com.github.gypsyjr777.ai.config

import kotlinx.serialization.Serializable

@Serializable
data class SearchConfig(
    val apiKey: String,
    val csi: String?,
    override val retries: Int = 3,
    override val logRequests: Boolean = false,
    override val logResponses: Boolean = false,
) : ToolConfig(retries, logRequests, logResponses)
