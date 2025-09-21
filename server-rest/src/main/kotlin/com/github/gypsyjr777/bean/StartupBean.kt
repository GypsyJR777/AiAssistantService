package com.github.gypsyjr777.bean

import io.github.dehuckakpyt.telegrambot.receiver.UpdateReceiver
import io.quarkus.runtime.Startup
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes


@Startup
@ApplicationScoped
class StartupBean(
    private val tgBotReceiver: UpdateReceiver? = null,
) {
    init {
        startBot()
    }

    private fun startBot() {
        tgBotReceiver?.start()
    }

    companion object {
         fun startup(@Observes event: StartupEvent?,tgBotReceiver: UpdateReceiver? = null) {
            StartupBean(tgBotReceiver)
        }
    }

}
