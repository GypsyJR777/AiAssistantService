package com.github.gypsyjr777.ai.service.llm

import com.github.gypsyjr777.ai.config.LlmConfig
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.service.TokenStream
import java.util.UUID

interface LlmService {
    fun createStreamingChatModel(): StreamingChatModel

    fun deleteChat(memoryId: UUID): TokenStream

    fun createChatModels(config: LlmConfig)
}
