package com.github.gypsyjr777.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.gypsyjr777.ai.config.AssistantConfig
import com.github.gypsyjr777.ai.exception.ConfigException
import com.github.gypsyjr777.ai.service.AssistantOllamaService
import com.github.gypsyjr777.ai.tool.CustomTool
import com.github.gypsyjr777.ai.tool.ToolsFactory
import io.quarkus.arc.DefaultBean
import jakarta.enterprise.context.Dependent
import jakarta.enterprise.inject.Produces
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.exists

@Dependent
class AssistantConfigBean {
    @Produces
    @DefaultBean
    fun getAssistantOllamaService(
        tools: Map<String, CustomTool>,
        assistantToolConfig: AssistantToolConfig,
        assistantConfig: AssistantConfig,
    ): AssistantOllamaService {
        val assistantOllamaService = AssistantOllamaService()
        assistantToolConfig.assistants().forEach { assistant ->
            assistantOllamaService.createAssistant(
                assistant,
                tools.values.toList(),
                assistantConfig.llm[assistant]!!,
                Class.forName(assistant),
            )
        }

        return assistantOllamaService
    }

    @Produces
    @DefaultBean
    fun getTools(assistantConfig: AssistantConfig): Map<String, CustomTool> = ToolsFactory(assistantConfig).toolsList

    @Produces
    @DefaultBean
    fun getAssistantConfig(assistantToolConfig: AssistantToolConfig): AssistantConfig {
        if (assistantToolConfig.path() != null) {
            return createFromPath(assistantToolConfig.path()!!)
        }

        throw ConfigException("Assistant config not found")
    }

    private fun createFromPath(path: String): AssistantConfig {
        if (Path(path).exists()) {
            val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
            return try {
                Files.newBufferedReader(Path(path)).use {
                    mapper.readValue(it, AssistantConfig::class.java)
                }
            } catch (exception: Exception) {
                throw ConfigException("Unable to read config file: $path", exception)
            }
        }

        throw ConfigException("Path $path does not exists")
    }
}
