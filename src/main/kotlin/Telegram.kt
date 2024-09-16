package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_API_DOMAIN = "https://api.telegram.org"

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId: Int? = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val updateIdString: Regex = "\"update_id\":(.+?),".toRegex()
        val matchResult: MatchResult? = updateIdString.find(updates)
        val groups = matchResult?.groups
        updateId = groups?.get(1)?.value?.toInt()
        println(updateId)
        updateId = updateId?.plus(1)
    }
}

fun getUpdates(botToken: String, updateId: Int?): String {
    val urlGetUpdates = "$TELEGRAM_API_DOMAIN/bot$botToken/getUpdates?offset=$updateId"
    val getUpdatesClient: HttpClient = HttpClient.newBuilder().build()
    val getUpdatesRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val getUpdatesResponse: HttpResponse<String> = getUpdatesClient.send(getUpdatesRequest, HttpResponse.BodyHandlers.ofString())

    return getUpdatesResponse.body()
}
