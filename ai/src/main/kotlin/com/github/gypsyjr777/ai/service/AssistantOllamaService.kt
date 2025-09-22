package com.github.gypsyjr777.ai.service

import com.github.gypsyjr777.ai.assistant.Assistant
import com.github.gypsyjr777.ai.config.LlmConfig
import com.github.gypsyjr777.ai.config.Platform
import com.github.gypsyjr777.ai.exception.ConfigException
import com.github.gypsyjr777.ai.tool.CustomTool
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.ollama.OllamaModel
import dev.langchain4j.model.ollama.OllamaModels
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.service.AiServices
import java.time.Duration

class AssistantOllamaService : LLMService() {
    private val chatMemoryProvider: ChatMemoryProvider =
        ChatMemoryProvider { memoryId: Any? ->
            MessageWindowChatMemory
                .builder()
                .id(memoryId)
                .maxMessages(10)
                .build()
        }

    override fun createAssistant(
        name: String,
        tools: List<CustomTool>,
        llmConfig: LlmConfig,
        assistantClass: Class<*>,
    ) {
        if (llmConfig.platform == Platform.OLLAMA) {
            val ollamaModels: List<OllamaModel> =
                OllamaModels
                    .builder()
                    .baseUrl(llmConfig.address)
                    .build()
                    .availableModels()
                    .content()

            val model = ollamaModels.find { it -> it.model == llmConfig.modelName }
            if (model == null) {
                throw ConfigException("Ollama model ${llmConfig.modelName} not found")
            }

            val assistant: Assistant =
                AiServices
                    .builder(assistantClass)
                    .streamingChatModel(
                        OllamaStreamingChatModel
                            .builder()
                            .baseUrl(llmConfig.address)
                            .temperature(llmConfig.temperature)
                            .logRequests(llmConfig.logRequests)
                            .logResponses(llmConfig.logResponse)
                            .modelName(llmConfig.modelName)
                            .timeout(Duration.ofSeconds(llmConfig.timeout))
                            .build(),
                    ).chatMemoryProvider(chatMemoryProvider)
                    .tools(tools)
                    .build() as Assistant

            assistants[name] = assistant
        }
    }
}
