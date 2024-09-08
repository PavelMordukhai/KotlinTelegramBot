package org.example

import java.io.File

data class Statistics(
    val numberOfWords: Int,
    val numberOfLearnedWords: Int,
    val percentage: Int,
)

data class Question(
    val answerOptions: List<Word>,
    val correctAnswer: Word
)

class LearnWordsTrainerTest {

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

        val answerOptions = unlearnedWords.shuffled().take(NUMBER_OF_ANSWER_CHOICES)
        val correctAnswer = answerOptions.random()

        question = Question(answerOptions, correctAnswer)
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerIndex = it.answerOptions.indexOf(it.correctAnswer)
            if (correctAnswerIndex == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else false
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        val dictionary = mutableListOf<Word>()
        val wordsFile = File("words.txt")
        wordsFile.readLines().forEach {
            val splitLine = it.split("|")
            dictionary.add(Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0))
        }
        return dictionary
    }

    private fun saveDictionary(words: List<Word>) {
        val wordsFile = File("words.txt")
        wordsFile.writeText("")
        words.forEach {
            wordsFile.appendText("${it.original}|${it.translate}|${it.correctAnswersCount}\n")
        }
    }
}
