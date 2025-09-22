package com.github.gypsyjr777.bot.tg.command

import com.github.gypsyjr777.ai.service.AssistantOllamaService
import io.github.dehuckakpyt.telegrambot.ext.container.chatId
import io.github.dehuckakpyt.telegrambot.handling.BotHandling
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

fun BotHandling.startCommand() {
    command("/start") {
        bot.sendMessage(chatId, "Hello, my name is ${bot.username} :-)")
    }
}

fun BotHandling.chatCommand(assistantOllamaService: AssistantOllamaService) {
    command("/chat") {
        val messageText = message.text
        var message = bot.sendMessage(chatId, "I'm thinking...\n")
        var messageThinkingText = ""
        assistantOllamaService.chat(
            if (idForChats.containsKey(chatId)) idForChats[chatId]!! else UUID.randomUUID()
                .also { idForChats[chatId] = it },
            messageText!!,
            "com.github.gypsyjr777.ai.assistant.BasicSearchAssistantqwen3:8b",
            { part ->
//                runBlocking {
//                    messageThinkingText += part
//                    val msg = message.text + messageThinkingText
//                    message = if (msg.length > 4096) {
//                        messageThinkingText = ""
//                        bot.sendMessage(chatId, msg)
//                    } else {
//                        try {
//                            bot.editMessageText(
//                                chatId,
//                                message.messageId,
//                                msg,
//                            ).also { messageThinkingText = "" }
//                        } catch (e: Exception) {
//                            print(e)
//                            message
//                        }
//                    }
//                }
            },
            { aiText ->
                CoroutineScope(Dispatchers.IO).launch {
                    if (aiText!!.contains("<think>")) {
                        val thinking = aiText.substringAfter("<think>").substringBefore("</think>").trim()
                        bot.editMessageText(
                            chatId, message.messageId, message.text + "\n" + thinking
                        )
                        bot.sendMessage(
                            chatId,
                            aiText.substringAfter("</think>").trim()
                        )
                    } else {
                        bot.sendMessage(
                            chatId, aiText
                        )
                    }
                }
            }, { fail ->
                CoroutineScope(Dispatchers.IO).launch {
                    bot.sendMessage(chatId, fail!!.stackTrace.toString())
                }
            }
        )
    }
}

val idForChats: MutableMap<Long, UUID> = hashMapOf()