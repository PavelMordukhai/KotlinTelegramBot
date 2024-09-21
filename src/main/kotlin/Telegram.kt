package org.example

fun main(args: Array<String>) {

    val telegramBotService = TelegramBotService()

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    val botToken = args[0]
    var lastUpdateId: Int? = 0

    val updateIdRegex: Regex = "\"update_id\":(\\d+),".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"chat\":\\{\"id\":(\\d+),".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    val helloResponse = "Hello"
    val commandsForSendMenu = listOf("menu", "/start")

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(botToken, lastUpdateId)
        println(updates)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        lastUpdateId = updateId + 1
        println(lastUpdateId)

        var chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toLong()
        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (text?.lowercase() == helloResponse.lowercase() && chatId != null)
            telegramBotService.sendMessage(botToken, chatId, helloResponse)

        if ((text?.lowercase() in commandsForSendMenu) && chatId != null)
            telegramBotService.sendMenu(botToken, chatId)

        if (data?.lowercase() == STATISTICS_CLICKED && chatId != null)
            telegramBotService.sendMessage(botToken, chatId, "Выучено 10 из 10 слов | 100%")
    }
}
