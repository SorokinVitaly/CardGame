package com.example.cardgame

import androidx.compose.runtime.Immutable


@Immutable
data class ScreenState(
    val players: List<PlayerData>,
    val bankChips: Int = 0,
    val isActionAvailable: Boolean = true,
    val isDealAvailable: Boolean = true,
    val isResetAvailable: Boolean = true,
    val isBetAvailable: Boolean = false
) {
    init {
        if (players.size != 4) {
            throw IllegalStateException("Wrong number of players")
        }
    }

    fun updatePlayer(index: Int, update: PlayerData.() -> PlayerData) =
        copy(players = updateOnePlayer(index, update))

    fun payToBank(index: Int, payed: Int) =
        copy(
            players = updateOnePlayer(index) { payChips(payed) },
            bankChips = bankChips + payed
        )

    fun takeBank(index: Int) = payToBank(index, -bankChips)

    private fun updateOnePlayer(index: Int, update: PlayerData.() -> PlayerData) =
        players.mapIndexed { i, player ->
            if (index == i) player.update() else player
        }
}