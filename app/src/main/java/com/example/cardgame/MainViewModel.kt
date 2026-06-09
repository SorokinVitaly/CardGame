package com.example.cardgame

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class MainViewModel(val localData: LocalDataRepository = LocalData) : ViewModel() {
    private val _state = MutableStateFlow(localData.savedState())
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    enum class RoundType {
        PRE_DRAW,
        DRAW,
        POST_DRAW
    }

    private val deck = mutableListOf<Card>()
    private var currentBet = 0
    private var numOfRaise = 0
    private var playerIndex = 0
    private var round = RoundType.PRE_DRAW

    init {
        if (localData.isGameStarted) {
            onResetGame()
        }
    }

    fun onResetGame() {
        localData.resetGame()
        _state.update { localData.savedState() }
        viewModelScope.launch {
            val mess = "Game was restarted"
            log(mess)
            delay(500L)
            _events.emit(UiEvent.ShowToast(mess))
        }
    }

    fun onDialNext() {
        viewModelScope.launch {
            _state.update { localData.savedState() }
            localData.isJustReset = false
            localData.isGameStarted = true
            localData.dealerIndex = nextInGameIndex(localData.dealerIndex)
            _state.update { it.copy(isActionAvailable = false) }
            newDeck()
            dealingCards()
            payAnte()
            currentBet = 0
            numOfRaise = 0
            playerIndex = localData.dealerIndex
            round = RoundType.PRE_DRAW
            History.clear()
            mainGameLoop()
        }
    }

    fun onAction(action: ActionType) {
        applyAction(0, action)
        viewModelScope.launch {
            mainGameLoop()
        }
    }

    private suspend fun payAnte() {
        repeat(4) { index ->
            if (player(index).isActive) {
                delay(300L)
                _state.update { it.payToBank(index, ANTE_BET) }
            }
        }
    }

    private suspend fun dealingCards() {
        _state.update { it.updateAllPlayers { clearCards() } }
        repeat(5) {
            repeat(4) { index ->
                if (player(index).isActive) {
                    delay(300L)
                    val card = deck.removeAt(deck.lastIndex)
                    _state.update { it.updatePlayer(index) { addCard(card) } }
                }
            }
        }
        delay(500L)
        _state.update { it.updateAllPlayers { sortCards() } }
    }

    private suspend fun mainGameLoop() {
        while (true) {
            val inGamePlayers = _state.value.players.filter { it.isInGame }

            if (inGamePlayers.size == 1) {
                gameOver(_state.value.players.indexOfFirst { it.isInGame })
                return
            }

            val endRound = if (round != RoundType.DRAW) {
                inGamePlayers.all { it.lastBet.bet == currentBet }
            } else {
                inGamePlayers.all { it.lastDraw is ActionType.Draw }
            }
            if (endRound) {
                if (endRound()) {
                    return
                }
            }

            playerIndex = nextInGameIndex(playerIndex)
            val availableActions = availableActions(playerIndex)
            if (playerIndex == 0) {
                _state.update { it.copy(isActionAvailable = true, actionsAvailable = availableActions) }
                return
            } else {
                val action = botBetting(playerIndex, availableActions)
                applyAction(playerIndex, action)
            }
        }
    }

    private suspend fun endRound(): Boolean {
        val newRound = when (round) {
            RoundType.PRE_DRAW -> {
                RoundType.DRAW
            }
            RoundType.DRAW -> {
                _state.update {
                    it.copy(players = it.players.map { player ->
                        if (player.isInGame) {
                            player.copy(lastBet = ActionType.NoAction())
                        } else {
                            player
                        }
                    })
                }
                RoundType.POST_DRAW
            }
            RoundType.POST_DRAW -> {
                _state.update { it.copy(isCardsOpen = true) }
                val combinations = _state.value.players.mapIndexedNotNull { i, playerData ->
                    if (playerData.isInGame) i to Combination.calcCombination(playerData.cards)
                    else null
                }
                val winCombination = combinations.maxBy { it.second }
                gameOver(winCombination.first)
                return true
            }
        }
        playerIndex = localData.dealerIndex
        round = newRound
        return false
    }

    private suspend fun gameOver(winIndex: Int) {
        val mess = "${player(winIndex).name} won and take bank ${_state.value.bankChips} chips"
        log(mess)
        _events.emit(UiEvent.ShowToast(mess))
        _state.update { it.takeBank(winIndex) }
        localData.saveState(_state.value)
        _state.update { it.copy(
            actionsAvailable = emptyList(),
            isDrawEnabled = false,
            isActionAvailable = true,
            isDealAvailable = true,
            isResetAvailable = true
        ) }
    }

    private fun newDeck() {
        deck.clear()
        deck.addAll(deckPoker)
        deck.shuffle()
    }

    private fun player(index: Int) = _state.value.players[index]

    private fun botBetting(index: Int, availableActions: List<ActionType>): ActionType {
        return availableActions.random()
    }

    private fun applyAction(index: Int, action: ActionType) {
        log("$index: ${action.name}")
        History.add(index, action)
        if (action is ActionType.Raise) {
            numOfRaise++
        }
        if (action.bet > 0) {
            currentBet = action.bet
            _state.update { it.payToBank(index, action.bet) }
        }
        if (action is ActionType.Draw) {
            _state.update { it.updatePlayer(index) { copy(lastDraw = action) } }
        } else {
            _state.update { it.updatePlayer(index) { copy(lastBet = action) } }
        }
    }

    private fun nextInGameIndex(index: Int): Int {
        val first = (index + 1) and 3
        var current = first

        while (true) {
            if (player(current).isInGame) {
                return current
            }
            current = (current + 1) and 3
            if (current == first) {
                throw IllegalStateException("Next player not found")
            }
        }
    }

    private fun availableActions(playerIndex: Int): List<ActionType> {
        if (round == RoundType.DRAW) {
            return listOf(ActionType.Draw())
        }
        val (betCount, raiseCount) = if (round == RoundType.PRE_DRAW) PRE_DRAW_BET to PRE_DRAW_RAISE else POST_DRAW_BET to POST_DRAW_RAISE
        val chips = player(playerIndex).chips
        val bets = ArrayList<ActionType>()
        if (currentBet == 0) {
            bets.add(ActionType.Check())
            if (chips >= betCount) {
                bets.add(ActionType.Bet(betCount))
            }
            bets.add(ActionType.Fold())
        } else {
            if (chips >= currentBet) {
                bets.add(ActionType.Call(currentBet))
            }
            if (chips >= currentBet + raiseCount && numOfRaise < MAX_NUM_OF_RAISE) {
                bets.add(ActionType.Raise(currentBet + raiseCount))
            }
            bets.add(ActionType.Fold())
        }
        return bets
    }

    private fun log(mess: String) = Log.e("GamePlay", mess)

    companion object {
        const val ANTE_BET = 1
        const val PRE_DRAW_BET = 1
        const val PRE_DRAW_RAISE = 1
        const val POST_DRAW_BET = 2
        const val POST_DRAW_RAISE = 2
        const val MAX_NUM_OF_RAISE = 3
    }
}