package com.github.gypsyjr777.ai.tool.search

import com.github.gypsyjr777.ai.tool.CustomTool
import dev.langchain4j.web.search.WebSearchEngine
import dev.langchain4j.web.search.WebSearchTool

class WebSearchTool(
    searchEngine: WebSearchEngine,
) : WebSearchTool(searchEngine),
    CustomTool
