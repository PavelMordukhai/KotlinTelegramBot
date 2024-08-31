package org.example.ktb_04

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

    dictionary.forEach { println(it) }
}
