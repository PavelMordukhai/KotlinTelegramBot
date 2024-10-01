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
    val json = Json { ignoreUnknownKeys = true }
    val telegramBotService = TelegramBotService(botToken, json)
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, trainers, telegramBotService) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update,
    trainers: HashMap<Long, LearnWordsTrainer>,
    telegramBotService: TelegramBotService
) {
    val helloResponse = "Hello"
    val commandsForSendMenu = listOf("menu", "/start")

    val message = update.message?.text
    var chatId = update.message?.chat?.id
        ?: update.callBackQuery?.message?.chat?.id ?: return
    val data = update.callBackQuery?.data
    val isAnswerPrefixInData = data?.lowercase()?.startsWith(CALLBACK_DATA_ANSWER_PREFIX)

    val trainer = try {
        trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }
    } catch (_: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

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

    if (data?.lowercase() == LEARN_WORDS_CLICKED)
        checkNextQuestionAndSend(trainer, telegramBotService, chatId)

    if(data?.lowercase() == RESET_CLICKED) {
        trainer.resetProgress()
        telegramBotService.sendMessage(chatId, "Прогресс сброшен")
    }

    if (isAnswerPrefixInData == true) {
        val answerIndex = data.lowercase().substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
        val response = when {
            trainer.checkAnswer(answerIndex) -> "Правильно!"
            else -> "\nНеправильно! ${trainer.getQuestion()?.correctAnswer?.original} " +
                    "- ${trainer.getQuestion()?.correctAnswer?.translate}"
        }
        telegramBotService.sendMessage(chatId, response)
        checkNextQuestionAndSend(trainer, telegramBotService, chatId)
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
