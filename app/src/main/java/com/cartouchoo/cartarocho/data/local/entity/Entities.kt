package com.cartouchoo.cartarocho.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cartouchoo.cartarocho.domain.Camp
import com.cartouchoo.cartarocho.domain.ChelemResult
import com.cartouchoo.cartarocho.domain.Contract
import com.cartouchoo.cartarocho.domain.MisereType
import com.cartouchoo.cartarocho.domain.PetitAuBoutCamp
import com.cartouchoo.cartarocho.domain.PoigneeType

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val playerCount: Int,
    val name: String? = null,
)

@Entity(
    tableName = "game_players",
    primaryKeys = ["gameId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("gameId"), Index("playerId")],
)
data class GamePlayerEntity(
    val gameId: Long,
    val playerId: Long,
    val position: Int,
)

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("gameId")],
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    @ColumnInfo(name = "round_index") val index: Int,
    val contract: Contract,
    val takerId: Long,
    val calledId: Long?,
    val bouts: Int,
    val points: Double,
    val petitAuBout: PetitAuBoutCamp,
    val poignee: PoigneeType,
    val poigneeCamp: Camp,
    val chelem: ChelemResult,
)

@Entity(
    tableName = "round_miseres",
    foreignKeys = [
        ForeignKey(
            entity = RoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("roundId"), Index("playerId")],
)
data class RoundMisereEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roundId: Long,
    val playerId: Long,
    val type: MisereType,
)

@Entity(
    tableName = "round_scores",
    primaryKeys = ["roundId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = RoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("playerId")],
)
data class RoundScoreEntity(
    val roundId: Long,
    val playerId: Long,
    val score: Double,
)
