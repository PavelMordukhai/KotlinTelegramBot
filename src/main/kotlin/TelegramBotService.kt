package org.example

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val TELEGRAM_API_DOMAIN = "https://api.telegram.org"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

class TelegramBotService {

    fun getUpdates(botToken: String, updateId: Int?): String {
        val urlGetUpdates = "$TELEGRAM_API_DOMAIN/bot$botToken/getUpdates?offset=$updateId"
        val getUpdatesClient: HttpClient = HttpClient.newBuilder().build()
        val getUpdatesRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val getUpdatesResponse: HttpResponse<String> =
            getUpdatesClient.send(getUpdatesRequest, HttpResponse.BodyHandlers.ofString())

        return getUpdatesResponse.body()
    }

    fun sendMessage(botToken: String, chatId: Long?, text: String): String {
        val encodedText = URLEncoder.encode(
            text, StandardCharsets.UTF_8
        )
        println(encodedText)

        val urlSendMessage = "$TELEGRAM_API_DOMAIN/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedText"
        val sendMessageClient: HttpClient = HttpClient.newBuilder().build()
        val sendMessageRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val sendMessageResponse: HttpResponse<String> =
            sendMessageClient.send(sendMessageRequest, HttpResponse.BodyHandlers.ofString())

        return sendMessageResponse.body()
    }

    fun sendMenu(botToken: String, chatId: Long?): String {
        val urlSendMessage = "$TELEGRAM_API_DOMAIN/bot$botToken/sendMessage"
        val sendMenuBody = """
            {
            	"chat_id": $chatId,
            	"text": "Основное меню",
            	"reply_markup": {
            		"inline_keyboard": [
            			[
            				{
            					"text": "Изучить слова",
            					"callback_data": "$LEARN_WORDS_CLICKED"
            				},
            				{
            					"text": "Статистика",
            					"callback_data": "$STATISTICS_CLICKED"
            				}
            			]
            		]
            	}
            }
        """.trimIndent()

        val sendMessageClient: HttpClient = HttpClient.newBuilder().build()
        val sendMessageRequest: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()

        val sendMessageResponse: HttpResponse<String> =
            sendMessageClient.send(sendMessageRequest, HttpResponse.BodyHandlers.ofString())

        return sendMessageResponse.body()
    }

    fun sendQuestion(botToken: String, chatId: Long?, question: Question): String {
        val urlSendMessage = "$TELEGRAM_API_DOMAIN/bot$botToken/sendMessage"
        val sendAnswersMenuBody = """
            {
            	"chat_id": $chatId,
            	"text": "${question.correctAnswer.original}",
            	"reply_markup": {
            		"inline_keyboard": [
                        ${
                            question.answerOptions.mapIndexed { index, word -> 
                            "[{\"text\": \"${word.translate}\", " +
                                    "\"callback_data\": \"${CALLBACK_DATA_ANSWER_PREFIX + index.toString()}\"" +
                                    "}]" 
                            }.joinToString()
                        }
            		]
            	}
            }
        """.trimIndent()

        val sendMessageClient: HttpClient = HttpClient.newBuilder().build()
        val sendMessageRequest: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendAnswersMenuBody))
            .build()

        val sendMessageResponse: HttpResponse<String> =
            sendMessageClient.send(sendMessageRequest, HttpResponse.BodyHandlers.ofString())

        return sendMessageResponse.body()
    }
}
