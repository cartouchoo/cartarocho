package com.cartouchoo.cartarocho.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TarotScoreCalculatorTest {

    private val p1 = 1L
    private val p2 = 2L
    private val p3 = 3L
    private val p4 = 4L
    private val p5 = 5L

    @Test
    fun `points a realiser selon le nombre de bouts`() {
        assertEquals(56, TarotScoreCalculator.pointsARealiser(0))
        assertEquals(51, TarotScoreCalculator.pointsARealiser(1))
        assertEquals(41, TarotScoreCalculator.pointsARealiser(2))
        assertEquals(36, TarotScoreCalculator.pointsARealiser(3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `points a realiser rejette un nombre de bouts invalide`() {
        TarotScoreCalculator.pointsARealiser(4)
    }

    @Test
    fun `exemple de l enonce - 4 joueurs GARDE 2 bouts 45 points`() {
        val result = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
            )
        )

        assertTrue(result.contractSuccess)
        assertEquals(58.0, result.base, 1e-9)
        assertEquals(58.0, result.value, 1e-9)
        assertEquals(174.0, result.totalScores[p1]!!, 1e-9)
        assertEquals(-58.0, result.totalScores[p2]!!, 1e-9)
        assertEquals(-58.0, result.totalScores[p3]!!, 1e-9)
        assertEquals(-58.0, result.totalScores[p4]!!, 1e-9)
        assertEquals(0.0, result.totalScores.values.sum(), 1e-9)
    }

    @Test
    fun `contrat rate - la valeur est negative pour le preneur`() {
        val result = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.PETITE,
                takerId = p1,
                bouts = 1,
                pointsRealises = 40.0,
            )
        )

        assertFalse(result.contractSuccess)
        assertEquals((25.0 + 11.0) * 1, result.base, 1e-9)
        assertEquals(-36.0, result.value, 1e-9)
        assertEquals(-108.0, result.totalScores[p1]!!, 1e-9)
        assertEquals(36.0, result.totalScores[p2]!!, 1e-9)
        assertEquals(0.0, result.totalScores.values.sum(), 1e-9)
    }

    @Test
    fun `repartition a 3 joueurs - preneur plus 2V defenseurs moins V`() {
        val result = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
            )
        )

        assertEquals(2 * result.value, result.totalScores[p1]!!, 1e-9)
        assertEquals(-result.value, result.totalScores[p2]!!, 1e-9)
        assertEquals(-result.value, result.totalScores[p3]!!, 1e-9)
        assertEquals(0.0, result.totalScores.values.sum(), 1e-9)
    }

    @Test
    fun `repartition a 5 joueurs avec un appele distinct`() {
        val result = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4, p5),
                contract = Contract.GARDE,
                takerId = p1,
                calledId = p2,
                bouts = 2,
                pointsRealises = 45.0,
            )
        )

        assertEquals(2 * result.value, result.totalScores[p1]!!, 1e-9)
        assertEquals(result.value, result.totalScores[p2]!!, 1e-9)
        assertEquals(-result.value, result.totalScores[p3]!!, 1e-9)
        assertEquals(-result.value, result.totalScores[p4]!!, 1e-9)
        assertEquals(-result.value, result.totalScores[p5]!!, 1e-9)
        assertEquals(0.0, result.totalScores.values.sum(), 1e-9)
    }

    @Test
    fun `repartition a 5 joueurs quand le preneur s appelle lui-meme`() {
        val result = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4, p5),
                contract = Contract.GARDE,
                takerId = p1,
                calledId = p1,
                bouts = 2,
                pointsRealises = 45.0,
            )
        )

        assertEquals(4 * result.value, result.totalScores[p1]!!, 1e-9)
        assertEquals(-result.value, result.totalScores[p2]!!, 1e-9)
        assertEquals(-result.value, result.totalScores[p3]!!, 1e-9)
        assertEquals(-result.value, result.totalScores[p4]!!, 1e-9)
        assertEquals(-result.value, result.totalScores[p5]!!, 1e-9)
        assertEquals(0.0, result.totalScores.values.sum(), 1e-9)
    }

    @Test
    fun `petit au bout au profit de l attaque`() {
        val sansPetit = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
            )
        )
        val avecPetit = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
                petitAuBout = PetitAuBoutCamp.ATTACK,
            )
        )

        assertEquals(sansPetit.value + 20.0, avecPetit.value, 1e-9)
    }

    @Test
    fun `petit au bout au profit de la defense`() {
        val sansPetit = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
            )
        )
        val avecPetit = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
                petitAuBout = PetitAuBoutCamp.DEFENSE,
            )
        )

        assertEquals(sansPetit.value - 20.0, avecPetit.value, 1e-9)
    }

    @Test
    fun `poignee attribuee au camp gagnant du contrat meme si annoncee par l autre camp`() {
        val base = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
            )
        )
        val avecPoigneeAnnonceeParDefense = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
                poignee = PoigneeType.DOUBLE,
                poigneeCamp = Camp.DEFENSE,
            )
        )

        // Contrat réussi (attaque gagnante) : la poignée profite à l'attaque
        // même si c'est la défense qui l'a annoncée.
        assertEquals(base.value + 30.0, avecPoigneeAnnonceeParDefense.value, 1e-9)
    }

    @Test
    fun `poignee attribuee a la defense quand le contrat est rate`() {
        val base = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.PETITE,
                takerId = p1,
                bouts = 1,
                pointsRealises = 40.0,
            )
        )
        val avecPoignee = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.PETITE,
                takerId = p1,
                bouts = 1,
                pointsRealises = 40.0,
                poignee = PoigneeType.SIMPLE,
                poigneeCamp = Camp.ATTACK,
            )
        )

        assertFalse(avecPoignee.contractSuccess)
        assertEquals(base.value - 20.0, avecPoignee.value, 1e-9)
    }

    @Test
    fun `chelem annonce et reussi`() {
        val base = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
            )
        )
        val avecChelem = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
                chelem = ChelemResult.ANNOUNCED_WON,
            )
        )

        assertEquals(base.value + 400.0, avecChelem.value, 1e-9)
    }

    @Test
    fun `chelem non annonce et reussi`() {
        val base = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
            )
        )
        val avecChelem = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
                chelem = ChelemResult.UNANNOUNCED_WON,
            )
        )

        assertEquals(base.value + 200.0, avecChelem.value, 1e-9)
    }

    @Test
    fun `chelem annonce et rate`() {
        val base = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
            )
        )
        val avecChelem = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
                chelem = ChelemResult.ANNOUNCED_LOST,
            )
        )

        assertEquals(base.value - 200.0, avecChelem.value, 1e-9)
    }

    @Test
    fun `misere simple - l annonciateur gagne et les autres perdent la valeur configuree`() {
        val result = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.PETITE,
                takerId = p1,
                bouts = 0,
                pointsRealises = 56.0,
                miseres = listOf(MisereAnnouncement(p2, MisereType.ATOUT)),
            )
        )

        assertEquals(30.0, result.scoresByMisere[p2]!!, 1e-9)
        assertEquals(-10.0, result.scoresByMisere[p1]!!, 1e-9)
        assertEquals(-10.0, result.scoresByMisere[p3]!!, 1e-9)
        assertEquals(-10.0, result.scoresByMisere[p4]!!, 1e-9)
        assertEquals(0.0, result.scoresByMisere.values.sum(), 1e-9)
    }

    @Test
    fun `plusieurs miseres pour le meme joueur se cumulent`() {
        val result = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.PETITE,
                takerId = p1,
                bouts = 0,
                pointsRealises = 56.0,
                miseres = listOf(
                    MisereAnnouncement(p2, MisereType.ATOUT),
                    MisereAnnouncement(p2, MisereType.TETES),
                ),
            )
        )

        assertEquals(60.0, result.scoresByMisere[p2]!!, 1e-9)
        assertEquals(-20.0, result.scoresByMisere[p1]!!, 1e-9)
        assertEquals(0.0, result.scoresByMisere.values.sum(), 1e-9)
    }

    @Test
    fun `valeur de misere configurable`() {
        val result = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.PETITE,
                takerId = p1,
                bouts = 0,
                pointsRealises = 56.0,
                miseres = listOf(MisereAnnouncement(p2, MisereType.ATOUT)),
                misereValue = 5.0,
            )
        )

        assertEquals(15.0, result.scoresByMisere[p2]!!, 1e-9)
        assertEquals(-5.0, result.scoresByMisere[p1]!!, 1e-9)
    }

    @Test
    fun `les scores totaux additionnent contrat et miseres`() {
        val result = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.GARDE,
                takerId = p1,
                bouts = 2,
                pointsRealises = 45.0,
                miseres = listOf(MisereAnnouncement(p2, MisereType.ATOUT)),
            )
        )

        assertEquals(result.scoresByContract[p1]!! - 10.0, result.totalScores[p1]!!, 1e-9)
        assertEquals(result.scoresByContract[p2]!! + 30.0, result.totalScores[p2]!!, 1e-9)
        assertEquals(0.0, result.totalScores.values.sum(), 1e-9)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `nombre de bouts invalide rejete`() {
        TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.PETITE,
                takerId = p1,
                bouts = 4,
                pointsRealises = 50.0,
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `points realises non multiples de 0,5 rejetes`() {
        TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.PETITE,
                takerId = p1,
                bouts = 1,
                pointsRealises = 50.3,
            )
        )
    }

    @Test
    fun `points realises avec demi-point accepte`() {
        val result = TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.PETITE,
                takerId = p1,
                bouts = 1,
                pointsRealises = 50.5,
            )
        )

        assertEquals((25.0 + 0.5), result.base, 1e-9)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `appele requis pour une partie a 5 joueurs`() {
        TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4, p5),
                contract = Contract.PETITE,
                takerId = p1,
                bouts = 1,
                pointsRealises = 50.0,
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `appele interdit pour une partie a 3 ou 4 joueurs`() {
        TarotScoreCalculator.calculate(
            RoundInput(
                playerIds = listOf(p1, p2, p3, p4),
                contract = Contract.PETITE,
                takerId = p1,
                calledId = p2,
                bouts = 1,
                pointsRealises = 50.0,
            )
        )
    }
}
