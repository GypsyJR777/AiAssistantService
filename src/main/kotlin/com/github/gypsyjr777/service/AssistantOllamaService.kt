package com.github.gypsyjr777.service

import com.github.gypsyjr777.service.llm.LlmService
import com.github.gypsyjr777.service.llm.OllamaService
import com.github.gypsyjr777.service.search.DDGSearchService
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.service.*
import dev.langchain4j.web.search.WebSearchEngine
import dev.langchain4j.web.search.WebSearchTool
import dev.langchain4j.web.search.google.customsearch.GoogleCustomWebSearchEngine
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer


@ApplicationScoped
@Named("assistantOllama")
class AssistantOllamaService {
    interface Assistant {
        @SystemMessage(
            "You are a web search support agent.",
            "If there is any event that has not happened yet",
            "You MUST create a web search request with user query and",
            "use the web search tool to search the web for organic web results.",
            "Include the source link in your final response."
        )
        fun chat(@MemoryId memoryId: UUID, @UserMessage userMessage: String?): TokenStream
    }

    var ollamaService: LlmService = OllamaService()

    var chatMemoryProvider: ChatMemoryProvider = ChatMemoryProvider { memoryId: Any? ->
        MessageWindowChatMemory.builder()
            .id(memoryId)
            .maxMessages(10)
            .build()
    }
    private val ddgSearchService: WebSearchEngine = DDGSearchService()
    private val assistants: MutableMap<String, Assistant> = mutableMapOf()

    init {
        ollamaService.getChatModels().forEach { (model, chatModel) ->
            val assistant = AiServices.builder(Assistant::class.java)
                .chatModel(chatModel)
                .tools(WebSearchTool.from(ddgSearchService))
                .chatMemoryProvider(chatMemoryProvider)
                .build()

            if (assistant != null) {
                assistants[model] = assistant
            }
        }
    }

    fun chat(memoryId: UUID, userMessage: String, model: String): String? {
        CoroutineScope(Dispatchers.IO).launch  {
            val googleSearch = GoogleCustomWebSearchEngine.builder()
                .apiKey("")
                .csi("")
                .maxRetries(2)
                .build()
            val assistant = AiServices.builder(Assistant::class.java)
                .streamingChatModel(
                    OllamaStreamingChatModel.builder()
                        .baseUrl("http://localhost:7869")
                        .temperature(0.8)
                        .logRequests(true)
                        .logResponses(true)
                        .modelName("qwen3:8b")
                        .build()
                )
                .tools(WebSearchTool.from(googleSearch))
                .chatMemoryProvider(chatMemoryProvider)
                .build()
            val tokenStream: TokenStream = assistant.chat(memoryId, userMessage)

            val futureResponse = CompletableFuture<ChatResponse?>()

            tokenStream.onPartialResponse(Consumer { s: String? -> print(s) })
                .onCompleteResponse(Consumer { value: ChatResponse? -> futureResponse.complete(value) })
                .onError(Consumer { ex: Throwable? -> futureResponse.completeExceptionally(ex) })
                .start()

            val chatResponse = futureResponse.get(3000, TimeUnit.MINUTES)
            println("\n" + chatResponse)
        }

        return ""
    }
}