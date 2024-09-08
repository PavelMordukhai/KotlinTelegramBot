package org.example

const val MIN_CORRECT_ANSWERS_COUNT = 3
const val NUMBER_OF_ANSWER_CHOICES = 4

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun Question.asConsoleString(): String {
    val answerOptions = this.answerOptions
        .mapIndexed { index, word -> " ${index + 1} - ${word.translate}" }
        .joinToString("\n")
    return this.correctAnswer.original + "\n" + answerOptions +
            "\n 0 - Выход в меню" + "\n" + "Введите номер ответа: "
}

fun main() {

    val trainer = LearnWordsTrainer()

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

                    val userAnswerInput = readln().trim().toIntOrNull()
                    if (userAnswerInput == 0) break

                    if (trainer.checkAnswer(userAnswerInput?.minus(1)))
                        println("\nПравильно!")
                    else
                        println("\nНеправильно! " +
                                "${question.correctAnswer.original} " +
                                "- ${question.correctAnswer.translate}")
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
