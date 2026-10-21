package com.cartouchoo.cartarocho.ui.stats

import com.cartouchoo.cartarocho.data.model.GamePlayerLink
import com.cartouchoo.cartarocho.data.model.Player
import com.cartouchoo.cartarocho.data.model.RoundWithScores
import com.cartouchoo.cartarocho.domain.Camp
import com.cartouchoo.cartarocho.domain.ChelemResult
import com.cartouchoo.cartarocho.domain.Contract
import com.cartouchoo.cartarocho.domain.PoigneeType
import com.cartouchoo.cartarocho.domain.TarotScoreCalculator

/** Statistiques d'un joueur, toutes parties confondues. */
data class PlayerStats(
    val player: Player,
    val gamesPlayed: Int,
    val wins: Int,
    val totalScore: Double,
    val averageScorePerGame: Double,
    val timesTaker: Int,
    val takerSuccesses: Int,
    val contractCounts: Map<Contract, Int>,
    val chelemCount: Int,
    val poigneeCount: Int,
    val misereCount: Int,
)

/**
 * Calcule les statistiques de chaque joueur à partir des parties auxquelles il a participé
 * et de l'ensemble des donnes jouées. Trié par score cumulé décroissant.
 */
fun computePlayerStats(
    players: List<Player>,
    gamePlayerLinks: List<GamePlayerLink>,
    rounds: List<RoundWithScores>,
): List<PlayerStats> {
    val gamesByPlayer: Map<Long, Set<Long>> = gamePlayerLinks
        .groupBy({ it.playerId }, { it.gameId })
        .mapValues { it.value.toSet() }

    val totalsByGame: Map<Long, Map<Long, Double>> = rounds
        .groupBy { it.gameId }
        .mapValues { (_, gameRounds) ->
            val totals = mutableMapOf<Long, Double>()
            gameRounds.forEach { round ->
                round.scores.forEach { (playerId, score) ->
                    totals[playerId] = (totals[playerId] ?: 0.0) + score
                }
            }
            totals
        }

    val rankingsByGame: Map<Long, Map<Long, Int>> = totalsByGame.mapValues { (_, totals) ->
        totals.entries
            .sortedByDescending { it.value }
            .mapIndexed { index, entry -> entry.key to index + 1 }
            .toMap()
    }

    return players.map { player ->
        val playerGames = gamesByPlayer[player.id].orEmpty()
        val wins = playerGames.count { gameId -> rankingsByGame[gameId]?.get(player.id) == 1 }
        val totalScore = totalsByGame.values.sumOf { it[player.id] ?: 0.0 }
        val takerRounds = rounds.filter { it.draft.takerId == player.id }
        val takerSuccesses = takerRounds.count { round ->
            round.draft.points >= TarotScoreCalculator.pointsARealiser(round.draft.bouts)
        }
        val chelemCount = takerRounds.count {
            it.draft.chelem == ChelemResult.ANNOUNCED_WON || it.draft.chelem == ChelemResult.UNANNOUNCED_WON
        }
        val poigneeCount = rounds.count { round ->
            round.draft.poignee != PoigneeType.NONE && round.scores.containsKey(player.id) && (
                (round.draft.poigneeCamp == Camp.ATTACK && (round.draft.takerId == player.id || round.draft.calledId == player.id)) ||
                    (round.draft.poigneeCamp == Camp.DEFENSE && round.draft.takerId != player.id && round.draft.calledId != player.id)
                )
        }
        val misereCount = rounds.sumOf { round -> round.draft.miseres.count { it.playerId == player.id } }

        PlayerStats(
            player = player,
            gamesPlayed = playerGames.size,
            wins = wins,
            totalScore = totalScore,
            averageScorePerGame = if (playerGames.isEmpty()) 0.0 else totalScore / playerGames.size,
            timesTaker = takerRounds.size,
            takerSuccesses = takerSuccesses,
            contractCounts = takerRounds.groupingBy { it.draft.contract }.eachCount(),
            chelemCount = chelemCount,
            poigneeCount = poigneeCount,
            misereCount = misereCount,
        )
    }.sortedByDescending { it.totalScore }
}
