package com.cartouchoo.cartarocho

import android.content.Context
import com.cartouchoo.cartarocho.data.local.AppDatabase
import com.cartouchoo.cartarocho.data.repository.GameRepository
import com.cartouchoo.cartarocho.data.repository.PlayerRepository
import com.cartouchoo.cartarocho.data.repository.RoomGameRepository
import com.cartouchoo.cartarocho.data.repository.RoomPlayerRepository
import com.cartouchoo.cartarocho.data.repository.RoomRoundRepository
import com.cartouchoo.cartarocho.data.repository.RoundRepository

/**
 * Point d'entrée de l'injection manuelle (pas de Hilt pour l'instant).
 * Instancie la base de données et les repositories, en gardant leurs interfaces
 * pour pouvoir brancher une autre source de données (ex. Firebase) plus tard.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase = AppDatabase.getInstance(context)

    val playerRepository: PlayerRepository = RoomPlayerRepository(database.playerDao())

    val gameRepository: GameRepository = RoomGameRepository(
        database = database,
        gameDao = database.gameDao(),
        gamePlayerDao = database.gamePlayerDao(),
    )

    val roundRepository: RoundRepository = RoomRoundRepository(database)
}
