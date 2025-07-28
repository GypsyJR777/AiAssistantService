package com.github.gypsyjr777.ai.service

import com.github.gypsyjr777.ai.assistant.Assistant
import com.github.gypsyjr777.ai.assistant.PerplLikeSearchAssistant
import com.github.gypsyjr777.ai.config.LlmConfig
import com.github.gypsyjr777.ai.config.Platform
import com.github.gypsyjr777.ai.exception.ConfigException
import com.github.gypsyjr777.ai.tool.CustomTool
import com.github.gypsyjr777.ai.tool.search.DDGSearchService
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.ollama.OllamaModel
import dev.langchain4j.model.ollama.OllamaModels
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.model.output.Response
import dev.langchain4j.service.*
import dev.langchain4j.web.search.WebSearchEngine
import dev.langchain4j.web.search.WebSearchTool
import dev.langchain4j.web.search.google.customsearch.GoogleCustomWebSearchEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class AssistantOllamaService {
    private val chatMemoryProvider: ChatMemoryProvider =
        ChatMemoryProvider { memoryId: Any? ->
            MessageWindowChatMemory
                .builder()
                .id(memoryId)
                .maxMessages(10)
                .build()
        }
    private val assistants: MutableMap<String, Assistant> = mutableMapOf()

    fun chat(
        memoryId: UUID,
        userMessage: String,
        assistantName: String,
        partialAction: (text: String?) -> Unit,
        completeAction: (text: String?) -> Unit,
        failAction: (text: Throwable?) -> Unit,
    ): String? {
        CoroutineScope(Dispatchers.IO).launch {
            val assistant =
                AiServices
                    .builder(PerplLikeSearchAssistant::class.java)
                    .streamingChatModel(
                        OllamaStreamingChatModel
                            .builder()
                            .baseUrl("http://localhost:7869")
                            .temperature(0.8)
                            .logRequests(true)
                            .logResponses(true)
                            .modelName("qwen3:8b")
                            .timeout(Duration.ofMinutes(10))
                            .build(),
                    )
                    .chatMemoryProvider(chatMemoryProvider)
                    .build()

            assistants[assistantName] = assistant
            val tokenStream: TokenStream = assistant.chat(memoryId, userMessage)

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

            val chatResponse = futureResponse.get(3000, TimeUnit.MINUTES)
            println("\n Result is: \n$chatResponse")
        }

        return ""
    }

    fun createAssistant(
        name: String,
        tools: List<CustomTool>,
        llmConfig: LlmConfig,
        assistantClass: Class<Any>
    ) {
        if (llmConfig.platform == Platform.OLLAMA) {
            val ollamaModels: List<OllamaModel> = OllamaModels
                .builder()
                .baseUrl(llmConfig.address)
                .build()
                .availableModels()
                .content()

            val model = ollamaModels.find { it -> it.model == llmConfig.modelName }
            if (model == null) {
                throw ConfigException("Ollama model ${llmConfig.modelName} not found")
            }

            val assistant =
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
                    )
                    .chatMemoryProvider(chatMemoryProvider)
                    .tools(tools)
                    .build()

            assistants[name] = assistant
        }
    }
}
