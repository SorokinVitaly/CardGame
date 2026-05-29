package com.example.cardgame

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(localData: LocalDataRepository = LocalData) : ViewModel() {
    val player0 = PlayerData(
        name = localData.player0Name,
        type = PlayerType.IT_ME,
        chips = localData.player0Chips
    )
    val player1 = PlayerData(
        name = localData.player1Name,
        type = isPlayerActive(localData.isPlayer1Active),
        chips = localData.player1Chips
    )
    val player2 = PlayerData(
        name = localData.player2Name,
        type = isPlayerActive(localData.isPlayer2Active),
        chips = localData.player2Chips
    )
    val player3 = PlayerData(
        name = localData.player3Name,
        type = isPlayerActive(localData.isPlayer3Active),
        chips = localData.player3Chips
    )

    private fun isPlayerActive(isActive: Boolean): PlayerType =
        if (isActive) {
            PlayerType.ACTIVE
        } else {
            PlayerType.NOT_ACTIVE
        }

    private val _state = MutableStateFlow(ScreenState(
        listOf(player0, player1, player2, player3)
    ))
    val state = _state.asStateFlow()

    private val deck: MutableList<Card> = deckPokerWithJokers.toMutableList()

    fun dealingCards() {

    }
}