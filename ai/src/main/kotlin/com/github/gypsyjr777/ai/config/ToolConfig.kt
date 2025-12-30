package com.github.gypsyjr777.ai.config

abstract class ToolConfig(
    open val retries: Int,
    open val logRequests: Boolean,
    open val logResponses: Boolean
)