package com.example.cardgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardgame.LocalData.DEFAULT_CHIP_NUMBER
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainViewModel(val localData: LocalDataRepository = LocalData) : ViewModel() {
    private val _state = MutableStateFlow(ScreenState(initPlayers(localData)))
    val state = _state.asStateFlow()

    private val deck = deckPokerWithJokers.toMutableList().apply { shuffle() }

    fun dealingCards() {
        viewModelScope.launch {
            _state.update { it.copy(isDealing = true) }
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
            _state.update { oldState ->
                oldState.copy(players = oldState.players.map { it.sortCards() })
            }
            _state.update { it.copy(isDealing = false) }

            delay(1000L)
            _state.update { it.payToBank(0, 10) }

            delay(1000L)
            _state.update { it.takeBank(0) }
        }
    }

    fun resetGame() {
        with (localData) {
            player0Chips = DEFAULT_CHIP_NUMBER
            player1Chips = DEFAULT_CHIP_NUMBER
            player2Chips = DEFAULT_CHIP_NUMBER
            player3Chips = DEFAULT_CHIP_NUMBER
            isPlayer1Active = true
            isPlayer2Active = true
            isPlayer3Active = true
            isDialActive = false
            dealerIndex = Random.nextInt(4)
        }
    }

    fun newDial() {
        if (localData.isDialActive) {
            resetGame()
        }
        dealerIndex = localData.dealerIndex
        localData.isDialActive = true

        // раздать карты

        isPostDraw = false
        currentBet = 0
        numOfRaise = 0
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
            isDialActive = false
        }
    }

    var dealerIndex = 0
    var isPostDraw = false
    var currentBet = 0
    var numOfRaise = 0

    companion object {
        const val ANTE_BET = 1
        const val PRE_DRAW_BET = 1
        const val PRE_DRAW_RAISE = 1
        const val POST_DRAW_BET = 2
        const val POST_DRAW_RAISE = 2
        const val MAX_NUM_OF_RAISE = 3
    }
}