package com.github.gypsyjr777.ai.assistant

import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.TokenStream
import dev.langchain4j.service.UserMessage
import java.util.UUID

interface Assistant {
    fun chat(
        @MemoryId memoryId: UUID,
        @UserMessage userMessage: String,
    ): TokenStream
}
