package com.github.gypsyjr777.ai.config

import kotlinx.serialization.Serializable

@Serializable
data class SearchConfig(
    val apiKey: String,
    val csi: String?,
)
