package com.wordpuzzle.game.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordpuzzle.game.ui.theme.Cream
import com.wordpuzzle.game.ui.theme.Gold
import com.wordpuzzle.game.ui.theme.NavyDeep
import com.wordpuzzle.game.ui.theme.Teal
import com.wordpuzzle.game.ui.theme.TealBright
import com.wordpuzzle.game.ui.theme.WheelRing
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

@Composable
fun LetterWheel(
    letters: List<Char>,
    selectedIndices: List<Int>,
    enabled: Boolean,
    onLetterTouched: (Int) -> Unit,
    onSwipeFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var fingerPosition by remember { mutableStateOf<Offset?>(null) }
    var centers by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .aspectRatio(1f)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(letters, enabled, centers) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        fingerPosition = down.position
                        nearestLetterIndex(down.position, centers)?.let(onLetterTouched)

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (change.pressed) {
                                val pos = change.position
                                fingerPosition = pos
                                nearestLetterIndex(pos, centers)?.let(onLetterTouched)
                                if (change.positionChange() != Offset.Zero) {
                                    change.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        fingerPosition = null
                        onSwipeFinished()
                    }
                }
        ) {
            val diameter = min(size.width, size.height)
            val center = Offset(size.width / 2f, size.height / 2f)
            val ringRadius = diameter * 0.38f
            val letterRadius = diameter * 0.11f

            drawCircle(
                color = WheelRing,
                radius = ringRadius + letterRadius * 0.85f,
                center = center,
                style = Stroke(width = letterRadius * 0.35f)
            )
            drawCircle(
                color = NavyDeep.copy(alpha = 0.55f),
                radius = ringRadius + letterRadius * 0.55f,
                center = center
            )

            val computedCenters = letters.indices.map { index ->
                val angle = -PI / 2.0 + (2.0 * PI * index / letters.size)
                Offset(
                    x = center.x + (ringRadius * cos(angle)).toFloat(),
                    y = center.y + (ringRadius * sin(angle)).toFloat()
                )
            }
            centers = computedCenters

            if (selectedIndices.isNotEmpty()) {
                val points = selectedIndices.map { computedCenters[it] }
                for (i in 0 until points.lastIndex) {
                    drawLine(
                        color = TealBright.copy(alpha = 0.9f),
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }
                fingerPosition?.let { finger ->
                    drawLine(
                        color = Gold.copy(alpha = 0.85f),
                        start = points.last(),
                        end = finger,
                        strokeWidth = 7f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = Gold, radius = 10f, center = finger)
                }
            }

            letters.forEachIndexed { index, letter ->
                val pos = computedCenters[index]
                val selected = index in selectedIndices
                val scale = if (selected) 1.18f else 1f
                val r = letterRadius * scale

                drawCircle(
                    color = if (selected) TealBright else Teal,
                    radius = r,
                    center = pos
                )
                if (selected) {
                    drawCircle(
                        color = Gold.copy(alpha = 0.45f),
                        radius = r * 1.25f,
                        center = pos,
                        style = Stroke(width = 5f)
                    )
                }

                val layout = textMeasurer.measure(
                    text = letter.toString(),
                    style = TextStyle(
                        color = if (selected) NavyDeep else Cream,
                        fontSize = (r * 0.95f).sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        pos.x - layout.size.width / 2f,
                        pos.y - layout.size.height / 2f
                    )
                )
            }
        }
    }
}

private fun nearestLetterIndex(position: Offset, centers: List<Offset>): Int? {
    if (centers.isEmpty()) return null
    var bestIndex = -1
    var bestDist = Float.MAX_VALUE
    centers.forEachIndexed { index, center ->
        val d = hypot((position.x - center.x).toDouble(), (position.y - center.y).toDouble()).toFloat()
        if (d < bestDist) {
            bestDist = d
            bestIndex = index
        }
    }
    val hitRadius = if (centers.size <= 3) 140f else 110f
    return if (bestIndex >= 0 && bestDist <= hitRadius) bestIndex else null
}
