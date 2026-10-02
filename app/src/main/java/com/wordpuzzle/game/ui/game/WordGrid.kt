package com.wordpuzzle.game.ui.game

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wordpuzzle.game.data.Level
import com.wordpuzzle.game.ui.theme.CellEmpty
import com.wordpuzzle.game.ui.theme.CellFilled
import com.wordpuzzle.game.ui.theme.Gold
import com.wordpuzzle.game.ui.theme.NavyDeep

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun WordGrid(
    level: Level,
    revealedCells: Set<Pair<Int, Int>>,
    celebrateCells: Set<Pair<Int, Int>>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val gap = 5.dp
        val cellByWidth = (maxWidth - gap * (level.cols - 1)) / level.cols
        val cellByHeight = (maxHeight - gap * (level.rows - 1)) / level.rows
        val cellSize = minOf(cellByWidth, cellByHeight, 52.dp)
        val gridWidth = cellSize * level.cols + gap * (level.cols - 1)
        val gridHeight = cellSize * level.rows + gap * (level.rows - 1)

        Box(
            modifier = Modifier
                .width(gridWidth)
                .height(gridHeight)
        ) {
            level.cellLetters.forEach { (coord, letter) ->
                val (row, col) = coord
                GridCell(
                    letter = letter,
                    revealed = coord in revealedCells,
                    celebrating = coord in celebrateCells,
                    cellSize = cellSize,
                    modifier = Modifier.offset(
                        x = (cellSize + gap) * col,
                        y = (cellSize + gap) * row
                    )
                )
            }
        }
    }
}

@Composable
private fun GridCell(
    letter: Char,
    revealed: Boolean,
    celebrating: Boolean,
    cellSize: Dp,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(celebrating) {
        if (celebrating) {
            scale.snapTo(0.7f)
            scale.animateTo(1.15f, spring(stiffness = Spring.StiffnessMediumLow))
            scale.animateTo(1f, tween(120))
        }
    }

    val bg = if (revealed) CellFilled else CellEmpty
    val density = LocalDensity.current
    val fontSize = with(density) { (cellSize * 0.42f).toSp() }

    Box(
        modifier = modifier
            .size(cellSize)
            .scale(scale.value)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .then(
                if (celebrating) {
                    Modifier.background(Gold.copy(alpha = 0.28f))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (revealed) {
            Text(
                text = letter.toString(),
                color = NavyDeep,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
        }
    }
}
