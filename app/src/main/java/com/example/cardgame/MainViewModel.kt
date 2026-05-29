package com.example.cardgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class MainViewModel(localData: LocalDataRepository = LocalData) : ViewModel() {
    private val _state = MutableStateFlow(ScreenState(initPlayers(localData)))
    val state = _state.asStateFlow()

    private val deck = deckPokerWithJokers.toMutableList().apply { shuffle() }

    fun dealingCards() {
        viewModelScope.launch {
            _state.update { it.copy(isDealing = true) }
            repeat(5) {
                repeat(4) { index ->
                    if (_state.value.players[index].type != PlayerType.NOT_ACTIVE) {
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
        }
    }
}