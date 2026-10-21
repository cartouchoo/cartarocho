package com.cartouchoo.cartarocho.data.model

import com.cartouchoo.cartarocho.domain.Camp
import com.cartouchoo.cartarocho.domain.ChelemResult
import com.cartouchoo.cartarocho.domain.Contract
import com.cartouchoo.cartarocho.domain.MisereAnnouncement
import com.cartouchoo.cartarocho.domain.PetitAuBoutCamp
import com.cartouchoo.cartarocho.domain.PoigneeType

/**
 * Modèles exposés par la couche repository, indépendants de Room, afin de pouvoir brancher
 * une autre source de données (par ex. Firebase) sans changer les écrans.
 */
data class Player(
    val id: Long,
    val name: String,
)

data class Game(
    val id: Long,
    val createdAt: Long,
    val playerCount: Int,
    val name: String? = null,
)

/** Saisie d'une donne, indépendante de son identifiant (création ou modification). */
data class RoundDraft(
    val contract: Contract,
    val takerId: Long,
    val calledId: Long?,
    val bouts: Int,
    val points: Double,
    val petitAuBout: PetitAuBoutCamp = PetitAuBoutCamp.NONE,
    val poignee: PoigneeType = PoigneeType.NONE,
    val poigneeCamp: Camp = Camp.ATTACK,
    val chelem: ChelemResult = ChelemResult.NONE,
    val miseres: List<MisereAnnouncement> = emptyList(),
)

/** Une donne sauvegardée, avec sa saisie et les scores calculés par joueur. */
data class RoundWithScores(
    val id: Long,
    val gameId: Long,
    val index: Int,
    val draft: RoundDraft,
    val scores: Map<Long, Double>,
)

/** Association joueur/partie, utilisée pour les statistiques globales. */
data class GamePlayerLink(
    val gameId: Long,
    val playerId: Long,
)
