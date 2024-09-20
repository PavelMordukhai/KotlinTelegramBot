package org.example

fun main(args: Array<String>) {

    val telegramBotService = TelegramBotService()

    val botToken = args[0]
    var updateId: Int? = 0
    var chatId: Long? = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(botToken, updateId)
        println(updates)

        val updateIdString: Regex = "\"update_id\":(.+?),".toRegex()
        val updateIdMatchResult: MatchResult? = updateIdString.find(updates)
        val updateIdGroups = updateIdMatchResult?.groups
        updateId = updateIdGroups?.get(1)?.value?.toInt()?.plus(1)
        println(updateId)

        val chatIdString: Regex = "\"id\":(.+?),".toRegex()
        val chatIdMatchResult: MatchResult? = chatIdString.find(updates)
        val chatIdGroups = chatIdMatchResult?.groups
        chatId = chatIdGroups?.get(1)?.value?.toLong()

        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val messageTextMatchResult: MatchResult? = messageTextRegex.find(updates)
        val messageTextGroups = messageTextMatchResult?.groups
        val text = messageTextGroups?.get(1)?.value

        val helloResponse = "Hello"
        if (text?.lowercase() == helloResponse.lowercase())
            telegramBotService.sendMessage(botToken, chatId, helloResponse)
    }
}
