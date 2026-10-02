package com.wordpuzzle.game.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wordpuzzle.game.data.LevelRepository
import com.wordpuzzle.game.data.ProgressRepository
import com.wordpuzzle.game.ui.game.GameplayScreen
import com.wordpuzzle.game.ui.home.HomeScreen
import com.wordpuzzle.game.ui.levels.LevelSelectScreen

object Routes {
    const val HOME = "home"
    const val LEVEL_SELECT = "levelSelect"
    const val GAMEPLAY = "gameplay/{levelId}"

    fun gameplay(levelId: Int) = "gameplay/$levelId"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current.applicationContext
    val levelRepository = remember { LevelRepository(context) }
    val progressRepository = remember { ProgressRepository(context) }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(280)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(280)
            )
        },
        exitTransition = {
            fadeOut(tween(220))
        },
        popEnterTransition = {
            fadeIn(tween(280)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(280)
            )
        },
        popExitTransition = {
            fadeOut(tween(220))
        }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onPlay = {
                    navController.navigate(Routes.LEVEL_SELECT) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.LEVEL_SELECT) {
            LevelSelectScreen(
                levelRepository = levelRepository,
                progressRepository = progressRepository,
                onBack = { navController.popBackStack() },
                onLevelSelected = { levelId ->
                    navController.navigate(Routes.gameplay(levelId)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Routes.GAMEPLAY,
            arguments = listOf(navArgument("levelId") { type = NavType.IntType })
        ) { entry ->
            val levelId = entry.arguments?.getInt("levelId") ?: 1
            GameplayScreen(
                levelId = levelId,
                levelRepository = levelRepository,
                progressRepository = progressRepository,
                onExitToLevels = {
                    navController.popBackStack(Routes.LEVEL_SELECT, inclusive = false)
                },
                onNextLevel = { nextId ->
                    navController.navigate(Routes.gameplay(nextId)) {
                        popUpTo(Routes.LEVEL_SELECT) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
