package com.cartouchoo.cartarocho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cartouchoo.cartarocho.data.local.entity.RoundScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundScoreDao {

    @Query("SELECT * FROM round_scores WHERE roundId = :roundId")
    suspend fun getForRound(roundId: Long): List<RoundScoreEntity>

    @Query(
        """
        SELECT rs.* FROM round_scores rs
        INNER JOIN rounds r ON r.id = rs.roundId
        WHERE r.gameId = :gameId
        """
    )
    fun observeForGame(gameId: Long): Flow<List<RoundScoreEntity>>

    @Query("SELECT * FROM round_scores")
    fun observeAll(): Flow<List<RoundScoreEntity>>

    @Insert
    suspend fun insertAll(scores: List<RoundScoreEntity>)

    @Query("DELETE FROM round_scores WHERE roundId = :roundId")
    suspend fun deleteForRound(roundId: Long)
}
