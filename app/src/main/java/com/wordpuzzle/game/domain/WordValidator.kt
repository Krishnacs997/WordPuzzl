package com.wordpuzzle.game.domain

import com.wordpuzzle.game.data.Level

sealed class WordValidationResult {
    data class GridWord(val word: String) : WordValidationResult()
    data class ExtraWord(val word: String) : WordValidationResult()
    data class AlreadyFound(val word: String) : WordValidationResult()
    data class Invalid(val word: String) : WordValidationResult()
    data object TooShort : WordValidationResult()
}

object WordValidator {
    fun validate(
        selectedLetters: List<Char>,
        level: Level,
        foundGridWords: Set<String>,
        foundExtraWords: Set<String>
    ): WordValidationResult {
        if (selectedLetters.size < 2) return WordValidationResult.TooShort
        val word = selectedLetters.joinToString("").uppercase()

        return when {
            word in foundGridWords || word in foundExtraWords ->
                WordValidationResult.AlreadyFound(word)
            word in level.gridWords ->
                WordValidationResult.GridWord(word)
            word in level.extraWords ->
                WordValidationResult.ExtraWord(word)
            else ->
                WordValidationResult.Invalid(word)
        }
    }
}

fun cellsForWord(level: Level, word: String): List<Pair<Int, Int>> {
    val placement = level.placements.find { it.word == word } ?: return emptyList()
    return word.indices.map { index ->
        when (placement.direction) {
            com.wordpuzzle.game.data.WordDirection.ACROSS ->
                placement.row to (placement.col + index)
            com.wordpuzzle.game.data.WordDirection.DOWN ->
                (placement.row + index) to placement.col
        }
    }
}
