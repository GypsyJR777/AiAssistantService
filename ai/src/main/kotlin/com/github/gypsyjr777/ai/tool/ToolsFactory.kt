package com.github.gypsyjr777.ai.tool

import com.github.gypsyjr777.ai.config.AssistantConfig
import com.github.gypsyjr777.ai.exception.ToolException
import com.github.gypsyjr777.ai.tool.search.WebSearchTool
import dev.langchain4j.web.search.google.customsearch.GoogleCustomWebSearchEngine

class ToolsFactory(
    private val assistantConfig: AssistantConfig,
) {
    val toolsList: MutableMap<String, CustomTool> = createToolsList()

    private fun createToolsList(): MutableMap<String, CustomTool> {
        val tools: MutableMap<String, CustomTool> = hashMapOf()

        if (!assistantConfig.search.isNullOrEmpty()) {
            assistantConfig.search.keys.forEach { name ->
                if (name == ToolType.GOOGLE_SEARCH.toolName) {
                    ToolType.GOOGLE_SEARCH.createTool(assistantConfig)?.let { tools.put(name, it) }
                }
            }
        }

        return tools
    }

    private enum class ToolType(
        val toolName: String,
    ) {
        GOOGLE_SEARCH("google") {
            override fun createTool(config: AssistantConfig): CustomTool? {
                if (config.search!!.containsKey(toolName)) {
                    try {
                        val googleSearch =
                            GoogleCustomWebSearchEngine
                                .builder()
                                .apiKey(config.search[toolName]!!.apiKey)
                                .csi(config.search[toolName]!!.csi)
                                .maxRetries(2)
                                .build()

                        return WebSearchTool(googleSearch)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return null
            }
        }, ;

        //        DDG_SEARCH("ddg", WebSearchTool::class.java),

        abstract fun createTool(config: AssistantConfig): CustomTool?
    }

    fun addCustomTool(
        toolName: String,
        tool: CustomTool,
    ) {
        if (toolsList.containsKey(toolName)) {
            throw ToolException("Tool with name $toolName already exists")
        }

        toolsList[toolName] = tool
    }
}
