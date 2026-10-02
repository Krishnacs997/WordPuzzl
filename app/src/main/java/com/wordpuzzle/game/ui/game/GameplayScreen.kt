package com.wordpuzzle.game.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordpuzzle.game.data.LevelRepository
import com.wordpuzzle.game.data.ProgressRepository
import com.wordpuzzle.game.ui.theme.Cream
import com.wordpuzzle.game.ui.theme.Gold
import com.wordpuzzle.game.ui.theme.InvalidRed
import com.wordpuzzle.game.ui.theme.Navy
import com.wordpuzzle.game.ui.theme.NavyDeep
import com.wordpuzzle.game.ui.theme.Teal
import com.wordpuzzle.game.ui.theme.TealBright
import kotlinx.coroutines.delay

@Composable
fun GameplayScreen(
    levelId: Int,
    levelRepository: LevelRepository,
    progressRepository: ProgressRepository,
    onExitToLevels: () -> Unit,
    onNextLevel: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GameViewModel = viewModel(
        key = "game_$levelId",
        factory = GameViewModel.Factory(levelId, levelRepository, progressRepository)
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        if (state.isPaused) {
            viewModel.togglePause(false)
        } else if (state.isLevelComplete) {
            onExitToLevels()
        } else {
            viewModel.togglePause(true)
        }
    }

    LaunchedEffect(state.isLevelComplete, state.nextLevelId) {
        if (state.isLevelComplete) {
            delay(1500)
            val next = state.nextLevelId
            if (next != null) {
                onNextLevel(next)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Teal
                )
            }
            state.level == null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Level not found", color = Cream)
                    TextButton(onClick = onExitToLevels) {
                        Text("Back")
                    }
                }
            }
            else -> {
                val level = requireNotNull(state.level)
                Column(modifier = Modifier.fillMaxSize()) {
                    GameTopBar(
                        levelId = level.id,
                        foundCount = state.foundGridWords.size,
                        totalCount = level.gridWords.size,
                        onBack = { viewModel.togglePause(true) },
                        onPause = { viewModel.togglePause(true) }
                    )

                    WordPreviewBanner(
                        preview = state.selectedWordPreview,
                        feedbackKind = state.feedbackKind,
                        feedbackWord = state.feedbackWord
                    )

                    WordGrid(
                        level = level,
                        revealedCells = state.revealedCells,
                        celebrateCells = state.celebrateCells,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp)
                    )

                    if (state.foundExtraWords.isNotEmpty()) {
                        Text(
                            text = "Bonus: ${state.foundExtraWords.joinToString()}",
                            color = Gold.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    LetterWheel(
                        letters = level.letters,
                        selectedIndices = state.selectedIndices,
                        enabled = !state.isPaused && !state.isLevelComplete,
                        onLetterTouched = viewModel::onLetterTouched,
                        onSwipeFinished = viewModel::onSwipeFinished,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )
                }

                if (state.isPaused) {
                    PauseOverlay(
                        onResume = { viewModel.togglePause(false) },
                        onQuit = onExitToLevels
                    )
                }

                AnimatedVisibility(
                    visible = state.isLevelComplete,
                    enter = fadeIn() + scaleIn(initialScale = 0.85f),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    LevelCompleteCard(
                        hasNext = state.nextLevelId != null,
                        onNext = { state.nextLevelId?.let(onNextLevel) },
                        onLevels = onExitToLevels
                    )
                }
            }
        }
    }
}

@Composable
private fun GameTopBar(
    levelId: Int,
    foundCount: Int,
    totalCount: Int,
    onBack: () -> Unit,
    onPause: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Cream
            )
        }
        Text(
            text = "Level $levelId  ·  $foundCount/$totalCount",
            style = MaterialTheme.typography.titleLarge,
            color = Cream
        )
        IconButton(onClick = onPause) {
            Icon(
                imageVector = Icons.Filled.Pause,
                contentDescription = "Pause",
                tint = Cream
            )
        }
    }
}

@Composable
private fun WordPreviewBanner(
    preview: String,
    feedbackKind: FeedbackKind,
    feedbackWord: String
) {
    val shake by animateFloatAsState(
        targetValue = if (feedbackKind == FeedbackKind.INVALID) 1f else 0f,
        animationSpec = tween(400),
        label = "shake"
    )
    val display = when {
        preview.isNotEmpty() -> preview
        feedbackWord.isNotEmpty() -> feedbackWord
        else -> " "
    }
    val color = when (feedbackKind) {
        FeedbackKind.GRID_SUCCESS -> TealBright
        FeedbackKind.EXTRA_SUCCESS -> Gold
        FeedbackKind.ALREADY_FOUND -> Gold.copy(alpha = 0.7f)
        FeedbackKind.INVALID -> InvalidRed
        FeedbackKind.NONE -> Cream
    }
    val label = when (feedbackKind) {
        FeedbackKind.EXTRA_SUCCESS -> "Bonus! $feedbackWord"
        FeedbackKind.ALREADY_FOUND -> "Already found"
        else -> display
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.graphicsLayer {
                translationX = kotlin.math.sin(shake * Math.PI.toFloat() * 6f) * 12f
            }
        )
    }
}

@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onQuit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep.copy(alpha = 0.82f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Navy)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Paused",
                style = MaterialTheme.typography.headlineMedium,
                color = Cream
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = NavyDeep),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Resume")
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onQuit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quit to Levels", color = Cream)
            }
        }
    }
}

@Composable
private fun LevelCompleteCard(
    hasNext: Boolean,
    onNext: () -> Unit,
    onLevels: () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(NavyDeep.copy(alpha = 0.95f))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Level Clear!",
            style = MaterialTheme.typography.headlineMedium,
            color = Gold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (hasNext) "Great job — next puzzle loading…" else "You finished every level!",
            color = Cream,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        if (hasNext) {
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = NavyDeep),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next Level")
            }
            Spacer(Modifier.height(8.dp))
        }
        TextButton(onClick = onLevels) {
            Text("Level Select", color = Cream)
        }
    }
}
