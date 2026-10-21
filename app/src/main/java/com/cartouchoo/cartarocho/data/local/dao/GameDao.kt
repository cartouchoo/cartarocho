package com.cartouchoo.cartarocho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cartouchoo.cartarocho.data.local.entity.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM games ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getById(id: Long): GameEntity?

    @Insert
    suspend fun insert(game: GameEntity): Long

    @Query("UPDATE games SET name = :name WHERE id = :id")
    suspend fun updateName(id: Long, name: String?)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteById(id: Long)
}
