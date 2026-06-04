package com.example.cardgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class MainViewModel(val localData: LocalDataRepository = LocalData) : ViewModel() {
    private val _state = MutableStateFlow(localData.savedState())
    val state = _state.asStateFlow()

    private val deck = mutableListOf<Card>()
    private var isPostDraw = false
    private var currentBet = 0
    private var numOfRaise = 0

    init {
        if (localData.isGameStarted) {
            onResetGame()
        }
    }

    fun onResetGame() {
        localData.resetGame()
        _state.update { localData.savedState() }
    }

    fun onDialNext() {
        viewModelScope.launch {
            localData.isJustReset = false
            localData.isGameStarted = true
            _state.update { it.copy(isActionAvailable = false) }

            newDeck()
            payAnte()
            dealingCards()

            isPostDraw = false
            betRound()

            isPostDraw = true
            betRound()

            _state.value.takeBank(0)

            //dealerIndex = (dealerIndex + 1) and 3

            with (localData) {
                player0Chips = player(0).chips
                player1Chips = player(1).chips
                player2Chips = player(2).chips
                player3Chips = player(3).chips
                isGameStarted = false
            }
            _state.update { it.copy(isActionAvailable = true) }
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

    private suspend fun betRound() {
        currentBet = 0
        numOfRaise = 0

    }

    private fun newDeck() {
        deck.clear()
        deck.addAll(deckPoker)
        deck.shuffle()
    }

    private fun player(index: Int) = _state.value.players[index]

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

    private fun availableBets(playerIndex: Int): List<BetType> {
        val (betCount, raiseCount) = if (isPostDraw) POST_DRAW_BET to POST_DRAW_RAISE else PRE_DRAW_BET to PRE_DRAW_RAISE
        val chips = player(playerIndex).chips
        val bets = ArrayList<BetType>()
        if (currentBet == 0) {
            bets.add(BetType.Check())
            if (chips >= betCount) {
                bets.add(BetType.Bet(betCount))
            }
            bets.add(BetType.Fold())
        } else {
            if (chips >= currentBet) {
                bets.add(BetType.Call(currentBet))
            }
            if (chips >= currentBet + raiseCount && numOfRaise < MAX_NUM_OF_RAISE) {
                bets.add(BetType.Raise(currentBet + raiseCount))
            }
            bets.add(BetType.Fold())
        }
        return bets
    }

    companion object {
        const val ANTE_BET = 1
        const val PRE_DRAW_BET = 1
        const val PRE_DRAW_RAISE = 1
        const val POST_DRAW_BET = 2
        const val POST_DRAW_RAISE = 2
        const val MAX_NUM_OF_RAISE = 3
    }
}