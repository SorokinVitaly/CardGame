package com.example.cardgame

import androidx.compose.runtime.Immutable


@Immutable
data class ScreenState(
    val players: List<PlayerData>,
    val betAvailable: List<ActionType>,
    val bankChips: Int,
    val isDrawEnabled: Boolean,
    val isActionAvailable: Boolean,
    val isDealAvailable: Boolean,
    val isResetAvailable: Boolean
) {
    init {
        if (players.size != 4) {
            throw IllegalStateException("Wrong number of players")
        }
    }

    fun updateAllPlayers(update: PlayerData.() -> PlayerData) =
        copy(players = players.map { it.update() })

    fun updatePlayer(index: Int, update: PlayerData.() -> PlayerData) =
        copy(players = updateOnePlayer(index, update))

    fun payToBank(index: Int, payed: Int) =
        copy(
            players = updateOnePlayer(index) { payChips(payed) },
            bankChips = bankChips + payed
        )

    //fun takeBank(index: Int) = payToBank(index, -bankChips)

    private fun updateOnePlayer(index: Int, update: PlayerData.() -> PlayerData) =
        players.mapIndexed { i, player ->
            if (index == i) player.update() else player
        }
}