package com.cartouchoo.cartarocho.domain

/**
 * Cartes remportées par le preneur, telles que comptées (ou reconnues sur une photo).
 *
 * [totalCards] est le nombre total de cartes du tas, têtes et bouts compris : toutes
 * les cartes qui ne sont ni un bout ni une tête valent un demi-point.
 */
data class CardCount(
    val petit: Boolean = false,
    val vingtEtUn: Boolean = false,
    val excuse: Boolean = false,
    val rois: Int = 0,
    val dames: Int = 0,
    val cavaliers: Int = 0,
    val valets: Int = 0,
    val totalCards: Int = 0,
) {
    val bouts: Int get() = listOf(petit, vingtEtUn, excuse).count { it }

    /** Nombre de cartes qui valent plus d'un demi-point. */
    val honorCards: Int get() = bouts + rois + dames + cavaliers + valets

    /** Le total ne peut pas être inférieur au nombre de bouts et de têtes. */
    val effectiveTotalCards: Int get() = maxOf(totalCards, honorCards)

    val points: Double
        get() {
            val others = effectiveTotalCards - honorCards
            return BOUT_OR_ROI * (bouts + rois) +
                DAME * dames +
                CAVALIER * cavaliers +
                VALET * valets +
                OTHER * others
        }

    companion object {
        const val MAX_PER_HEAD = 4
        const val MAX_CARDS = 78

        private const val BOUT_OR_ROI = 4.5
        private const val DAME = 3.5
        private const val CAVALIER = 2.5
        private const val VALET = 1.5
        private const val OTHER = 0.5
    }
}
