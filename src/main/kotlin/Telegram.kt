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

    val telegramBotService = TelegramBotService()

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        val message = "Невозможно загрузить словарь"
        println(message)
        telegramBotService.sendMessage(json, botToken, null, message)
        return
    }

    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(botToken, lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        println(lastUpdateId)

        val message = firstUpdate.message?.text
        var chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callBackQuery?.message?.chat?.id
        val data = firstUpdate.callBackQuery?.data
        val isAnswerPrefixInData = data?.lowercase()?.startsWith(CALLBACK_DATA_ANSWER_PREFIX)

        if ((message?.lowercase() == helloResponse.lowercase()) && (chatId != null))
            telegramBotService.sendMessage(json, botToken, chatId, helloResponse)

        if ((message?.lowercase() in commandsForSendMenu) && (chatId != null))
            telegramBotService.sendMenu(json, botToken, chatId)

        if ((data?.lowercase() == STATISTICS_CLICKED) && (chatId != null)) {
            val statistics = trainer.getStatistics()
            val statisticsString = "\nВыучено ${statistics.numberOfLearnedWords} " +
                    "из ${statistics.numberOfWords} слов | ${statistics.percentage}%"
            telegramBotService.sendMessage(json, botToken, chatId, statisticsString)
        }

        if ((isAnswerPrefixInData == true) && (question != null)) {
            val answerIndex = data.lowercase().substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            val response = when {
                trainer.checkAnswer(answerIndex) -> "Правильно!"
                else -> "\nНеправильно! ${question.correctAnswer.original} " +
                        "- ${question.correctAnswer.translate}"
            }
            telegramBotService.sendMessage(json, botToken, chatId, response)
            question = checkNextQuestionAndSend(json, trainer, telegramBotService, chatId, botToken)
        }

        if ((data?.lowercase() == LEARN_WORDS_CLICKED) && (chatId != null))
            question = checkNextQuestionAndSend(json, trainer, telegramBotService, chatId, botToken)
    }
}

fun checkNextQuestionAndSend(
    json: Json,
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long?,
    botToken: String,
): Question? {
    val question = trainer.getNextQuestion()
    if (question == null) {
        val response = "Вы выучили все слова в базе"
        telegramBotService.sendMessage(json, botToken, chatId, response)
    } else
        telegramBotService.sendQuestion(json, botToken, chatId, question)
    return question
}
