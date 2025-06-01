package com.github.gypsyjr777.controller

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.ollama.OllamaChatModel
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType


@Path("/search")
class OllamaSearchController {

    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    fun test(): String {
        val model: ChatModel = OllamaChatModel.builder()
            .baseUrl("http://localhost:7869")
            .temperature(0.0)
            .logRequests(true)
            .logResponses(true)
            .modelName("tinydolphin")
            .build()

        return model.chat("Provide 3 short bullet points explaining why Java is awesome")
    }


}