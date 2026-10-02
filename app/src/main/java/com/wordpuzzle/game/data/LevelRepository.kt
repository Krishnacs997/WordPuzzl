package com.wordpuzzle.game.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class LevelRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Volatile
    private var cached: List<Level>? = null

    suspend fun getLevels(): List<Level> = withContext(Dispatchers.IO) {
        cached ?: loadLevels().also { cached = it }
    }

    suspend fun getLevel(id: Int): Level? = getLevels().find { it.id == id }

    private fun loadLevels(): List<Level> {
        val text = context.assets.open(LEVELS_ASSET).bufferedReader().use { it.readText() }
        return json.decodeFromString<LevelsFile>(text).levels.map { it.toDomain() }
    }

    companion object {
        private const val LEVELS_ASSET = "levels.json"
    }
}
