package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callBackQuery: CallBackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallBackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyBoard>>,
)

@Serializable
data class InlineKeyBoard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0L
    val json = Json {
        ignoreUnknownKeys = true
    }

    val helloResponse = "Hello"
    val commandsForSendMenu = listOf("menu", "/start")
    var question: Question? = null

    val telegramBotService = TelegramBotService(botToken, json)

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        val message = "Невозможно загрузить словарь"
        println(message)
        return
    }

    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        println(lastUpdateId)

        val message = firstUpdate.message?.text
        var chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callBackQuery?.message?.chat?.id ?: continue
        val data = firstUpdate.callBackQuery?.data
        val isAnswerPrefixInData = data?.lowercase()?.startsWith(CALLBACK_DATA_ANSWER_PREFIX)

        if (message?.lowercase() == helloResponse.lowercase())
            telegramBotService.sendMessage(chatId, helloResponse)

        if (message?.lowercase() in commandsForSendMenu)
            telegramBotService.sendMenu(chatId)

        if (data?.lowercase() == STATISTICS_CLICKED) {
            val statistics = trainer.getStatistics()
            val statisticsString = "\nВыучено ${statistics.numberOfLearnedWords} " +
                    "из ${statistics.numberOfWords} слов | ${statistics.percentage}%"
            telegramBotService.sendMessage(chatId, statisticsString)
        }

        if ((isAnswerPrefixInData == true) && (question != null)) {
            val answerIndex = data.lowercase().substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            val response = when {
                trainer.checkAnswer(answerIndex) -> "Правильно!"
                else -> "\nНеправильно! ${question.correctAnswer.original} " +
                        "- ${question.correctAnswer.translate}"
            }
            telegramBotService.sendMessage(chatId, response)
            question = checkNextQuestionAndSend(trainer, telegramBotService, chatId)
        }

        if (data?.lowercase() == LEARN_WORDS_CLICKED)
            question = checkNextQuestionAndSend(trainer, telegramBotService, chatId)
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long?,
): Question? {
    val question = trainer.getNextQuestion()
    if (question == null) {
        val response = "Вы выучили все слова в базе"
        telegramBotService.sendMessage(chatId, response)
    } else
        telegramBotService.sendQuestion(chatId, question)
    return question
}
