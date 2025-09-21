package com.github.gypsyjr777.bean

import com.github.gypsyjr777.ai.service.AssistantOllamaService
import com.github.gypsyjr777.bot.tg.command.chatCommand
import com.github.gypsyjr777.bot.tg.command.startCommand
import com.github.gypsyjr777.config.BotConfig
import io.github.dehuckakpyt.telegrambot.config.TelegramBotConfig
import io.github.dehuckakpyt.telegrambot.context.TelegramBotContext
import io.github.dehuckakpyt.telegrambot.ext.config.receiver.handling
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
    fun getTelegramBotContext(botConfig: BotConfig, assistantOllamaService: AssistantOllamaService): TelegramBotContext? {
        if (botConfig.getBot() == null) {
            return null
        }

        botConfig.getBot()?.get("telegram")?.let {
            val config =
                TelegramBotConfig().apply {
                    token = it.token()
                    username = it.name()

                    receiving {
                        handling {
                            startCommand()
                            chatCommand(assistantOllamaService)
                        }
                    }
                }
            return TelegramBotFactory.createTelegramBotContext(config)
        }

        return null
    }
}
