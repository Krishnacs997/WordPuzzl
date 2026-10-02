package com.wordpuzzle.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "word_puzzle_progress"
)

class ProgressRepository(private val context: Context) {

    private val completedKey = stringSetPreferencesKey("completed_level_ids")

    val completedLevelIds: Flow<Set<Int>> = context.progressDataStore.data.map { prefs ->
        prefs[completedKey]
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            .orEmpty()
    }

    suspend fun markCompleted(levelId: Int) {
        context.progressDataStore.edit { prefs ->
            val current = prefs[completedKey].orEmpty().toMutableSet()
            current.add(levelId.toString())
            prefs[completedKey] = current
        }
    }
}
