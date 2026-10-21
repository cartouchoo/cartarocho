package com.cartouchoo.cartarocho.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CardCountTest {

    @Test
    fun wholeDeckIsWorth91Points() {
        val deck = CardCount(
            petit = true,
            vingtEtUn = true,
            excuse = true,
            rois = 4,
            dames = 4,
            cavaliers = 4,
            valets = 4,
            totalCards = 78,
        )
        assertEquals(91.0, deck.points, 0.0)
        assertEquals(3, deck.bouts)
    }

    @Test
    fun lowCardsAreWorthHalfAPoint() {
        assertEquals(5.0, CardCount(totalCards = 10).points, 0.0)
    }

    @Test
    fun honorsAreValuedByRank() {
        val count = CardCount(petit = true, rois = 1, dames = 1, cavaliers = 1, valets = 1, totalCards = 7)
        // 4.5 + 4.5 + 3.5 + 2.5 + 1.5 + 2 × 0.5
        assertEquals(17.5, count.points, 0.0)
    }

    @Test
    fun totalCannotBeLowerThanHonors() {
        val count = CardCount(rois = 2, totalCards = 0)
        assertEquals(2, count.effectiveTotalCards)
        assertEquals(9.0, count.points, 0.0)
    }
}
