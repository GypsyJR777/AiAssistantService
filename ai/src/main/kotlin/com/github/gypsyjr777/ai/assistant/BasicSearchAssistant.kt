package com.github.gypsyjr777.ai.assistant

import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.TokenStream
import dev.langchain4j.service.UserMessage
import java.util.UUID

interface BasicSearchAssistant {
    @SystemMessage(
        "You are a web search support agent.",
        "If there is any event that has not happened yet",
        "You MUST create a web search request with user query and",
        "use the web search tool to search the web for organic web results.",
        "Include the source link in your final response.",
    )
    fun chat(
        @MemoryId memoryId: UUID,
        @UserMessage userMessage: String?,
    ): TokenStream
}
