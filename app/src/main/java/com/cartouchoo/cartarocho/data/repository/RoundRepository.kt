package com.cartouchoo.cartarocho.data.repository

import androidx.room.withTransaction
import com.cartouchoo.cartarocho.data.local.AppDatabase
import com.cartouchoo.cartarocho.data.local.dao.GamePlayerDao
import com.cartouchoo.cartarocho.data.local.dao.RoundDao
import com.cartouchoo.cartarocho.data.local.dao.RoundMisereDao
import com.cartouchoo.cartarocho.data.local.dao.RoundScoreDao
import com.cartouchoo.cartarocho.data.local.entity.RoundEntity
import com.cartouchoo.cartarocho.data.local.entity.RoundMisereEntity
import com.cartouchoo.cartarocho.data.local.entity.RoundScoreEntity
import com.cartouchoo.cartarocho.data.model.RoundDraft
import com.cartouchoo.cartarocho.data.model.RoundWithScores
import com.cartouchoo.cartarocho.domain.MisereAnnouncement
import com.cartouchoo.cartarocho.domain.RoundInput
import com.cartouchoo.cartarocho.domain.RoundResult
import com.cartouchoo.cartarocho.domain.TarotScoreCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

interface RoundRepository {
    fun observeRounds(gameId: Long): Flow<List<RoundWithScores>>
    fun observeAllRounds(): Flow<List<RoundWithScores>>
    fun observeTotals(gameId: Long): Flow<Map<Long, Double>>
    suspend fun getRound(roundId: Long): RoundWithScores?
    suspend fun addRound(gameId: Long, draft: RoundDraft, misereValue: Double = DEFAULT_MISERE_VALUE): Long
    suspend fun updateRound(roundId: Long, draft: RoundDraft, misereValue: Double = DEFAULT_MISERE_VALUE)
    suspend fun deleteRound(roundId: Long)
    suspend fun restoreRound(round: RoundWithScores): Long

    companion object {
        const val DEFAULT_MISERE_VALUE = 10.0
    }
}

class RoomRoundRepository(
    private val database: AppDatabase,
    private val roundDao: RoundDao = database.roundDao(),
    private val misereDao: RoundMisereDao = database.roundMisereDao(),
    private val scoreDao: RoundScoreDao = database.roundScoreDao(),
    private val gamePlayerDao: GamePlayerDao = database.gamePlayerDao(),
) : RoundRepository {

    override fun observeRounds(gameId: Long): Flow<List<RoundWithScores>> =
        combine(
            roundDao.observeForGame(gameId),
            misereDao.observeForGame(gameId),
            scoreDao.observeForGame(gameId),
        ) { rounds, miseres, scores ->
            rounds.map { round ->
                round.toModel(
                    miseres = miseres.filter { it.roundId == round.id },
                    scores = scores.filter { it.roundId == round.id },
                )
            }
        }

    override fun observeAllRounds(): Flow<List<RoundWithScores>> =
        combine(
            roundDao.observeAll(),
            misereDao.observeAll(),
            scoreDao.observeAll(),
        ) { rounds, miseres, scores ->
            rounds.map { round ->
                round.toModel(
                    miseres = miseres.filter { it.roundId == round.id },
                    scores = scores.filter { it.roundId == round.id },
                )
            }
        }

    override fun observeTotals(gameId: Long): Flow<Map<Long, Double>> =
        scoreDao.observeForGame(gameId).map { scores ->
            scores.groupBy { it.playerId }.mapValues { (_, v) -> v.sumOf { it.score } }
        }

    override suspend fun getRound(roundId: Long): RoundWithScores? {
        val entity = roundDao.getById(roundId) ?: return null
        return entity.toModel(
            miseres = misereDao.getForRound(roundId),
            scores = scoreDao.getForRound(roundId),
        )
    }

    override suspend fun addRound(gameId: Long, draft: RoundDraft, misereValue: Double): Long {
        val playerIds = gamePlayerDao.getForGame(gameId).map { it.playerId }
        val nextIndex = roundDao.getMaxIndex(gameId) + 1
        val result = TarotScoreCalculator.calculate(draft.toRoundInput(playerIds, misereValue))
        return database.withTransaction {
            val roundId = roundDao.insert(draft.toEntity(gameId = gameId, index = nextIndex))
            persistDetails(roundId, draft.miseres, result)
            roundId
        }
    }

    override suspend fun updateRound(roundId: Long, draft: RoundDraft, misereValue: Double) {
        val existing = requireNotNull(roundDao.getById(roundId)) { "Donne inconnue : $roundId" }
        val playerIds = gamePlayerDao.getForGame(existing.gameId).map { it.playerId }
        val result = TarotScoreCalculator.calculate(draft.toRoundInput(playerIds, misereValue))
        database.withTransaction {
            roundDao.update(draft.toEntity(gameId = existing.gameId, index = existing.index, id = roundId))
            misereDao.deleteForRound(roundId)
            scoreDao.deleteForRound(roundId)
            persistDetails(roundId, draft.miseres, result)
        }
    }

    override suspend fun deleteRound(roundId: Long) {
        roundDao.deleteById(roundId)
    }

    override suspend fun restoreRound(round: RoundWithScores): Long =
        database.withTransaction {
            val roundId = roundDao.insert(round.draft.toEntity(gameId = round.gameId, index = round.index))
            if (round.draft.miseres.isNotEmpty()) {
                misereDao.insertAll(round.draft.miseres.map { it.toEntity(roundId) })
            }
            scoreDao.insertAll(round.scores.map { (playerId, score) -> RoundScoreEntity(roundId, playerId, score) })
            roundId
        }

    private suspend fun persistDetails(roundId: Long, miseres: List<MisereAnnouncement>, result: RoundResult) {
        if (miseres.isNotEmpty()) {
            misereDao.insertAll(miseres.map { it.toEntity(roundId) })
        }
        scoreDao.insertAll(result.totalScores.map { (playerId, score) -> RoundScoreEntity(roundId, playerId, score) })
    }
}

private fun RoundDraft.toRoundInput(playerIds: List<Long>, misereValue: Double) = RoundInput(
    playerIds = playerIds,
    contract = contract,
    takerId = takerId,
    calledId = calledId,
    bouts = bouts,
    pointsRealises = points,
    petitAuBout = petitAuBout,
    poignee = poignee,
    poigneeCamp = poigneeCamp,
    chelem = chelem,
    miseres = miseres,
    misereValue = misereValue,
)

private fun RoundDraft.toEntity(gameId: Long, index: Int, id: Long = 0) = RoundEntity(
    id = id,
    gameId = gameId,
    index = index,
    contract = contract,
    takerId = takerId,
    calledId = calledId,
    bouts = bouts,
    points = points,
    petitAuBout = petitAuBout,
    poignee = poignee,
    poigneeCamp = poigneeCamp,
    chelem = chelem,
)

private fun MisereAnnouncement.toEntity(roundId: Long) = RoundMisereEntity(
    roundId = roundId,
    playerId = playerId,
    type = type,
)

private fun RoundEntity.toModel(
    miseres: List<RoundMisereEntity>,
    scores: List<RoundScoreEntity>,
) = RoundWithScores(
    id = id,
    gameId = gameId,
    index = index,
    draft = RoundDraft(
        contract = contract,
        takerId = takerId,
        calledId = calledId,
        bouts = bouts,
        points = points,
        petitAuBout = petitAuBout,
        poignee = poignee,
        poigneeCamp = poigneeCamp,
        chelem = chelem,
        miseres = miseres.map { MisereAnnouncement(it.playerId, it.type) },
    ),
    scores = scores.associate { it.playerId to it.score },
)
