package com.github.gypsyjr777.ai.service

import com.github.gypsyjr777.ai.assistant.Assistant
import com.github.gypsyjr777.ai.config.LlmConfig
import com.github.gypsyjr777.ai.config.Platform
import dev.langchain4j.http.client.jdk.JdkHttpClient
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import dev.langchain4j.service.AiServices
import java.net.http.HttpClient
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
        tools: List<Any>,
        llmConfig: LlmConfig,
        assistantClass: Class<*>
    ) {
        if (llmConfig.platform == Platform.LMSTUDIO ||
            llmConfig.platform == Platform.OPENAI ||
            llmConfig.platform == Platform.PREPLEXITY
        ) {
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
                .httpClientBuilder(
                    if (llmConfig.platform == Platform.OPENAI) null
                    else JdkHttpClient.builder()
                        .httpClientBuilder(HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1))
                )
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