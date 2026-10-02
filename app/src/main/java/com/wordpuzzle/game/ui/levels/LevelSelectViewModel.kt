package com.wordpuzzle.game.ui.levels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wordpuzzle.game.data.Level
import com.wordpuzzle.game.data.LevelRepository
import com.wordpuzzle.game.data.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LevelSelectUiState(
    val levels: List<Level> = emptyList(),
    val completedIds: Set<Int> = emptySet(),
    val isLoading: Boolean = true
)

class LevelSelectViewModel(
    private val levelRepository: LevelRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelSelectUiState())
    val uiState: StateFlow<LevelSelectUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val levels = levelRepository.getLevels()
            progressRepository.completedLevelIds.collect { completed ->
                _uiState.update {
                    LevelSelectUiState(
                        levels = levels,
                        completedIds = completed,
                        isLoading = false
                    )
                }
            }
        }
    }

    class Factory(
        private val levelRepository: LevelRepository,
        private val progressRepository: ProgressRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LevelSelectViewModel(levelRepository, progressRepository) as T
        }
    }
}
