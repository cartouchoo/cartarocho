package com.cartouchoo.cartarocho.ui.navigation

/** Routes de l'application. Les écrans paramétrés exposent une fabrique de route dédiée. */
object Destinations {
    const val HOME = "home"
    const val PLAYERS = "players"
    const val NEW_GAME = "new_game"
    const val GAMES = "games"
    const val STATS = "stats"
    const val RULES = "rules"

    const val ARG_GAME_ID = "gameId"
    const val ARG_ROUND_ID = "roundId"

    const val GAME_BOARD = "games/{$ARG_GAME_ID}"
    const val NEW_ROUND = "games/{$ARG_GAME_ID}/rounds/new"
    const val EDIT_ROUND = "games/{$ARG_GAME_ID}/rounds/{$ARG_ROUND_ID}/edit"

    fun gameBoard(gameId: Long) = "games/$gameId"
    fun newRound(gameId: Long) = "games/$gameId/rounds/new"
    fun editRound(gameId: Long, roundId: Long) = "games/$gameId/rounds/$roundId/edit"
}
