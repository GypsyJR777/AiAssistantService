package com.github.gypsyjr777.ai.service

import java.util.UUID

interface LLMService {
    fun chat(
        memoryId: UUID,
        userMessage: String,
        assistantName: String,
        partialAction: (text: String?) -> Unit,
        completeAction: (text: String?) -> Unit,
        failAction: (text: Throwable?) -> Unit,
    )
}