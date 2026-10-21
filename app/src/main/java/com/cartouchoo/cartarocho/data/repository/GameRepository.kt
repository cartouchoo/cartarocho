package com.cartouchoo.cartarocho.data.repository

import androidx.room.withTransaction
import com.cartouchoo.cartarocho.data.local.AppDatabase
import com.cartouchoo.cartarocho.data.local.dao.GameDao
import com.cartouchoo.cartarocho.data.local.dao.GamePlayerDao
import com.cartouchoo.cartarocho.data.local.entity.GameEntity
import com.cartouchoo.cartarocho.data.local.entity.GamePlayerEntity
import com.cartouchoo.cartarocho.data.local.entity.PlayerEntity
import com.cartouchoo.cartarocho.data.model.Game
import com.cartouchoo.cartarocho.data.model.GamePlayerLink
import com.cartouchoo.cartarocho.data.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface GameRepository {
    fun observeAll(): Flow<List<Game>>
    suspend fun getById(id: Long): Game?
    fun observePlayers(gameId: Long): Flow<List<Player>>
    suspend fun getPlayers(gameId: Long): List<Player>
    fun observeAllGamePlayerLinks(): Flow<List<GamePlayerLink>>
    suspend fun createGame(playerIds: List<Long>): Long
    suspend fun deleteGame(id: Long)
    suspend fun rename(id: Long, name: String?)
}

class RoomGameRepository(
    private val database: AppDatabase,
    private val gameDao: GameDao,
    private val gamePlayerDao: GamePlayerDao,
) : GameRepository {

    override fun observeAll(): Flow<List<Game>> =
        gameDao.observeAll().map { games -> games.map { it.toModel() } }

    override suspend fun getById(id: Long): Game? = gameDao.getById(id)?.toModel()

    override fun observePlayers(gameId: Long): Flow<List<Player>> =
        gamePlayerDao.observePlayersForGame(gameId).map { players -> players.map { it.toModel() } }

    override suspend fun getPlayers(gameId: Long): List<Player> =
        gamePlayerDao.getPlayersForGame(gameId).map { it.toModel() }

    override fun observeAllGamePlayerLinks(): Flow<List<GamePlayerLink>> =
        gamePlayerDao.observeAll().map { links -> links.map { GamePlayerLink(it.gameId, it.playerId) } }

    override suspend fun createGame(playerIds: List<Long>): Long {
        require(playerIds.size in 3..5) { "Une partie compte de 3 à 5 joueurs" }
        return database.withTransaction {
            val gameId = gameDao.insert(
                GameEntity(createdAt = System.currentTimeMillis(), playerCount = playerIds.size)
            )
            gamePlayerDao.insertAll(
                playerIds.mapIndexed { position, playerId ->
                    GamePlayerEntity(gameId = gameId, playerId = playerId, position = position)
                }
            )
            gameId
        }
    }

    override suspend fun deleteGame(id: Long) {
        gameDao.deleteById(id)
    }

    override suspend fun rename(id: Long, name: String?) {
        gameDao.updateName(id, name?.trim()?.takeIf { it.isNotEmpty() })
    }
}

private fun GameEntity.toModel() = Game(id = id, createdAt = createdAt, playerCount = playerCount, name = name)
private fun PlayerEntity.toModel() = Player(id = id, name = name)
