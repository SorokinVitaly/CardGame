package com.example.cardgame


data class PlayerData(
    val name: String,
    val cards: List<Card> = emptyList(),
    val chips: Int = 0,
    val isActive: Boolean,
    val lastDraw: ActionType = ActionType.NoAction(),
    val lastBet: ActionType = ActionType.NoAction()
) {
    val isInGame = isActive && lastBet !is ActionType.Fold
    val footerText = if (lastDraw !is ActionType.Draw) {
        ""
    } else {
        "Draw count: ${lastDraw.number} "
    } + lastBet.name

    fun payChips(payed: Int) = copy(chips = chips - payed)
    fun clearCards() = copy(cards =emptyList())
    fun sortCards() = copy(cards = cards.sortedBy { it.rank })
    fun addCard(card: Card) = copy(cards = cards + card)
    //fun removeCard(card: Card) = copy(cards = cards - card)
    //fun changeCard(oldCard: Card, newCard: Card) = copy(cards = cards - oldCard + newCard)
}