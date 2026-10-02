package com.wordpuzzle.game.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Navy = Color(0xFF0B1F33)
val NavyDeep = Color(0xFF071523)
val Teal = Color(0xFF1FA8A0)
val TealBright = Color(0xFF2CD4C8)
val Cream = Color(0xFFF5E6C8)
val Gold = Color(0xFFE8B84A)
val SoftWhite = Color(0xFFF7F4EE)
val InvalidRed = Color(0xFFE57373)
val CellEmpty = Color(0xFF16324A)
val CellFilled = Color(0xFF1FA8A0)
val WheelRing = Color(0xFF12405A)

private val ColorScheme = darkColorScheme(
    primary = Teal,
    onPrimary = NavyDeep,
    secondary = Gold,
    onSecondary = NavyDeep,
    background = Navy,
    onBackground = Cream,
    surface = NavyDeep,
    onSurface = Cream,
    surfaceVariant = CellEmpty,
    onSurfaceVariant = SoftWhite,
    error = InvalidRed,
    onError = SoftWhite
)

@Composable
fun WordPuzzleTheme(content: @Composable () -> Unit) {
    // Always use the branded dark puzzle palette regardless of system theme.
    @Suppress("UNUSED_VARIABLE")
    val ignored = isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = ColorScheme,
        typography = MaterialTheme.typography.copy(
            displayLarge = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                color = Cream
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                color = Cream
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Cream
            ),
            bodyLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = SoftWhite
            ),
            labelLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Cream
            )
        ),
        content = content
    )
}
