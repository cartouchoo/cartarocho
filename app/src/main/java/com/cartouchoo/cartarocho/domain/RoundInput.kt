package com.cartouchoo.cartarocho.domain

/** Une misère annoncée par un joueur pendant la donne. */
data class MisereAnnouncement(
    val playerId: Long,
    val type: MisereType,
)

/**
 * Données saisies pour une donne, nécessaires au calcul des scores.
 * [playerIds] contient tous les joueurs de la partie, dans un ordre quelconque.
 */
data class RoundInput(
    val playerIds: List<Long>,
    val contract: Contract,
    val takerId: Long,
    val calledId: Long? = null,
    val bouts: Int,
    val pointsRealises: Double,
    val petitAuBout: PetitAuBoutCamp = PetitAuBoutCamp.NONE,
    val poignee: PoigneeType = PoigneeType.NONE,
    val poigneeCamp: Camp = Camp.ATTACK,
    val chelem: ChelemResult = ChelemResult.NONE,
    val miseres: List<MisereAnnouncement> = emptyList(),
    val misereValue: Double = 10.0,
)
