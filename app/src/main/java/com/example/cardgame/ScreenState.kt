package com.example.cardgame

import androidx.compose.runtime.Immutable

@Immutable
data class ScreenState(val players: List<PlayerData>) {
    init {
        if (players.size != 4) {
            throw IllegalStateException("Wrong number of players")
        }
    }

    fun updatePlayer(index: Int, update: PlayerData.() -> PlayerData) =
        copy(players = players.mapIndexed { i, player ->
            if (index == i) player.update() else player
        })
}