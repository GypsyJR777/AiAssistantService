package com.github.gypsyjr777.ai.tool.search

import dev.langchain4j.web.search.WebSearchEngine
import dev.langchain4j.web.search.WebSearchInformationResult
import dev.langchain4j.web.search.WebSearchOrganicResult
import dev.langchain4j.web.search.WebSearchRequest
import dev.langchain4j.web.search.WebSearchResults
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URI
import java.net.URISyntaxException

class DDGSearchService(
    private val httpClient: HttpClient = HttpClient(CIO),
) : WebSearchEngine {
    /**
     * Получение случайного User-Agent из списка.
     */
    fun getRandomUserAgent(): String = COMMON_USER_AGENTS.random()

    /**
     * Выполняет поиск в DuckDuckGo, используя веб-скрейпинг.
     * @param request Запрос на поиск. Может быть null.
     * @return Объект WebSearchResults, содержащий найденные результаты, или null в случае ошибки или отсутствия запроса.
     */
    override fun search(request: WebSearchRequest?): WebSearchResults? {
        if (request == null) {
            System.err.println("Search request is null. Returning null.")
            return null
        }

        val query = request.searchTerms()
        val maxResults = request.maxResults() ?: 5

        println("--- [DDGSearchService] Выполняется поиск: '$query', max_results=$maxResults ---")

        return try {
            runBlocking {
                val response: HttpResponse =
                    httpClient.get(BASE_URL) {
                        parameter("q", query)
                        header(HttpHeaders.UserAgent, getRandomUserAgent())
                    }

                if (!response.status.isSuccess()) {
                    System.err.println("Ошибка при запросе к DuckDuckGo: ${response.status}")
                    return@runBlocking null
                }

                val htmlContent = response.bodyAsText()
                val document = Jsoup.parse(htmlContent)

                val results = mutableListOf<WebSearchOrganicResult>()

                val resultElements = document.select(RESULT_ITEM_SELECTOR)

                for (element: Element in resultElements.take(maxResults)) {
                    val titleLinkElement = element.selectFirst(TITLE_LINK_SELECTOR)
                    val snippetElement = element.selectFirst(SNIPPET_SELECTOR)

                    if (titleLinkElement != null && snippetElement != null) {
                        val urlString = titleLinkElement.attr("href")
                        val title = titleLinkElement.text()
                        val snippet = snippetElement.text()

                        if (urlString.isNotBlank() && title.isNotBlank()) {
                            try {
                                // --- ИЗМЕНЕНО: Создаем объект URI и вызываем прямой конструктор ---
                                val uri = URI(urlString)
                                // В качестве 'content' передаем 'snippet', т.к. у нас нет полного контента страницы
                                val searchResult = WebSearchOrganicResult(title, uri, snippet, snippet)
                                results.add(searchResult)
                            } catch (e: URISyntaxException) {
                                System.err.println("Пропущен некорректный URL: $urlString")
                                // Просто пропускаем этот результат, если URL некорректный
                            }
                        }
                    }
                }

                val searchInformation = WebSearchInformationResult(results.size.toLong())

                WebSearchResults(searchInformation, results)
            }
        } catch (e: Exception) {
            System.err.println("Исключение во время поиска через CustomDuckDuckGo: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val BASE_URL = "https://html.duckduckgo.com/html/"
        private const val RESULT_ITEM_SELECTOR = "div.web-result"
        private const val TITLE_LINK_SELECTOR = "h2.result__title a.result__a"
        private const val SNIPPET_SELECTOR = "a.result__snippet"

        /**
         * Список из 20 разнообразных User-Agent строк для ротации при выполнении HTTP-запросов.
         * Использование разных агентов помогает имитировать запросы от различных пользователей
         * и снижает вероятность блокировки по причине "подозрительной активности робота".
         */
        val COMMON_USER_AGENTS: List<String> =
            listOf(
                // --- Windows ---
                // Chrome on Windows 11
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
                // Edge on Windows 11
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.2535.67",
                // Firefox on Windows 11
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0",
                // Chrome on Windows 10
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                // Firefox on Windows 10
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:125.0) Gecko/20100101 Firefox/125.0",
                // --- macOS ---
                // Chrome on macOS (Apple Silicon)
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
                // Safari on macOS (Apple Silicon)
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15",
                // Firefox on macOS (Intel)
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:126.0) Gecko/20100101 Firefox/126.0",
                // Chrome on macOS (Intel)
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                // Safari on macOS (Intel)
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Safari/605.1.15",
                // --- Linux ---
                // Chrome on Ubuntu
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
                // Firefox on Ubuntu
                "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:126.0) Gecko/20100101 Firefox/126.0",
                // Generic Linux Chrome
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                // --- Mobile: Android ---
                // Chrome on Samsung Android
                "Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.113 Mobile Safari/537.36",
                // Chrome on Google Pixel Android
                "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36",
                // Firefox on Android
                "Mozilla/5.0 (Android 14; Mobile; rv:126.0) Gecko/126.0 Firefox/126.0",
                // Samsung Browser
                "Mozilla/5.0 (Linux; Android 14; SM-A546E) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/25.0 Chrome/121.0.0.0 Mobile Safari/537.36",
                // --- Mobile: iOS (iPhone & iPad) ---
                // Safari on iPhone
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",
                // Safari on iPad
                "Mozilla/5.0 (iPad; CPU OS 17_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",
                // Chrome on iPhone
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/125.0.6422.80 Mobile/15E148 Safari/604.1",
            )
    }
}
