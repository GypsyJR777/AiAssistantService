package com.github.gypsyjr777.bot.tg.command

import com.github.gypsyjr777.ai.service.LLMService
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

fun BotHandling.chatCommand(lLMServices: List<LLMService>) {
    command("/chat") {
        val messageText = message.text
        var message = bot.sendMessage(chatId, "I'm thinking...\n")
        var messageThinkingText = ""

        lLMServices.find { it.hasAssistant("com.github.gypsyjr777.ai.assistant.BasicSearchAssistantopenai/gpt-oss-20b") }
            ?.chat(
                if (idForChats.containsKey(chatId)) idForChats[chatId]!! else UUID.randomUUID()
                    .also { idForChats[chatId] = it },
                messageText!!,
                "com.github.gypsyjr777.ai.assistant.BasicSearchAssistantopenai/gpt-oss-20b",
                { part ->
                    // Don't delete, for debug
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

                            val finalMsg = aiText.substringAfter("</think>").trim()
                            if (finalMsg.length > 4096) {
                                splitMessage(finalMsg).forEach {
                                    bot.sendMessage(
                                        chatId,
                                        it
                                    )
                                }
                            } else {
                                bot.sendMessage(
                                    chatId,
                                    finalMsg
                                )
                            }
                        } else {
                            if (aiText.length > 4096) {
                                splitMessage(aiText).forEach {
                                    bot.sendMessage(
                                        chatId,
                                        it
                                    )
                                }
                            } else {
                                bot.sendMessage(
                                    chatId,
                                    aiText
                                )
                            }
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

private fun splitMessage(message: String): List<String> {
    val messages: MutableList<String> = ArrayList()

    val splitMsg = message.split("\n")
    for (msg in splitMsg) {
        if (messages.isNotEmpty() && messages[messages.size - 1].length + msg.length > 4096) {
            messages.add(msg)
        } else {
            messages[messages.size - 1] = messages[messages.size - 1] + "\n" + msg
        }
    }

    return messages
}