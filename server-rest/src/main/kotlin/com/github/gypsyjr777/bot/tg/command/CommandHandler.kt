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

fun BotHandling.helpCommand() {
    command("/help") {
        bot.sendMessage(chatId, buildHelpMessage(bot.username))
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

private const val TELEGRAM_MESSAGE_LIMIT = 4096

internal fun splitMessage(
    message: String,
    limit: Int = TELEGRAM_MESSAGE_LIMIT,
): List<String> {
    if (message.isEmpty()) {
        return emptyList()
    }
    val messages: MutableList<String> = ArrayList()
    val current = StringBuilder()

    fun flush() {
        if (current.isNotEmpty()) {
            messages.add(current.toString())
            current.setLength(0)
        }
    }

    message.forEach { char ->
        if (current.length == limit) {
            flush()
        }

        if (char == '\n') {
            if (current.length + 1 > limit) {
                flush()
            }

            current.append(char)
            flush()
        } else {
            if (current.length + 1 > limit) {
                flush()
            }

            current.append(char)
        }
    }

    if (current.isNotEmpty()) {
        messages.add(current.toString())
    }

    return messages
}

internal fun buildHelpMessage(botName: String): String =
    """
        Привет! Я $botName — ИИ-ассистент для глубоких исследований и общения с подключенными LLM.

        Что я умею:
        • выполнять детальные исследования и собирать ответы с учетом контекста чата;
        • делиться промежуточными рассуждениями (think) и разбивать длинные ответы на части;
        • помнить историю переписки в рамках чата, чтобы продолжать диалог.

        Команды:
        • /start — короткое приветствие и проверка, что бот активен.
        • /help — показать это описание возможностей.
        • /chat <запрос> — отправьте команду с вопросом или уточнением, и я подготовлю развёрнутый ответ, сохранив контекст.
    """
        .trimIndent()
