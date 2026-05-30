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
) {
    val isActive = type != PlayerType.NOT_ACTIVE
    fun sortCards() = copy(cards = cards.sortedBy { it.rank })
    fun addCard(card: Card) = copy(cards = cards + card)
    fun removeCard(card: Card) = copy(cards = cards - card)
    fun changeCard(oldCard: Card, newCard: Card) = copy(cards = cards - oldCard + newCard)
}