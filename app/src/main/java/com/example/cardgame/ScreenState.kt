package com.example.cardgame

import androidx.compose.runtime.Immutable

@Immutable
data class ScreenState(val players: List<PlayerData>) {
    init {
        if (players.size != 4) {
            throw IllegalStateException("Wrong number of players")
        }
    }
}