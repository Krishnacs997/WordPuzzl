package com.wordpuzzle.game.ui.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wordpuzzle.game.data.Level
import com.wordpuzzle.game.data.LevelRepository
import com.wordpuzzle.game.data.ProgressRepository
import com.wordpuzzle.game.domain.WordValidationResult
import com.wordpuzzle.game.domain.WordValidator
import com.wordpuzzle.game.domain.cellsForWord
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FeedbackKind {
    NONE,
    GRID_SUCCESS,
    EXTRA_SUCCESS,
    ALREADY_FOUND,
    INVALID
}

data class GameUiState(
    val level: Level? = null,
    val isLoading: Boolean = true,
    val selectedIndices: List<Int> = emptyList(),
    val selectedWordPreview: String = "",
    val revealedCells: Set<Pair<Int, Int>> = emptySet(),
    val foundGridWords: Set<String> = emptySet(),
    val foundExtraWords: Set<String> = emptySet(),
    val isPaused: Boolean = false,
    val feedbackKind: FeedbackKind = FeedbackKind.NONE,
    val feedbackWord: String = "",
    val isLevelComplete: Boolean = false,
    val celebrateCells: Set<Pair<Int, Int>> = emptySet(),
    val nextLevelId: Int? = null
)

class GameViewModel(
    private val levelId: Int,
    private val levelRepository: LevelRepository,
    private val progressRepository: ProgressRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var feedbackJob: Job? = null

    init {
        viewModelScope.launch {
            val level = levelRepository.getLevel(levelId)
            val allLevels = levelRepository.getLevels()
            val nextId = allLevels
                .filter { it.id > levelId }
                .minByOrNull { it.id }
                ?.id

            val restoredGrid = savedStateHandle.get<ArrayList<String>>(KEY_FOUND_GRID)
                ?.toSet()
                .orEmpty()
            val restoredExtra = savedStateHandle.get<ArrayList<String>>(KEY_FOUND_EXTRA)
                ?.toSet()
                .orEmpty()
            val restoredPaused = savedStateHandle.get<Boolean>(KEY_PAUSED) ?: false

            val revealed = if (level != null) {
                restoredGrid.flatMap { cellsForWord(level, it) }.toSet()
            } else {
                emptySet()
            }

            _uiState.update {
                GameUiState(
                    level = level,
                    isLoading = false,
                    foundGridWords = restoredGrid,
                    foundExtraWords = restoredExtra,
                    revealedCells = revealed,
                    isPaused = restoredPaused,
                    isLevelComplete = level != null && restoredGrid.containsAll(level.gridWords),
                    nextLevelId = nextId
                )
            }
        }
    }

    fun onLetterTouched(index: Int) {
        val state = _uiState.value
        if (state.isPaused || state.isLevelComplete || state.level == null) return
        val selected = state.selectedIndices
        when {
            selected.isEmpty() -> {
                _uiState.update {
                    it.copy(
                        selectedIndices = listOf(index),
                        selectedWordPreview = state.level.letters[index].toString(),
                        feedbackKind = FeedbackKind.NONE,
                        feedbackWord = ""
                    )
                }
            }
            selected.last() == index -> Unit
            index in selected -> {
                // Allow backtracking one letter by returning to previous selection
                val cut = selected.indexOf(index)
                if (cut >= 0) {
                    val trimmed = selected.take(cut + 1)
                    _uiState.update {
                        it.copy(
                            selectedIndices = trimmed,
                            selectedWordPreview = trimmed.joinToString("") { i ->
                                state.level.letters[i].toString()
                            }
                        )
                    }
                }
            }
            else -> {
                val next = selected + index
                _uiState.update {
                    it.copy(
                        selectedIndices = next,
                        selectedWordPreview = next.joinToString("") { i ->
                            state.level.letters[i].toString()
                        }
                    )
                }
            }
        }
    }

    fun onSwipeFinished() {
        val state = _uiState.value
        val level = state.level ?: return
        if (state.isPaused || state.isLevelComplete) {
            clearSelection()
            return
        }
        val result = WordValidator.validate(
            selectedLetters = state.selectedIndices.map { level.letters[it] },
            level = level,
            foundGridWords = state.foundGridWords,
            foundExtraWords = state.foundExtraWords
        )
        when (result) {
            is WordValidationResult.TooShort -> clearSelection()
            is WordValidationResult.GridWord -> handleGridWord(result.word, level)
            is WordValidationResult.ExtraWord -> handleExtraWord(result.word)
            is WordValidationResult.AlreadyFound -> showFeedback(
                FeedbackKind.ALREADY_FOUND,
                result.word
            )
            is WordValidationResult.Invalid -> showFeedback(FeedbackKind.INVALID, result.word)
        }
    }

    fun togglePause(paused: Boolean) {
        _uiState.update { it.copy(isPaused = paused) }
        savedStateHandle[KEY_PAUSED] = paused
    }

    fun clearFeedback() {
        _uiState.update {
            it.copy(
                feedbackKind = FeedbackKind.NONE,
                feedbackWord = "",
                celebrateCells = emptySet()
            )
        }
    }

    private fun handleGridWord(word: String, level: Level) {
        val cells = cellsForWord(level, word)
        val newFound = _uiState.value.foundGridWords + word
        val newRevealed = _uiState.value.revealedCells + cells
        val complete = newFound.containsAll(level.gridWords)

        _uiState.update {
            it.copy(
                foundGridWords = newFound,
                revealedCells = newRevealed,
                celebrateCells = cells.toSet(),
                selectedIndices = emptyList(),
                selectedWordPreview = "",
                feedbackKind = FeedbackKind.GRID_SUCCESS,
                feedbackWord = word,
                isLevelComplete = complete
            )
        }
        persistFound(newFound, _uiState.value.foundExtraWords)

        if (complete) {
            viewModelScope.launch {
                progressRepository.markCompleted(level.id)
            }
        }

        scheduleFeedbackClear(if (complete) 1400 else 900)
    }

    private fun handleExtraWord(word: String) {
        val newExtra = _uiState.value.foundExtraWords + word
        _uiState.update {
            it.copy(
                foundExtraWords = newExtra,
                selectedIndices = emptyList(),
                selectedWordPreview = "",
                feedbackKind = FeedbackKind.EXTRA_SUCCESS,
                feedbackWord = word
            )
        }
        persistFound(_uiState.value.foundGridWords, newExtra)
        scheduleFeedbackClear(1000)
    }

    private fun showFeedback(kind: FeedbackKind, word: String) {
        _uiState.update {
            it.copy(
                feedbackKind = kind,
                feedbackWord = word,
                selectedIndices = emptyList(),
                selectedWordPreview = ""
            )
        }
        scheduleFeedbackClear(700)
    }

    private fun clearSelection() {
        _uiState.update {
            it.copy(selectedIndices = emptyList(), selectedWordPreview = "")
        }
    }

    private fun persistFound(grid: Set<String>, extra: Set<String>) {
        savedStateHandle[KEY_FOUND_GRID] = ArrayList(grid)
        savedStateHandle[KEY_FOUND_EXTRA] = ArrayList(extra)
    }

    private fun scheduleFeedbackClear(delayMs: Long) {
        feedbackJob?.cancel()
        feedbackJob = viewModelScope.launch {
            delay(delayMs)
            clearFeedback()
        }
    }

    class Factory(
        private val levelId: Int,
        private val levelRepository: LevelRepository,
        private val progressRepository: ProgressRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return GameViewModel(
                levelId = levelId,
                levelRepository = levelRepository,
                progressRepository = progressRepository,
                savedStateHandle = extras.createSavedStateHandle()
            ) as T
        }
    }

    companion object {
        private const val KEY_FOUND_GRID = "found_grid"
        private const val KEY_FOUND_EXTRA = "found_extra"
        private const val KEY_PAUSED = "paused"
    }
}
