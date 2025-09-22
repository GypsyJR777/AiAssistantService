package com.github.gypsyjr777.ai.service

import com.github.gypsyjr777.ai.assistant.Assistant
import com.github.gypsyjr777.ai.config.LlmConfig
import com.github.gypsyjr777.ai.config.Platform
import com.github.gypsyjr777.ai.tool.CustomTool
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import dev.langchain4j.service.AiServices
import java.time.Duration

class AssistantOpenAIService : LLMService() {
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
        assistantClass: Class<*>
    ) {
        if (llmConfig.platform == Platform.LMSTUDIO ||
            llmConfig.platform == Platform.OPENAI ||
            llmConfig.platform == Platform.PREPLEXITY) {

            val model = OpenAiStreamingChatModel.builder()
                .baseUrl(llmConfig.address)
                .modelName(llmConfig.modelName)
                .apiKey(llmConfig.apiKey)
                .strictTools(true)
                .parallelToolCalls(true)
                .strictJsonSchema(true)
                .returnThinking(true)
                .temperature(llmConfig.temperature)
                .logRequests(llmConfig.logRequests)
                .logResponses(llmConfig.logResponse)
                .timeout(Duration.ofSeconds(llmConfig.timeout))
                .build()

            val assistant: Assistant =
                AiServices
                    .builder(assistantClass)
                    .streamingChatModel(model)
                    .chatMemoryProvider(chatMemoryProvider)
                    .tools(tools)
                    .build() as Assistant

            assistants[name] = assistant
        }
    }
}