package org.example

fun Question.asConsoleString(): String {
    val answerOptions = this.answerOptions
        .mapIndexed { index, word -> " ${index + 1} - ${word.translate}" }
        .joinToString("\n")
    return this.correctAnswer.original + "\n" + answerOptions + "\n 0 - Выход в меню\n"
}

fun main() {

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {

        print(
            """

            Меню:
            1 - Учить слова
            2 - Статистика
            0 - Выход
            Ваш выбор: 
        """.trimIndent()
        )

        when (readln().trim().toIntOrNull()) {
            1 -> {
                while (true) {
                    println()
                    val question = trainer.getNextQuestion()
                    if (question == null) {
                        println("Вы выучили все слова")
                        break
                    }
                    print(question.asConsoleString())

                    var userAnswerInput: Int?
                    do {
                        print("Введите номер ответа либо 0: ")
                        userAnswerInput = readln().trim().toIntOrNull()
                    } while (userAnswerInput !in 0..NUMBER_OF_ANSWER_CHOICES)

                    when {
                        userAnswerInput == 0 -> break
                        trainer.checkAnswer(userAnswerInput?.minus(1)) -> println("\nПравильно!")
                        else -> println("\nНеправильно! ${question.correctAnswer.original} " +
                                    "- ${question.correctAnswer.translate}"
                        )
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println(
                    "\nВыучено ${statistics.numberOfLearnedWords} " +
                            "из ${statistics.numberOfWords} слов | " +
                            "${statistics.percentage}%"
                )
            }

            0 -> break
            else -> println("\nНеверный ввод, введите 1, 2 или 0")
        }
    }
}
