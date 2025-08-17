package com.github.gypsyjr777.bean

import com.github.gypsyjr777.ai.service.AssistantOllamaService
import io.github.dehuckakpyt.telegrambot.receiver.UpdateReceiver
import io.quarkus.runtime.Startup
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes


@Startup
@ApplicationScoped
class StartupBean(
    private val tgBotReceiver: UpdateReceiver? = null,
    private val assistantOllamaService: AssistantOllamaService,
) {
    init {
        startBot()
    }

    private fun startBot() {
        tgBotReceiver?.start()
    }

    companion object {
        public fun startup(@Observes event: StartupEvent?,tgBotReceiver: UpdateReceiver? = null,
                    assistantOllamaService: AssistantOllamaService) {
            StartupBean(tgBotReceiver, assistantOllamaService)
        }
    }

}
