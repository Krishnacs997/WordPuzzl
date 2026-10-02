package com.wordpuzzle.game.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wordpuzzle.game.ui.theme.Cream
import com.wordpuzzle.game.ui.theme.Gold
import com.wordpuzzle.game.ui.theme.Navy
import com.wordpuzzle.game.ui.theme.NavyDeep
import com.wordpuzzle.game.ui.theme.Teal
import com.wordpuzzle.game.ui.theme.TealBright

@Composable
fun HomeScreen(
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700),
        label = "homeFade"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(NavyDeep, Navy, Teal.copy(alpha = 0.35f))
                )
            )
            .alpha(alpha)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "WORD PUZZLE",
                style = MaterialTheme.typography.displayLarge,
                color = Cream,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Swipe letters. Fill the crossword. Chase every bonus word.",
                style = MaterialTheme.typography.bodyLarge,
                color = Cream.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(36.dp))
            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealBright,
                    contentColor = NavyDeep
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Text("Play", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "12 handcrafted levels",
                color = Gold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
