package com.github.gypsyjr777.controller

import com.github.gypsyjr777.config.OllamaConfig
import com.github.gypsyjr777.service.AssistantOllamaService
import com.github.gypsyjr777.service.llm.LlmService
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.ollama.OllamaChatModel
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import java.util.UUID


@Path("/search")
class OllamaSearchController {

    @Inject
    lateinit var ollamaConfig: OllamaConfig

    @Inject
    lateinit var ollamaService: LlmService

    @Inject
    lateinit var assistantOllama: AssistantOllamaService

    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    fun test(): String {
        val model: ChatModel = OllamaChatModel.builder()
            .baseUrl("http://${ollamaConfig.host()}:${ollamaConfig.port()}")
            .temperature(0.0)
            .logRequests(true)
            .logResponses(true)
            .modelName("tinydolphin")
            .build()

        return model.chat("Provide 3 short bullet points explaining why Java is awesome")
    }

    @GET
    @Path("/models/list")
    @Produces(MediaType.TEXT_PLAIN)
    fun modelList(): List<String> {
        val models: MutableList<String> = mutableListOf()
        ollamaService.getModels().forEach { models.add(it.name) }
        return models
    }

    @GET
    @Path("/models/ddg")
    @Produces(MediaType.APPLICATION_JSON)
    fun searchDdg(message: String): String? {
        return assistantOllama.chat(UUID.randomUUID(), message, "qwen3:8b")
    }
}