package com.example.cardgame


enum class PlayerType {
    IT_ME,
    ACTIVE,
    NOT_ACTIVE
}

data class PlayerData(
    val name: String,
    val type: PlayerType,
    val cards: List<Card> = emptyList(),
    val chips: Int = 0
)