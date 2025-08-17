package com.github.gypsyjr777.bean

import com.github.gypsyjr777.config.BotConfig
import io.github.dehuckakpyt.telegrambot.config.TelegramBotConfig
import io.github.dehuckakpyt.telegrambot.context.TelegramBotContext
import io.github.dehuckakpyt.telegrambot.factory.TelegramBotFactory
import io.github.dehuckakpyt.telegrambot.receiver.UpdateReceiver
import io.quarkus.arc.DefaultBean
import jakarta.enterprise.context.Dependent
import jakarta.enterprise.inject.Produces

@Dependent
class BotBean {
    @Produces
    @DefaultBean
    fun getTgUpdateReceiver(context: TelegramBotContext?): UpdateReceiver? = context?.updateReceiver

    @Produces
    @DefaultBean
    fun getTelegramBotContext(botConfig: BotConfig): TelegramBotContext? {
        if (botConfig.getBot() == null) {
            return null
        }

        botConfig.getBot()?.get("telegram")?.let {
            val config =
                TelegramBotConfig().apply {
                    token = it.token()
                    username = it.name()

                    receiving {
                    }
                }
            return TelegramBotFactory.createTelegramBotContext(config)
        }

        return null
    }
}
