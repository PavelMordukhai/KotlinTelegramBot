package org.example

import kotlinx.serialization.Serializable
import java.io.File

const val MIN_CORRECT_ANSWERS_COUNT = 3
const val NUMBER_OF_ANSWER_CHOICES = 4

@Serializable
data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

data class Statistics(
    val numberOfWords: Int,
    val numberOfLearnedWords: Int,
    val percentage: Int,
)

data class Question(
    val answerOptions: List<Word>,
    val correctAnswer: Word
)

class LearnWordsTrainer(
    private val fileName: String = "words.txt"
) {

    private var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val numberOfWords = dictionary.size
        val numberOfLearnedWords = dictionary.filter {
            it.correctAnswersCount >= MIN_CORRECT_ANSWERS_COUNT
        }.size
        val percentage = numberOfLearnedWords * 100 / numberOfWords

        return Statistics(numberOfWords, numberOfLearnedWords, percentage)
    }

    fun getNextQuestion(): Question? {
        val unlearnedWords = dictionary.filter {
            it.correctAnswersCount < MIN_CORRECT_ANSWERS_COUNT
        }
        if (unlearnedWords.isEmpty())
            return null

        val answerOptions = if (unlearnedWords.size < NUMBER_OF_ANSWER_CHOICES) {
            val learnedWords = dictionary.filter {
                it.correctAnswersCount >= MIN_CORRECT_ANSWERS_COUNT
            }
            (unlearnedWords + learnedWords.shuffled().take(NUMBER_OF_ANSWER_CHOICES - unlearnedWords.size)).shuffled()
        } else
            unlearnedWords.shuffled().take(NUMBER_OF_ANSWER_CHOICES)

        val correctAnswer = answerOptions.random()

        question = Question(answerOptions, correctAnswer)
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Pair<Boolean, String> {
        val correct = "Правильно!"
        val incorrect = "Неправильно! ${question?.correctAnswer?.original} - ${question?.correctAnswer?.translate}"
        question?.let {
            val correctAnswerIndex = it.answerOptions.indexOf(it.correctAnswer)
            if (correctAnswerIndex == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary()
                return Pair(true, correct)
            } else
                return Pair(false, incorrect)
        }
        return Pair(true, correct)
    }

    fun resetProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }

    private fun loadDictionary(): List<Word> {
        try {
            val wordsFile = File(fileName)
            if (!wordsFile.exists())
                File(fileName).copyTo(wordsFile)

            val dictionary = mutableListOf<Word>()

            wordsFile.readLines().forEach {
                val splitLine = it.split("|")
                dictionary.add(Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0))
            }
            return dictionary
        } catch (_: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл")
        }
    }

    private fun saveDictionary() {
        val wordsFile = File(fileName)
        wordsFile.writeText("")
        dictionary.forEach {
            wordsFile.appendText("${it.original}|${it.translate}|${it.correctAnswersCount}\n")
        }
    }
}
