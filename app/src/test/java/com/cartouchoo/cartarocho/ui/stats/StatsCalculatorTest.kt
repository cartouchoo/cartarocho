package com.cartouchoo.cartarocho.ui.stats

import com.cartouchoo.cartarocho.data.model.GamePlayerLink
import com.cartouchoo.cartarocho.data.model.Player
import com.cartouchoo.cartarocho.data.model.RoundDraft
import com.cartouchoo.cartarocho.data.model.RoundWithScores
import com.cartouchoo.cartarocho.domain.Camp
import com.cartouchoo.cartarocho.domain.ChelemResult
import com.cartouchoo.cartarocho.domain.Contract
import com.cartouchoo.cartarocho.domain.MisereAnnouncement
import com.cartouchoo.cartarocho.domain.MisereType
import com.cartouchoo.cartarocho.domain.PetitAuBoutCamp
import com.cartouchoo.cartarocho.domain.PoigneeType
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsCalculatorTest {

    private val alice = Player(1, "Alice")
    private val bob = Player(2, "Bob")
    private val charlie = Player(3, "Charlie")
    private val players = listOf(alice, bob, charlie)

    private fun round(
        id: Long,
        gameId: Long,
        index: Int,
        contract: Contract = Contract.PETITE,
        takerId: Long,
        calledId: Long? = null,
        bouts: Int = 0,
        points: Double = 56.0,
        poignee: PoigneeType = PoigneeType.NONE,
        poigneeCamp: Camp = Camp.ATTACK,
        chelem: ChelemResult = ChelemResult.NONE,
        miseres: List<MisereAnnouncement> = emptyList(),
        scores: Map<Long, Double>,
    ) = RoundWithScores(
        id = id,
        gameId = gameId,
        index = index,
        draft = RoundDraft(
            contract = contract,
            takerId = takerId,
            calledId = calledId,
            bouts = bouts,
            points = points,
            petitAuBout = PetitAuBoutCamp.NONE,
            poignee = poignee,
            poigneeCamp = poigneeCamp,
            chelem = chelem,
            miseres = miseres,
        ),
        scores = scores,
    )

    @Test
    fun `parties jouees et score cumule pour un seul round`() {
        val links = listOf(
            GamePlayerLink(gameId = 1, playerId = alice.id),
            GamePlayerLink(gameId = 1, playerId = bob.id),
            GamePlayerLink(gameId = 1, playerId = charlie.id),
        )
        val rounds = listOf(
            round(
                id = 1, gameId = 1, index = 0, takerId = alice.id,
                scores = mapOf(alice.id to 100.0, bob.id to -50.0, charlie.id to -50.0),
            ),
        )

        val stats = computePlayerStats(players, links, rounds)

        val aliceStats = stats.first { it.player.id == alice.id }
        assertEquals(1, aliceStats.gamesPlayed)
        assertEquals(100.0, aliceStats.totalScore, 1e-9)
        assertEquals(100.0, aliceStats.averageScorePerGame, 1e-9)
        assertEquals(1, aliceStats.wins)

        val bobStats = stats.first { it.player.id == bob.id }
        assertEquals(0, bobStats.wins)
        assertEquals(-50.0, bobStats.totalScore, 1e-9)
    }

    @Test
    fun `un joueur sans partie a des stats a zero`() {
        val stats = computePlayerStats(players, emptyList(), emptyList())

        stats.forEach { stat ->
            assertEquals(0, stat.gamesPlayed)
            assertEquals(0.0, stat.totalScore, 1e-9)
            assertEquals(0.0, stat.averageScorePerGame, 1e-9)
            assertEquals(0, stat.wins)
        }
    }

    @Test
    fun `seul le premier compte comme victoire meme en cas d egalite`() {
        val links = listOf(
            GamePlayerLink(gameId = 1, playerId = alice.id),
            GamePlayerLink(gameId = 1, playerId = bob.id),
            GamePlayerLink(gameId = 1, playerId = charlie.id),
        )
        // Bob et Charlie à égalité derrière Alice.
        val rounds = listOf(
            round(
                id = 1, gameId = 1, index = 0, takerId = alice.id,
                scores = mapOf(alice.id to 60.0, bob.id to -30.0, charlie.id to -30.0),
            ),
        )

        val stats = computePlayerStats(players, links, rounds)

        assertEquals(1, stats.first { it.player.id == alice.id }.wins)
        assertEquals(0, stats.first { it.player.id == bob.id }.wins)
        assertEquals(0, stats.first { it.player.id == charlie.id }.wins)
    }

    @Test
    fun `taux de reussite au contrat et repartition des contrats`() {
        val links = listOf(GamePlayerLink(gameId = 1, playerId = alice.id))
        val rounds = listOf(
            round(
                id = 1, gameId = 1, index = 0, contract = Contract.PETITE, takerId = alice.id,
                bouts = 0, points = 56.0, // réussi
                scores = mapOf(alice.id to 50.0, bob.id to -25.0, charlie.id to -25.0),
            ),
            round(
                id = 2, gameId = 1, index = 1, contract = Contract.GARDE, takerId = alice.id,
                bouts = 0, points = 40.0, // raté (seuil 56)
                scores = mapOf(alice.id to -50.0, bob.id to 25.0, charlie.id to 25.0),
            ),
        )

        val stats = computePlayerStats(players, links, rounds).first { it.player.id == alice.id }

        assertEquals(2, stats.timesTaker)
        assertEquals(1, stats.takerSuccesses)
        assertEquals(1, stats.contractCounts[Contract.PETITE])
        assertEquals(1, stats.contractCounts[Contract.GARDE])
    }

    @Test
    fun `chelem poignee et misere sont comptabilises`() {
        val links = listOf(GamePlayerLink(gameId = 1, playerId = alice.id))
        val rounds = listOf(
            round(
                id = 1, gameId = 1, index = 0, takerId = alice.id,
                chelem = ChelemResult.ANNOUNCED_WON,
                poignee = PoigneeType.DOUBLE,
                poigneeCamp = Camp.ATTACK,
                miseres = listOf(MisereAnnouncement(bob.id, MisereType.ATOUT)),
                scores = mapOf(alice.id to 500.0, bob.id to -250.0, charlie.id to -250.0),
            ),
            round(
                id = 2, gameId = 1, index = 1, takerId = bob.id,
                miseres = listOf(
                    MisereAnnouncement(alice.id, MisereType.ATOUT),
                    MisereAnnouncement(alice.id, MisereType.TETES),
                ),
                scores = mapOf(alice.id to -25.0, bob.id to 50.0, charlie.id to -25.0),
            ),
        )

        val stats = computePlayerStats(players, links, rounds).first { it.player.id == alice.id }

        assertEquals(1, stats.chelemCount)
        assertEquals(1, stats.poigneeCount)
        assertEquals(2, stats.misereCount)
    }

    @Test
    fun `poignee annoncee par la defense profite au preneur si le contrat reussit`() {
        val links = listOf(GamePlayerLink(gameId = 1, playerId = bob.id))
        val rounds = listOf(
            round(
                id = 1, gameId = 1, index = 0, takerId = alice.id,
                poignee = PoigneeType.SIMPLE,
                poigneeCamp = Camp.DEFENSE,
                scores = mapOf(alice.id to 100.0, bob.id to -50.0, charlie.id to -50.0),
            ),
        )

        val bobStats = computePlayerStats(players, links, rounds).first { it.player.id == bob.id }

        // Bob est défenseur et la poignée a été annoncée par la défense : elle le concerne.
        assertEquals(1, bobStats.poigneeCount)
    }
}
