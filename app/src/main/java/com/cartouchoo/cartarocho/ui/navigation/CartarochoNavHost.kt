package com.cartouchoo.cartarocho.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cartouchoo.cartarocho.AppContainer
import com.cartouchoo.cartarocho.ui.game.GameBoardScreen
import com.cartouchoo.cartarocho.ui.game.GamesScreen
import com.cartouchoo.cartarocho.ui.home.HomeScreen
import com.cartouchoo.cartarocho.ui.newgame.NewGameScreen
import com.cartouchoo.cartarocho.ui.newround.NewRoundScreen
import com.cartouchoo.cartarocho.ui.players.PlayersScreen
import com.cartouchoo.cartarocho.ui.rules.RulesScreen
import com.cartouchoo.cartarocho.ui.stats.StatsScreen

@Composable
fun CartarochoNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME,
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(220)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(220)) },
    ) {
        composable(Destinations.HOME) {
            HomeScreen(
                onNavigateToGames = { navController.navigate(Destinations.GAMES) },
                onNavigateToPlayers = { navController.navigate(Destinations.PLAYERS) },
                onNavigateToStats = { navController.navigate(Destinations.STATS) },
                onNavigateToRules = { navController.navigate(Destinations.RULES) },
            )
        }

        composable(Destinations.RULES) {
            RulesScreen(onBack = { navController.popBackStack() })
        }

        composable(Destinations.PLAYERS) {
            PlayersScreen(
                onBack = { navController.popBackStack() },
                playerRepository = container.playerRepository,
            )
        }

        composable(Destinations.NEW_GAME) {
            NewGameScreen(
                onBack = { navController.popBackStack() },
                onGameCreated = { gameId ->
                    navController.navigate(Destinations.gameBoard(gameId)) {
                        popUpTo(Destinations.GAMES)
                    }
                },
                playerRepository = container.playerRepository,
                gameRepository = container.gameRepository,
            )
        }

        composable(Destinations.GAMES) {
            GamesScreen(
                onBack = { navController.popBackStack() },
                onOpenGame = { gameId -> navController.navigate(Destinations.gameBoard(gameId)) },
                onNewGame = { navController.navigate(Destinations.NEW_GAME) },
                gameRepository = container.gameRepository,
            )
        }

        composable(
            route = Destinations.GAME_BOARD,
            arguments = listOf(navArgument(Destinations.ARG_GAME_ID) { type = NavType.LongType }),
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong(Destinations.ARG_GAME_ID) ?: return@composable
            GameBoardScreen(
                gameId = gameId,
                onBack = { navController.popBackStack() },
                onNewRound = { navController.navigate(Destinations.newRound(gameId)) },
                onEditRound = { roundId -> navController.navigate(Destinations.editRound(gameId, roundId)) },
                gameRepository = container.gameRepository,
                roundRepository = container.roundRepository,
            )
        }

        composable(
            route = Destinations.NEW_ROUND,
            arguments = listOf(navArgument(Destinations.ARG_GAME_ID) { type = NavType.LongType }),
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong(Destinations.ARG_GAME_ID) ?: return@composable
            NewRoundScreen(
                gameId = gameId,
                roundId = null,
                onDone = { navController.popBackStack() },
                gameRepository = container.gameRepository,
                roundRepository = container.roundRepository,
            )
        }

        composable(
            route = Destinations.EDIT_ROUND,
            arguments = listOf(
                navArgument(Destinations.ARG_GAME_ID) { type = NavType.LongType },
                navArgument(Destinations.ARG_ROUND_ID) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong(Destinations.ARG_GAME_ID) ?: return@composable
            val roundId = backStackEntry.arguments?.getLong(Destinations.ARG_ROUND_ID) ?: return@composable
            NewRoundScreen(
                gameId = gameId,
                roundId = roundId,
                onDone = { navController.popBackStack() },
                gameRepository = container.gameRepository,
                roundRepository = container.roundRepository,
            )
        }

        composable(Destinations.STATS) {
            StatsScreen(
                onBack = { navController.popBackStack() },
                playerRepository = container.playerRepository,
                gameRepository = container.gameRepository,
                roundRepository = container.roundRepository,
            )
        }
    }
}
