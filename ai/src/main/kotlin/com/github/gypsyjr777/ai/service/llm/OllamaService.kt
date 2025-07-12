package com.github.gypsyjr777.ai.service.llm

import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaModel
import dev.langchain4j.model.ollama.OllamaModels
import dev.langchain4j.model.output.Response

class OllamaService : LlmService {
//    @Inject
//    lateinit var ollamaConfig: OllamaConfig

    private val chatModels: MutableMap<String, OllamaChatModel> = mutableMapOf()
    private val fullAddress: String = "http://localhost:7869"

    init {
        createChatModels()
    }

    override fun getModels(): List<OllamaModel> {
        val response: Response<List<OllamaModel>> = OllamaModels.builder().baseUrl(fullAddress).build().availableModels()
        return response.content()
    }

    override fun createChat() {

        TODO("Not yet implemented")
    }

    override fun chat() {
        TODO("Not yet implemented")
    }

    override fun getChatModel(model: String) = chatModels[model]

    override fun getChatModels() = chatModels

    override fun createChatModels() {
        val models = getModels()
        models.forEach {
            val chatModel = OllamaChatModel.builder()
                .baseUrl(fullAddress)
                .temperature(0.8)
                .logRequests(true)
                .logResponses(true)
                .modelName(it.model)
                .build()

            chatModels[it.model] = chatModel
        }
    }
}