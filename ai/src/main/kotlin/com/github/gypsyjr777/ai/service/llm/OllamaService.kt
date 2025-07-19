package com.github.gypsyjr777.ai.service.llm

import com.github.gypsyjr777.ai.config.LlmConfig
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaModel
import dev.langchain4j.model.ollama.OllamaModels
import dev.langchain4j.model.output.Response
import dev.langchain4j.service.TokenStream
import java.util.UUID

class OllamaService : LlmService {
    private val chatModels: MutableMap<String, ChatModel> = mutableMapOf()
    private val fullAddress: String = "http://localhost:7869"

    constructor(config: LlmConfig) {
        createChatModels(config)
    }

    private fun getModels(): List<OllamaModel> {
        val response: Response<List<OllamaModel>> =
            OllamaModels
                .builder()
                .baseUrl(fullAddress)
                .build()
                .availableModels()
        return response.content()
    }

    override fun createStreamingChatModel(): StreamingChatModel {
        TODO("Not yet implemented")
    }

    override fun deleteChat(memoryId: UUID): TokenStream {
        TODO("Not yet implemented")
    }

    override fun createChatModels(config: LlmConfig) {
        if (config.modelName == null) {
            val models = getModels()
            models.forEach {
                val chatModel =
                    OllamaChatModel
                        .builder()
                        .baseUrl(config.address)
                        .temperature(config.temperature)
                        .logRequests(config.logRequests)
                        .logResponses(config.logResponse)
                        .modelName(config.modelName)
                        .build()

                chatModels[it.model] = chatModel
            }
        } else {
            chatModels[config.modelName] = OllamaChatModel
                .builder()
                .baseUrl(config.address)
                .temperature(config.temperature)
                .logRequests(config.logRequests)
                .logResponses(config.logResponse)
                .modelName(config.modelName)
                .build()
        }
    }
}
