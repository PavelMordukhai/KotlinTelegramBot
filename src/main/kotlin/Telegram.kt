package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_API_DOMAIN = "https://api.telegram.org"

fun main(args: Array<String>) {

    val botToken = args[0]
    val urlGetMe = "$TELEGRAM_API_DOMAIN/bot$botToken/getMe"
    val urlGetUpdates = "$TELEGRAM_API_DOMAIN/bot$botToken/getUpdates"

    val getMeClient: HttpClient = HttpClient.newBuilder().build()
    val getUpdatesClient: HttpClient = HttpClient.newBuilder().build()

    val getMeRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetMe)).build()
    val getUpdatesRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

    val getMeResponse: HttpResponse<String> = getMeClient.send(getMeRequest, HttpResponse.BodyHandlers.ofString())
    val getUpdatesResponse: HttpResponse<String> = getUpdatesClient.send(getUpdatesRequest, HttpResponse.BodyHandlers.ofString())

    println(getMeResponse.body())
    println()
    println(getUpdatesResponse.body())
}
