package com.cartouchoo.cartarocho.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrickPhotoParserTest {

    @Test
    fun readsTrumpNumbersOnceEach() {
        val detection = TrickPhotoParser.parse(listOf("21", "7", "21", "1", "12", "7"))
        assertEquals(setOf(1, 7, 12, 21), detection.trumps)
    }

    @Test
    fun ignoresNumbersThatAreNotTrumps() {
        val detection = TrickPhotoParser.parse(listOf("0", "22", "45", "007", "abc"))
        assertTrue(detection.trumps.isEmpty())
    }

    @Test
    fun countsHeadLettersAndIgnoresPunctuation() {
        val detection = TrickPhotoParser.parse(listOf("R", "r.", "D", "C", "V", "V", "x", "Roi"))
        assertEquals(2, detection.rois)
        assertEquals(1, detection.dames)
        assertEquals(1, detection.cavaliers)
        assertEquals(2, detection.valets)
    }

    @Test
    fun capsHeadsAtFourPerRank() {
        val detection = TrickPhotoParser.parse(List(7) { "R" })
        assertEquals(4, detection.rois)
    }

    @Test
    fun prefillsBoutsAndCardTotal() {
        val count = TrickPhotoParser.parse(listOf("1", "21", "5", "R", "D")).toCardCount()
        assertTrue(count.petit)
        assertTrue(count.vingtEtUn)
        assertFalse(count.excuse)
        assertEquals(2, count.bouts)
        assertEquals(5, count.totalCards)
    }
}
