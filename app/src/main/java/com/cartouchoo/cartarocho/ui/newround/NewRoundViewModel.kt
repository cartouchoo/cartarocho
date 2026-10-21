package com.cartouchoo.cartarocho.ui.newround

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cartouchoo.cartarocho.data.model.Player
import com.cartouchoo.cartarocho.data.model.RoundDraft
import com.cartouchoo.cartarocho.data.repository.GameRepository
import com.cartouchoo.cartarocho.data.repository.RoundRepository
import com.cartouchoo.cartarocho.domain.Camp
import com.cartouchoo.cartarocho.domain.CardCount
import com.cartouchoo.cartarocho.domain.TrickPhotoParser
import com.cartouchoo.cartarocho.domain.ChelemResult
import com.cartouchoo.cartarocho.domain.Contract
import com.cartouchoo.cartarocho.domain.MisereAnnouncement
import com.cartouchoo.cartarocho.domain.MisereType
import com.cartouchoo.cartarocho.domain.PetitAuBoutCamp
import com.cartouchoo.cartarocho.domain.PoigneeType
import com.cartouchoo.cartarocho.domain.RoundInput
import com.cartouchoo.cartarocho.domain.TarotScoreCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Étapes de la saisie des points par photo des plis. */
sealed interface PhotoAnalysis {
    data object Analyzing : PhotoAnalysis

    /** Comptage pré-rempli depuis la photo, modifiable avant d'être appliqué. */
    data class Review(val count: CardCount, val trumpsSeen: List<Int>) : PhotoAnalysis

    data class Failed(val message: String) : PhotoAnalysis
}

data class NewRoundUiState(
    val players: List<Player> = emptyList(),
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val contract: Contract = Contract.PETITE,
    val takerId: Long? = null,
    val calledId: Long? = null,
    val bouts: Int = 0,
    val pointsText: String = "",
    val petitAuBout: PetitAuBoutCamp = PetitAuBoutCamp.NONE,
    val poignee: PoigneeType = PoigneeType.NONE,
    val poigneeCamp: Camp = Camp.ATTACK,
    val chelem: ChelemResult = ChelemResult.NONE,
    val miseres: List<MisereAnnouncement> = emptyList(),
    val photoAnalysis: PhotoAnalysis? = null,
) {
    val playerCount: Int get() = players.size
    val points: Double? get() = pointsText.trim().replace(',', '.').toDoubleOrNull()

    /** Résultat du calcul si la saisie courante est valide, sinon null. */
    val previewScores: Map<Long, Double>?
        get() {
            val taker = takerId ?: return null
            val pts = points ?: return null
            if (players.isEmpty()) return null
            if (playerCount == 5 && calledId == null) return null
            return try {
                TarotScoreCalculator.calculate(
                    RoundInput(
                        playerIds = players.map { it.id },
                        contract = contract,
                        takerId = taker,
                        calledId = if (playerCount == 5) calledId else null,
                        bouts = bouts,
                        pointsRealises = pts,
                        petitAuBout = petitAuBout,
                        poignee = poignee,
                        poigneeCamp = poigneeCamp,
                        chelem = chelem,
                        miseres = miseres,
                    )
                ).totalScores
            } catch (_: IllegalArgumentException) {
                null
            }
        }

    val canValidate: Boolean get() = !isSaving && previewScores != null
}

