package com.cartouchoo.cartarocho.domain

/** Contrat annoncé par le preneur. */
enum class Contract(val coefficient: Int) {
    PETITE(1),
    GARDE(2),
    GARDE_SANS(4),
    GARDE_CONTRE(6),
}

/** Camp ayant mené le petit (le 1 d'atout) au bout de la donne. */
enum class PetitAuBoutCamp {
    NONE,
    ATTACK,
    DEFENSE,
}

/** Type de poignée annoncée (nombre d'atouts montrés). */
enum class PoigneeType(val bonus: Int) {
    NONE(0),
    SIMPLE(20),
    DOUBLE(30),
    TRIPLE(40),
}

/** Camp ayant annoncé la poignée (information de registre, sans effet sur le calcul). */
enum class Camp {
    ATTACK,
    DEFENSE,
}

/** Résultat du chelem annoncé ou non, du point de vue de l'attaque. */
enum class ChelemResult {
    NONE,
    ANNOUNCED_WON,
    UNANNOUNCED_WON,
    ANNOUNCED_LOST,
}

/** Type de misère annoncée (règle maison). */
enum class MisereType {
    ATOUT,
    TETES,
}
