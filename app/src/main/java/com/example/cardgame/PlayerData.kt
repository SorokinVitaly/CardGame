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
    val chips: Int = 0,
    val drawCount: Int = -1,
    val lastBet: LastBet = LastBet.NoBet()
) {
    val footerText = if (drawCount < 0) { "" } else { "Draw count: $drawCount " } + lastBet.name
    val isActive = type != PlayerType.NOT_ACTIVE

    fun payChips(payed: Int) = copy(chips = chips - payed)
    fun sortCards() = copy(cards = cards.sortedBy { it.rank })
    fun addCard(card: Card) = copy(cards = cards + card)
    //fun removeCard(card: Card) = copy(cards = cards - card)
    //fun changeCard(oldCard: Card, newCard: Card) = copy(cards = cards - oldCard + newCard)
}