class NewRoundViewModel(
    private val gameId: Long,
    private val roundId: Long?,
    private val gameRepository: GameRepository,
    private val roundRepository: RoundRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewRoundUiState())
    val uiState: StateFlow<NewRoundUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val players = gameRepository.getPlayers(gameId)
            val existing = roundId?.let { roundRepository.getRound(it) }
            _uiState.update { state ->
                if (existing != null) {
                    val draft = existing.draft
                    state.copy(
                        players = players,
                        isLoading = false,
                        isEditing = true,
                        contract = draft.contract,
                        takerId = draft.takerId,
                        calledId = draft.calledId,
                        bouts = draft.bouts,
                        pointsText = formatPointsText(draft.points),
                        petitAuBout = draft.petitAuBout,
                        poignee = draft.poignee,
                        poigneeCamp = draft.poigneeCamp,
                        chelem = draft.chelem,
                        miseres = draft.miseres,
                    )
                } else {
                    state.copy(players = players, isLoading = false, takerId = players.firstOrNull()?.id)
                }
            }
        }
    }

    fun setContract(contract: Contract) = updateState { it.copy(contract = contract) }
    fun setTaker(playerId: Long) = updateState { it.copy(takerId = playerId) }
    fun setCalled(playerId: Long) = updateState { it.copy(calledId = playerId) }
    fun setBouts(bouts: Int) = updateState { it.copy(bouts = bouts) }
    fun setPointsText(text: String) = updateState { it.copy(pointsText = text) }
    fun setPetitAuBout(value: PetitAuBoutCamp) = updateState { it.copy(petitAuBout = value) }
    fun setPoignee(value: PoigneeType) = updateState { it.copy(poignee = value) }
    fun setPoigneeCamp(value: Camp) = updateState { it.copy(poigneeCamp = value) }
    fun setChelem(value: ChelemResult) = updateState { it.copy(chelem = value) }

    fun toggleMisere(playerId: Long, type: MisereType) = updateState { state ->
        val announcement = MisereAnnouncement(playerId, type)
        if (announcement in state.miseres) {
            state.copy(miseres = state.miseres - announcement)
        } else {
            state.copy(miseres = state.miseres + announcement)
        }
    }

    fun onPhotoAnalyzing() = updateState { it.copy(photoAnalysis = PhotoAnalysis.Analyzing) }

    fun onPhotoRead(words: List<String>) {
        val detection = TrickPhotoParser.parse(words)
        updateState {
            it.copy(photoAnalysis = PhotoAnalysis.Review(detection.toCardCount(), detection.trumps.sorted()))
        }
    }

    fun onPhotoFailed(message: String) = updateState { it.copy(photoAnalysis = PhotoAnalysis.Failed(message)) }

    fun updateCardCount(count: CardCount) = updateState { state ->
        val review = state.photoAnalysis as? PhotoAnalysis.Review ?: return@updateState state
        state.copy(photoAnalysis = review.copy(count = count))
    }

    /** Reporte les points et les bouts comptés dans la donne. */
    fun applyCardCount() = updateState { state ->
        val review = state.photoAnalysis as? PhotoAnalysis.Review ?: return@updateState state
        state.copy(
            bouts = review.count.bouts,
            pointsText = formatPointsText(review.count.points),
            photoAnalysis = null,
        )
    }

    fun dismissPhotoAnalysis() = updateState { it.copy(photoAnalysis = null) }

    fun validate(onDone: () -> Unit) {
        val state = _uiState.value
        if (!state.canValidate) return
        val draft = RoundDraft(
            contract = state.contract,
            takerId = requireNotNull(state.takerId),
            calledId = if (state.playerCount == 5) state.calledId else null,
            bouts = state.bouts,
            points = requireNotNull(state.points),
            petitAuBout = state.petitAuBout,
            poignee = state.poignee,
            poigneeCamp = state.poigneeCamp,
            chelem = state.chelem,
            miseres = state.miseres,
        )
        updateState { it.copy(isSaving = true) }
        viewModelScope.launch {
            if (roundId == null) {
                roundRepository.addRound(gameId, draft)
            } else {
                roundRepository.updateRound(roundId, draft)
            }
            onDone()
        }
    }

    private inline fun updateState(block: (NewRoundUiState) -> NewRoundUiState) {
        _uiState.update(block)
    }

    companion object {
        fun factory(
            gameId: Long,
            roundId: Long?,
            gameRepository: GameRepository,
            roundRepository: RoundRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { NewRoundViewModel(gameId, roundId, gameRepository, roundRepository) }
        }
    }
}

private fun formatPointsText(value: Double): String =
    if (value == Math.floor(value)) value.toLong().toString() else value.toString()
