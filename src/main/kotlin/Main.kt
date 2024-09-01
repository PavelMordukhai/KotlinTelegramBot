package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int? = 0,
)

fun main() {
    val dictionary: MutableList<Word> = mutableListOf()

    val wordsFile: File = File("words.txt")
    wordsFile.createNewFile()
    wordsFile.writeText("hello|привет|2\n")
    wordsFile.appendText("dog|собака|1\n")
    wordsFile.appendText("cat|кошка|0\n")
    wordsFile.appendText("thank you|спасибо|")

    val lines: List<String> = wordsFile.readLines()
    for (line in lines) {
        val splitLine = line.split("|")
        val word = Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }

    while (true) {
        println("""
            Меню:
            1 - Учить слова
            2 - Статистика
            0 - Выход
        """.trimIndent())
        when (readlnOrNull()?.trim()) {
            "1" -> println("Выбран пункт 1 - Учить слова")
            "2" -> println("Выбран пункт 2 - Статистика")
            "0" -> break
            else -> println("Неверный ввод, введите 1, 2 или 0")
        }
    }

    dictionary.forEach { println(it) }
}
