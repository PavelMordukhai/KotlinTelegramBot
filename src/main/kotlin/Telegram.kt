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
    var question: Question? = null

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
        val isAnswerPrefixInData = data?.lowercase()?.startsWith(CALLBACK_DATA_ANSWER_PREFIX)

        if ((isAnswerPrefixInData != true) && (text?.lowercase() == helloResponse.lowercase()) && (chatId != null))
            telegramBotService.sendMessage(botToken, chatId, helloResponse)

        if ((text?.lowercase() in commandsForSendMenu) && chatId != null)
            telegramBotService.sendMenu(botToken, chatId)

        if (data?.lowercase() == STATISTICS_CLICKED && chatId != null) {
            val statistics = trainer.getStatistics()
            val statisticsString = "\nВыучено ${statistics.numberOfLearnedWords} " +
                    "из ${statistics.numberOfWords} слов | ${statistics.percentage}%"
            telegramBotService.sendMessage(botToken, chatId, statisticsString)
        }

        if ((isAnswerPrefixInData == true) && (question != null)) {
            val answerIndex = data.lowercase().substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            val response = when {
                trainer.checkAnswer(answerIndex) -> "Правильно!"
                else -> "\nНеправильно! ${question.correctAnswer.original} " +
                        "- ${question.correctAnswer.translate}"
            }
            telegramBotService.sendMessage(botToken, chatId, response)
            question = checkNextQuestionAndSend(trainer, telegramBotService, chatId, botToken)
        }

        if (data?.lowercase() == LEARN_WORDS_CLICKED && chatId != null)
            question = checkNextQuestionAndSend(trainer, telegramBotService, chatId, botToken)
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long?,
    botToken: String,
): Question? {
    val question = trainer.getNextQuestion()
    if (question == null) {
        val response = "Вы выучили все слова в базе"
        telegramBotService.sendMessage(botToken, chatId, response)
    } else
        telegramBotService.sendQuestion(botToken, chatId, question)
    return question
}
