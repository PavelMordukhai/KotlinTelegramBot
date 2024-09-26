package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

const val TELEGRAM_API_DOMAIN = "https://api.telegram.org"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

class TelegramBotService {

    fun getUpdates(botToken: String, updateId: Long?): String {
        val urlGetUpdates = "$TELEGRAM_API_DOMAIN/bot$botToken/getUpdates?offset=$updateId"
        val getUpdatesClient: HttpClient = HttpClient.newBuilder().build()
        val getUpdatesRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val getUpdatesResponse: HttpResponse<String> =
            getUpdatesClient.send(getUpdatesRequest, HttpResponse.BodyHandlers.ofString())

        return getUpdatesResponse.body()
    }

    fun sendMessage(json: Json, botToken: String, chatId: Long?, text: String): String {
        val urlSendMessage = "$TELEGRAM_API_DOMAIN/bot$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = text,
        )
        val requestBodyString = json.encodeToString(requestBody)

        val sendMessageClient: HttpClient = HttpClient.newBuilder().build()
        val sendMessageRequest: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val sendMessageResponse: HttpResponse<String> =
            sendMessageClient.send(sendMessageRequest, HttpResponse.BodyHandlers.ofString())

        return sendMessageResponse.body()
    }

    fun sendMenu(json: Json, botToken: String, chatId: Long): String {
        val urlSendMessage = "$TELEGRAM_API_DOMAIN/bot$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyBoard(text = "Изучать слова", callbackData = LEARN_WORDS_CLICKED),
                        InlineKeyBoard(text = "Статистика", callbackData = STATISTICS_CLICKED),
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val sendMessageClient: HttpClient = HttpClient.newBuilder().build()
        val sendMessageRequest: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val sendMessageResponse: HttpResponse<String> =
            sendMessageClient.send(sendMessageRequest, HttpResponse.BodyHandlers.ofString())

        return sendMessageResponse.body()
    }

    fun sendQuestion(json: Json, botToken: String, chatId: Long?, question: Question): String {
        val urlSendMessage = "$TELEGRAM_API_DOMAIN/bot$botToken/sendMessage"
        val option = mutableListOf<InlineKeyBoard>()
        val optionsList = mutableListOf(listOf<InlineKeyBoard>())

        for ((index, word) in question.answerOptions.withIndex()) {
            option.add(InlineKeyBoard(text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"))
            optionsList.add(option.toList())
            option.clear()
        }

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(optionsList.toList())
        )
        val requestBodyString = json.encodeToString(requestBody)

        val sendMessageClient: HttpClient = HttpClient.newBuilder().build()
        val sendMessageRequest: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val sendMessageResponse: HttpResponse<String> =
            sendMessageClient.send(sendMessageRequest, HttpResponse.BodyHandlers.ofString())

        return sendMessageResponse.body()
    }
}
