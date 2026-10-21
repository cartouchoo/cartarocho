package com.cartouchoo.cartarocho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cartouchoo.cartarocho.data.local.entity.RoundMisereEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundMisereDao {

    @Query("SELECT * FROM round_miseres WHERE roundId = :roundId")
    suspend fun getForRound(roundId: Long): List<RoundMisereEntity>

    @Query(
        """
        SELECT rm.* FROM round_miseres rm
        INNER JOIN rounds r ON r.id = rm.roundId
        WHERE r.gameId = :gameId
        """
    )
    fun observeForGame(gameId: Long): Flow<List<RoundMisereEntity>>

    @Query("SELECT * FROM round_miseres")
    fun observeAll(): Flow<List<RoundMisereEntity>>

    @Insert
    suspend fun insertAll(miseres: List<RoundMisereEntity>)

    @Query("DELETE FROM round_miseres WHERE roundId = :roundId")
    suspend fun deleteForRound(roundId: Long)
}
