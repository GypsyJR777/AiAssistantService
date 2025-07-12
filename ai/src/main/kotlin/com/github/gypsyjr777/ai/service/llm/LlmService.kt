package com.github.gypsyjr777.ai.service.llm

import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaModel

interface LlmService {
    fun getModels(): List<OllamaModel>
    fun createChat()
    fun chat()
    fun createChatModels()
    fun getChatModel(model: String): OllamaChatModel?
    fun getChatModels(): MutableMap<String, OllamaChatModel>
}