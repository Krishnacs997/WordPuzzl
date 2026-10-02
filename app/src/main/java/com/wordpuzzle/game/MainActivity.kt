package com.wordpuzzle.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.wordpuzzle.game.navigation.AppNavHost
import com.wordpuzzle.game.ui.theme.Navy
import com.wordpuzzle.game.ui.theme.WordPuzzleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordPuzzleTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()

                        .safeDrawingPadding(),
                    color = Navy
                ) {
                    AppNavHost(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
