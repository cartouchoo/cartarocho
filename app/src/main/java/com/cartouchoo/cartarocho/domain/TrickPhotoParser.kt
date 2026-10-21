package com.cartouchoo.cartarocho.domain

/** Ce que la lecture d'une photo des plis a pu repérer. */
data class TrickPhotoDetection(
    /** Numéros d'atouts lus (1 à 21), sans doublon : chaque atout est unique dans le jeu. */
    val trumps: Set<Int>,
    val rois: Int,
    val dames: Int,
    val cavaliers: Int,
    val valets: Int,
) {
    /** Pré-remplissage du comptage à vérifier par l'utilisateur. */
    fun toCardCount(): CardCount {
        val count = CardCount(
            petit = 1 in trumps,
            vingtEtUn = 21 in trumps,
            rois = rois,
            dames = dames,
            cavaliers = cavaliers,
            valets = valets,
        )
        // Seules les cartes lues sont comptées : les cartes basses sans index devront être ajoutées.
        return count.copy(totalCards = trumps.size + rois + dames + cavaliers + valets)
    }
}

/**
 * Repère les cartes de tarot dans le texte lu sur une photo.
 *
 * Les atouts portent leur numéro (1 à 21) dans les coins, les têtes une lettre :
 * R (roi), D (dame), C (cavalier), V (valet). L'Excuse et les cartes basses sans
 * index ne peuvent pas être lues : l'utilisateur les complète à la main.
 */
object TrickPhotoParser {

    fun parse(tokens: List<String>): TrickPhotoDetection {
        val trumps = mutableSetOf<Int>()
        val heads = mutableMapOf('R' to 0, 'D' to 0, 'C' to 0, 'V' to 0)

        for (raw in tokens) {
            val token = raw.trim().trim('.', ',', ':', ';', '-', '_', '\'', '"', '|', '(', ')')
            if (token.isEmpty()) continue

            val number = token.toIntOrNull()
            if (number != null) {
                if (number in 1..21 && token.length <= 2) trumps += number
                continue
            }

            if (token.length == 1) {
                val letter = token[0].uppercaseChar()
                if (letter in heads) heads[letter] = heads.getValue(letter) + 1
            }
        }

        // Une même carte peut montrer sa lettre dans deux coins : on plafonne à 4 par couleur.
        fun capped(letter: Char) = heads.getValue(letter).coerceAtMost(CardCount.MAX_PER_HEAD)

        return TrickPhotoDetection(
            trumps = trumps,
            rois = capped('R'),
            dames = capped('D'),
            cavaliers = capped('C'),
            valets = capped('V'),
        )
    }
}
