package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_API_DOMAIN = "https://api.telegram.org"

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
        val urlSendMessage = "$TELEGRAM_API_DOMAIN/bot$botToken/sendMessage?chat_id=$chatId&text=$text"
        val sendMessageClient: HttpClient = HttpClient.newBuilder().build()
        val sendMessageRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val sendMessageResponse: HttpResponse<String> =
            sendMessageClient.send(sendMessageRequest, HttpResponse.BodyHandlers.ofString())

        return sendMessageResponse.body()
    }
}
