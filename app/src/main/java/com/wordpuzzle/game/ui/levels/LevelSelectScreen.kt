package com.wordpuzzle.game.ui.levels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordpuzzle.game.data.Level
import com.wordpuzzle.game.data.LevelRepository
import com.wordpuzzle.game.data.ProgressRepository
import com.wordpuzzle.game.ui.theme.Cream
import com.wordpuzzle.game.ui.theme.Gold
import com.wordpuzzle.game.ui.theme.Navy
import com.wordpuzzle.game.ui.theme.NavyDeep
import com.wordpuzzle.game.ui.theme.Teal

@Composable
fun LevelSelectScreen(
    levelRepository: LevelRepository,
    progressRepository: ProgressRepository,
    onBack: () -> Unit,
    onLevelSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: LevelSelectViewModel = viewModel(
        factory = LevelSelectViewModel.Factory(levelRepository, progressRepository)
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Cream
                )
            }
            Text(
                text = "Select Level",
                style = MaterialTheme.typography.headlineMedium,
                color = Cream
            )
        }

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Teal)
                }
            }
            else -> {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { it / 6 }
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.levels, key = { it.id }) { level ->
                            LevelCard(
                                level = level,
                                completed = level.id in state.completedIds,
                                onClick = { onLevelSelected(level.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: Level,
    completed: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (completed) Teal.copy(alpha = 0.35f) else NavyDeep)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (completed) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Gold
            )
            Spacer(Modifier.height(6.dp))
        }
        Text(
            text = level.id.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Cream
        )
        Text(
            text = "${level.letters.size} letters",
            style = MaterialTheme.typography.labelLarge,
            color = Cream.copy(alpha = 0.75f)
        )
    }
}
