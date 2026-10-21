package com.cartouchoo.cartarocho.domain

import kotlin.math.abs
import kotlin.math.floor

/** Moteur de calcul des points d'une donne de tarot (règles FFT + règle maison de la misère). */
object TarotScoreCalculator {

    /** Points à atteindre par le preneur selon le nombre de bouts qu'il possède. */
    fun pointsARealiser(bouts: Int): Int = when (bouts) {
        0 -> 56
        1 -> 51
        2 -> 41
        3 -> 36
        else -> throw IllegalArgumentException("Nombre de bouts invalide : $bouts")
    }

    fun calculate(input: RoundInput): RoundResult {
        validate(input)

        val seuil = pointsARealiser(input.bouts)
        val base = (25.0 + abs(input.pointsRealises - seuil)) * input.contract.coefficient
        val success = input.pointsRealises >= seuil

        val petitBonus = 10.0 * input.contract.coefficient
        val petitContribution = when (input.petitAuBout) {
            PetitAuBoutCamp.ATTACK -> petitBonus
            PetitAuBoutCamp.DEFENSE -> -petitBonus
            PetitAuBoutCamp.NONE -> 0.0
        }

        val poigneeContribution = if (input.poignee == PoigneeType.NONE) {
            0.0
        } else if (success) {
            input.poignee.bonus.toDouble()
        } else {
            -input.poignee.bonus.toDouble()
        }

        val chelemContribution = when (input.chelem) {
            ChelemResult.ANNOUNCED_WON -> 400.0
            ChelemResult.UNANNOUNCED_WON -> 200.0
            ChelemResult.ANNOUNCED_LOST -> -200.0
            ChelemResult.NONE -> 0.0
        }

        val value = (if (success) base else -base) +
            petitContribution +
            poigneeContribution +
            chelemContribution

        val scoresByContract = distributeContractValue(
            value = value,
            playerIds = input.playerIds,
            takerId = input.takerId,
            calledId = input.calledId,
        )
        val scoresByMisere = distributeMiseres(
            miseres = input.miseres,
            playerIds = input.playerIds,
            misereValue = input.misereValue,
        )
        val totalScores = input.playerIds.associateWith { playerId ->
            (scoresByContract[playerId] ?: 0.0) + (scoresByMisere[playerId] ?: 0.0)
        }

        return RoundResult(
            contractSuccess = success,
            pointsARealiser = seuil,
            base = base,
            value = value,
            scoresByContract = scoresByContract,
            scoresByMisere = scoresByMisere,
            totalScores = totalScores,
        )
    }

    private fun distributeContractValue(
        value: Double,
        playerIds: List<Long>,
        takerId: Long,
        calledId: Long?,
    ): Map<Long, Double> {
        val defenders = playerIds.filter { it != takerId && it != calledId }
        return when (playerIds.size) {
            3 -> buildMap {
                put(takerId, 2 * value)
                defenders.forEach { put(it, -value) }
            }
            4 -> buildMap {
                put(takerId, 3 * value)
                defenders.forEach { put(it, -value) }
            }
            5 -> if (calledId == takerId) {
                buildMap {
                    put(takerId, 4 * value)
                    defenders.forEach { put(it, -value) }
                }
            } else {
                buildMap {
                    put(takerId, 2 * value)
                    put(requireNotNull(calledId), value)
                    defenders.forEach { put(it, -value) }
                }
            }
            else -> throw IllegalArgumentException("Nombre de joueurs invalide : ${playerIds.size}")
        }
    }

    private fun distributeMiseres(
        miseres: List<MisereAnnouncement>,
        playerIds: List<Long>,
        misereValue: Double,
    ): Map<Long, Double> {
        val scores = playerIds.associateWith { 0.0 }.toMutableMap()
        val otherCount = playerIds.size - 1
        for (misere in miseres) {
            scores[misere.playerId] = scores.getValue(misere.playerId) + misereValue * otherCount
            for (playerId in playerIds) {
                if (playerId != misere.playerId) {
                    scores[playerId] = scores.getValue(playerId) - misereValue
                }
            }
        }
        return scores
    }

    private fun validate(input: RoundInput) {
        val playerCount = input.playerIds.size
        require(playerCount in 3..5) { "Le nombre de joueurs doit être compris entre 3 et 5" }
        require(input.playerIds.toSet().size == playerCount) {
            "Les identifiants de joueurs doivent être uniques"
        }
        require(input.takerId in input.playerIds) {
            "Le preneur doit faire partie des joueurs de la partie"
        }
        require(input.bouts in 0..3) { "Le nombre de bouts doit être compris entre 0 et 3" }
        require(input.pointsRealises in 0.0..91.0) {
            "Les points réalisés doivent être compris entre 0 et 91"
        }
        require(floor(input.pointsRealises * 2) == input.pointsRealises * 2) {
            "Les points réalisés doivent être un multiple de 0,5"
        }
        if (playerCount == 5) {
            requireNotNull(input.calledId) { "L'appelé est requis pour une partie à 5 joueurs" }
            require(input.calledId in input.playerIds) {
                "L'appelé doit faire partie des joueurs de la partie"
            }
        } else {
            require(input.calledId == null) {
                "L'appelé n'est utilisé que pour les parties à 5 joueurs"
            }
        }
        for (misere in input.miseres) {
            require(misere.playerId in input.playerIds) {
                "Le joueur de la misère doit faire partie des joueurs de la partie"
            }
        }
    }
}
