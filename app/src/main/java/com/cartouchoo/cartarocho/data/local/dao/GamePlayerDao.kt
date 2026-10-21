package com.cartouchoo.cartarocho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cartouchoo.cartarocho.data.local.entity.GamePlayerEntity
import com.cartouchoo.cartarocho.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GamePlayerDao {

    @Query("SELECT * FROM game_players WHERE gameId = :gameId ORDER BY position")
    suspend fun getForGame(gameId: Long): List<GamePlayerEntity>

    @Query(
        """
        SELECT p.* FROM players p
        INNER JOIN game_players gp ON gp.playerId = p.id
        WHERE gp.gameId = :gameId
        ORDER BY gp.position
        """
    )
    fun observePlayersForGame(gameId: Long): Flow<List<PlayerEntity>>

    @Query(
        """
        SELECT p.* FROM players p
        INNER JOIN game_players gp ON gp.playerId = p.id
        WHERE gp.gameId = :gameId
        ORDER BY gp.position
        """
    )
    suspend fun getPlayersForGame(gameId: Long): List<PlayerEntity>

    @Query("SELECT * FROM game_players")
    fun observeAll(): Flow<List<GamePlayerEntity>>

    @Insert
    suspend fun insertAll(gamePlayers: List<GamePlayerEntity>)
}
