package com.github.gypsyjr777.ai.service

import com.github.gypsyjr777.ai.assistant.Assistant
import com.github.gypsyjr777.ai.config.LlmConfig
import com.github.gypsyjr777.ai.config.Platform
import com.github.gypsyjr777.ai.exception.AssistantNotFoundException
import com.github.gypsyjr777.ai.exception.ConfigException
import com.github.gypsyjr777.ai.tool.CustomTool
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.ollama.OllamaModel
import dev.langchain4j.model.ollama.OllamaModels
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.TokenStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CompletableFuture

class AssistantOllamaService(
    private val chatMemoryProvider: ChatMemoryProvider =
        ChatMemoryProvider { memoryId: Any? ->
            MessageWindowChatMemory
                .builder()
                .id(memoryId)
                .maxMessages(10)
                .build()
        }
) {

    private val assistants: MutableMap<String, Assistant> = hashMapOf()

    fun chat(
        memoryId: UUID,
        userMessage: String,
        assistantName: String,
        partialAction: (text: String?) -> Unit,
        completeAction: (text: String?) -> Unit,
        failAction: (text: Throwable?) -> Unit,
    ) {
        if (!assistants.containsKey(assistantName)) {
            throw AssistantNotFoundException("$assistantName not found")
        }

        CoroutineScope(Dispatchers.IO).launch {
            val assistant = assistants[assistantName]
            val tokenStream: TokenStream = assistant!!.chat(memoryId, userMessage)
            val futureResponse = CompletableFuture<ChatResponse>()

            tokenStream
                .onPartialResponse { s: String -> partialAction(s) }
                .onCompleteResponse { value: ChatResponse ->
                    {
                        completeAction(value.aiMessage().text())
                        futureResponse.complete(value)
                    }
                }.onError { ex: Throwable ->
                    {
                        failAction(ex)
                        futureResponse.completeExceptionally(ex)
                    }
                }.start()
        }
    }

    fun createAssistant(
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
