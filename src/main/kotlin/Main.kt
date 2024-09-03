package org.example

import java.io.File

const val MIN_CORRECT_ANSWERS_COUNT = 3
const val NUMBER_OF_ANSWER_CHOICES = 4

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)

fun main() {
    val dictionary: MutableList<Word> = mutableListOf()

    val wordsFile = File("words.txt")
    wordsFile.createNewFile()
    wordsFile.writeText("hello|привет|1\n")
    wordsFile.appendText("dog|собака|1\n")
    wordsFile.appendText("cat|кошка|1\n")
    wordsFile.appendText("green|зелёный|1\n")
    wordsFile.appendText("read|читать|1\n")
    wordsFile.appendText("thank you|спасибо|1")

    val lines: List<String> = wordsFile.readLines()
    for (line in lines) {
        val splitLine = line.split("|")
        val word = Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }

    while (true) {
        println(
            """
                
            Меню:
            1 - Учить слова
            2 - Статистика
            0 - Выход
        """.trimIndent()
        )

        when (readlnOrNull()?.trim()) {
            "1" -> {
                println("\nВведите 0 для выхода в главное меню\n")
                while (true) {
                    val unlearnedWords = dictionary
                        .filter { it.correctAnswersCount < MIN_CORRECT_ANSWERS_COUNT }.shuffled()

                    if (unlearnedWords.isEmpty()) {
                        println("Вы выучили все слова")
                        break
                    }
                    val unlearnedWordsCount = unlearnedWords.size

                    val answers = if (unlearnedWordsCount <= NUMBER_OF_ANSWER_CHOICES)
                        unlearnedWords
                    else
                        unlearnedWords.take(NUMBER_OF_ANSWER_CHOICES)

                    val englishWord = answers.random().original
                    println(englishWord)

                    for (i in answers.indices) {
                        print("${i + 1} - ${answers[i].translate}")
                        if (i < (answers.size - 1)) print(", ") else println()
                    }

                    print("Ваш ответ: ")
                    if (readlnOrNull()?.trim() == "0") break
                    else {
                        println()
                        continue
                    }
                }
            }

            "2" -> {
                val numberOfWords = dictionary.size
                val numberOfLearnedWords = dictionary
                    .filter { it.correctAnswersCount >= MIN_CORRECT_ANSWERS_COUNT }.size
                val percentage = numberOfLearnedWords * 100 / numberOfWords

                println("Выучено $numberOfLearnedWords из $numberOfWords слов | $percentage%")
            }

            "0" -> break
            else -> println("Неверный ввод, введите 1, 2 или 0")
        }
    }

    dictionary.forEach { println(it) }
}
