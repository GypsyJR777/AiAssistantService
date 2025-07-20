package com.github.gypsyjr777.ai.service

import com.github.gypsyjr777.ai.assistant.Assistant
import com.github.gypsyjr777.ai.assistant.PerplLikeSearchAssistant
import com.github.gypsyjr777.ai.tool.search.DDGSearchService
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
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
    var chatMemoryProvider: ChatMemoryProvider =
        ChatMemoryProvider { memoryId: Any? ->
            MessageWindowChatMemory
                .builder()
                .id(memoryId)
                .maxMessages(10)
                .build()
        }
    private val ddgSearchService: WebSearchEngine = DDGSearchService()
    private val assistants: MutableMap<String, Assistant> = mutableMapOf()

    fun chat(
        memoryId: UUID,
        userMessage: String,
        model: String,
        partialAction: (test: String?) -> Unit,
        completeAction: (text: String?) -> Unit,
        failAction: (text: Throwable?) -> Unit
    ): String? {
        CoroutineScope(Dispatchers.IO).launch {
            val googleSearch =
                GoogleCustomWebSearchEngine
                    .builder()
                    .apiKey("")
                    .csi("")
                    .maxRetries(2)
                    .build()
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
                    .tools(WebSearchTool.from(googleSearch))
                    .chatMemoryProvider(chatMemoryProvider)
                    .build()

            assistants[model] = assistant
            val tokenStream: TokenStream = assistant.chat(memoryId, userMessage)

            val futureResponse = CompletableFuture<ChatResponse>()

            tokenStream
                .onPartialResponse { s: String -> partialAction(s) }
                .onCompleteResponse { value: ChatResponse ->
                    {
                        completeAction(value.aiMessage().text())
                        futureResponse.complete(value)
                    }
                }
                .onError { ex: Throwable ->
                    {
                        failAction(ex)
                        futureResponse.completeExceptionally(ex)
                    }
                }
                .start()

            val chatResponse = futureResponse.get(3000, TimeUnit.MINUTES)
            println("\n Result is: \n$chatResponse")
        }

        return ""
    }
}
