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

                    val answers = unlearnedWords.take(NUMBER_OF_ANSWER_CHOICES)
                    val englishWord = answers.random().original
                    println(englishWord)
                    println(answers.joinToString { it: Word -> "${answers.indexOf(it) + 1} - ${it.translate}" })
                    print("Ваш ответ: ")

                    if (readlnOrNull()?.trim() == "0") break
                    println()
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
