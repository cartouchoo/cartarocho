package com.cartouchoo.cartarocho.data.repository

import com.cartouchoo.cartarocho.data.local.dao.PlayerDao
import com.cartouchoo.cartarocho.data.local.entity.PlayerEntity
import com.cartouchoo.cartarocho.data.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PlayerRepository {
    fun observeAll(): Flow<List<Player>>
    suspend fun getById(id: Long): Player?
    suspend fun add(name: String): Long
    suspend fun rename(id: Long, name: String)
    suspend fun delete(id: Long)
}

class RoomPlayerRepository(
    private val playerDao: PlayerDao,
) : PlayerRepository {

    override fun observeAll(): Flow<List<Player>> =
        playerDao.observeAll().map { players -> players.map { it.toModel() } }

    override suspend fun getById(id: Long): Player? = playerDao.getById(id)?.toModel()

    override suspend fun add(name: String): Long = playerDao.insert(PlayerEntity(name = name.trim()))

    override suspend fun rename(id: Long, name: String) {
        playerDao.update(PlayerEntity(id = id, name = name.trim()))
    }

    override suspend fun delete(id: Long) {
        playerDao.getById(id)?.let { playerDao.delete(it) }
    }
}

private fun PlayerEntity.toModel() = Player(id = id, name = name)
