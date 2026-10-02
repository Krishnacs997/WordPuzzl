package com.wordpuzzle.game.data

import kotlinx.serialization.Serializable

@Serializable
data class LevelsFile(
    val levels: List<LevelDto>
)

@Serializable
data class LevelDto(
    val id: Int,
    val letters: List<String>,
    val gridWords: List<String>,
    val extraWords: List<String> = emptyList(),
    val layout: LayoutDto
)

@Serializable
data class LayoutDto(
    val rows: Int,
    val cols: Int,
    val placements: List<PlacementDto>
)

@Serializable
data class PlacementDto(
    val word: String,
    val row: Int,
    val col: Int,
    val direction: String
)

enum class WordDirection {
    ACROSS,
    DOWN
}

data class WordPlacement(
    val word: String,
    val row: Int,
    val col: Int,
    val direction: WordDirection
)

data class Level(
    val id: Int,
    val letters: List<Char>,
    val gridWords: Set<String>,
    val extraWords: Set<String>,
    val rows: Int,
    val cols: Int,
    val placements: List<WordPlacement>
) {
    /** Flat map of cell key "r,c" -> expected letter for any grid word cell. */
    val cellLetters: Map<Pair<Int, Int>, Char> by lazy {
        buildMap {
            placements.forEach { placement ->
                placement.word.forEachIndexed { index, char ->
                    val r = when (placement.direction) {
                        WordDirection.ACROSS -> placement.row
                        WordDirection.DOWN -> placement.row + index
                    }
                    val c = when (placement.direction) {
                        WordDirection.ACROSS -> placement.col + index
                        WordDirection.DOWN -> placement.col
                    }
                    put(r to c, char)
                }
            }
        }
    }
}

fun LevelDto.toDomain(): Level {
    return Level(
        id = id,
        letters = letters.map { it.first().uppercaseChar() },
        gridWords = gridWords.map { it.uppercase() }.toSet(),
        extraWords = extraWords.map { it.uppercase() }.toSet(),
        rows = layout.rows,
        cols = layout.cols,
        placements = layout.placements.map { p ->
            WordPlacement(
                word = p.word.uppercase(),
                row = p.row,
                col = p.col,
                direction = WordDirection.valueOf(p.direction.uppercase())
            )
        }
    )
}
