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

    private val deck = deckPokerWithJokers.toMutableList().apply { shuffle() }
    private var isPostDraw = false
    private var dealerIndex = 0
    private var currentBet = 0
    private var numOfRaise = 0

    init {
        if (localData.isGameStarted) {
            onResetGame()
        }
    }

    /*        private var current = 0

            override fun hasNext(): Boolean = current < max
            override fun next(): Int = current++*/


    fun onResetGame() {
        localData.resetGame()
        _state.update { localData.savedState() }
    }

    fun dealingCards() {
        viewModelScope.launch {
            localData.isJustReset = false
            localData.isGameStarted = true
            _state.update { it.copy(isActionAvailable = false) }

            repeat(5) {
                repeat(4) { index ->
                    if (_state.value.players[index].isActive) {
                        delay(250L)
                        val card = deck.removeAt(deck.lastIndex)
                        _state.update { it.updatePlayer(index) { addCard(card) } }
                    }
                }
            }
            delay(500L)

            dealerIndex = localData.dealerIndex
            isPostDraw = false
            currentBet = 0
            numOfRaise = 0
            _state.update { it.copy(players = it.players.map { player -> player.sortCards() }) }
            _state.update { it.copy(isActionAvailable = true, isBetAvailable = true) }
        }
    }

    fun newDial() {
        if (localData.isGameStarted) {
            onResetGame()
        }
        dealerIndex = localData.dealerIndex
        localData.isGameStarted = true

        // раздать карты

        isPostDraw = false
        currentBet = 0
        numOfRaise = 0

        //========================
        // PreDraw раунд ставок

        isPostDraw = true
        currentBet = 0
        numOfRaise = 0
        // PostDraw раунд ставок
        // Открыть карты и определить победителя

        _state.value.takeBank(0)

        dealerIndex = (dealerIndex + 1) and 3

        with (localData) {
            player0Chips = _state.value.players[0].chips
            player1Chips = _state.value.players[1].chips
            player2Chips = _state.value.players[2].chips
            player3Chips = _state.value.players[3].chips
            dealerIndex = dealerIndex
            isGameStarted = false
        }
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