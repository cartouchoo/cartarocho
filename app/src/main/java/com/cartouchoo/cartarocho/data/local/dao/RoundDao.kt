package com.cartouchoo.cartarocho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cartouchoo.cartarocho.data.local.entity.RoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao {

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY round_index")
    fun observeForGame(gameId: Long): Flow<List<RoundEntity>>

    @Query("SELECT * FROM rounds")
    fun observeAll(): Flow<List<RoundEntity>>

    @Query("SELECT * FROM rounds WHERE id = :id")
    suspend fun getById(id: Long): RoundEntity?

    @Query("SELECT COALESCE(MAX(round_index), -1) FROM rounds WHERE gameId = :gameId")
    suspend fun getMaxIndex(gameId: Long): Int

    @Insert
    suspend fun insert(round: RoundEntity): Long

    @Update
    suspend fun update(round: RoundEntity)

    @Query("DELETE FROM rounds WHERE id = :id")
    suspend fun deleteById(id: Long)
}
