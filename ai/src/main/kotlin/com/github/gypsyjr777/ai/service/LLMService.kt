package com.github.gypsyjr777.ai.service

import com.github.gypsyjr777.ai.assistant.Assistant
import com.github.gypsyjr777.ai.config.LlmConfig
import com.github.gypsyjr777.ai.exception.AssistantNotFoundException
import com.github.gypsyjr777.ai.tool.CustomTool
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.service.TokenStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.CompletableFuture

abstract class LLMService {
    val assistants: MutableMap<String, Assistant> = hashMapOf()

     fun chat(
        memoryId: UUID,
        userMessage: String,
        assistantName: String,
        partialAction: (String?) -> Unit,
        completeAction: (String?) -> Unit,
        failAction: (Throwable?) -> Unit
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
                .onCompleteResponse { value: ChatResponse? -> futureResponse.complete(value) }
                .onError { ex: Throwable ->
                    {
                        futureResponse.completeExceptionally(ex)
                    }
                }.start()

            futureResponse.whenComplete { result, error ->
                if (error != null) {
                    failAction(error)
                }
                    if (result != null) {
                        completeAction(result.aiMessage().text())
                    }
            }
        }
    }

    abstract fun createAssistant(
        name: String,
        tools: List<CustomTool>,
        llmConfig: LlmConfig,
        assistantClass: Class<*>,
    )
}