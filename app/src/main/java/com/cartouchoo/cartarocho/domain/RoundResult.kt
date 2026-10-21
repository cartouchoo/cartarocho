package com.cartouchoo.cartarocho.domain

/**
 * Résultat du calcul d'une donne.
 * La somme des [totalScores] est toujours égale à 0.
 */
data class RoundResult(
    val contractSuccess: Boolean,
    val pointsARealiser: Int,
    val base: Double,
    val value: Double,
    val scoresByContract: Map<Long, Double>,
    val scoresByMisere: Map<Long, Double>,
    val totalScores: Map<Long, Double>,
)
