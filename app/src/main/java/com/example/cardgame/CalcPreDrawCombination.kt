package com.example.cardgame

fun calcPreDrawCombination(history: History, cards: List<Card>): DrawCombination {
    require(cards.size == 5)
    val combination = calcCombination(cards)
    if (combination.type >= CombinationType.STRAIGHT) {
        return DrawCombination(combination)
    }
    if (combination.type == CombinationType.THREE_OF_A_KIND) {
        return DrawCombination(
            combination,
            cardsForDraw = cards.filter { it.rank != combination.highRank }
        )
    }
    if (combination.type == CombinationType.TWO_PAIRS) {
        return DrawCombination(
            combination,
            cardsForDraw = cards.filter { it.rank != combination.highRank && it.rank != combination.lowRank }
        )
    }
    if (combination.type == CombinationType.PAIR) {
        if (combination.highRank < CardRank.EIGHT) {
            findFourToStraightFlush(cards)?.let {
                return DrawCombination(
                    combination,
                    IncompleteCombinationType.FOUR_TO_STRAIGHT_FLUSH,
                    cardsForDraw = it
                )
            }
        }
        val kicker = combination.highKicker()
        val list = if (kicker >= CardRank.JACK && history.isAggressiveTable()) {
            cards.filter { it.rank != kicker && it.rank != combination.highRank }
        } else {
            cards.filter { it.rank != combination.highRank }
        }
        return DrawCombination(
            combination,
            cardsForDraw = list
        )
    }
    findFourToStraightFlush(cards)?.let {
        return DrawCombination(
            combination,
            IncompleteCombinationType.FOUR_TO_STRAIGHT_FLUSH,
            cardsForDraw = it
        )
    }
    findThreeToStraightFlush(cards)?.let {
        return DrawCombination(
            combination,
            IncompleteCombinationType.THREE_TO_STRAIGHT_FLUSH,
            cardsForDraw = it
        )
    }
    findFourToFlush(cards)?.let {
        return DrawCombination(
            combination,
            IncompleteCombinationType.FOUR_TO_FLUSH,
            cardsForDraw = it
        )
    }
    findFourToStraightOpen(cards)?.let {
        return DrawCombination(
            combination,
            IncompleteCombinationType.FOUR_TO_STRAIGHT_OPEN,
            cardsForDraw = it
        )
    }
    findFourToStraight(cards)?.let {
        return DrawCombination(
            combination,
            IncompleteCombinationType.FOUR_TO_STRAIGHT,
            cardsForDraw = it
        )
    }
    val numCards = if (combination.highKicker() >= CardRank.JACK) 3 else 4
    return DrawCombination(
        combination,
        cardsForDraw = cards.take(numCards)
    )
}

// Functions to find incomplete combination. Return set of cards to draw or null
private fun findFourToStraightFlush(cards: List<Card>): List<Card>? {
    val sameSuitCards = findSameSuit(cards, 4) ?: return null
    return if (sameSuitCards[3].rank.ordinal - sameSuitCards[0].rank.ordinal != 3 ||
        sameSuitCards[3].rank == CardRank.ACE
    ) null else cards - sameSuitCards
}

private fun findThreeToStraightFlush(cards: List<Card>): List<Card>? {
    val sameSuitCards = findSameSuit(cards, 3) ?: return null
    return if (sameSuitCards[2].rank.ordinal - sameSuitCards[0].rank.ordinal != 2) null
    else cards - sameSuitCards
}

private fun findFourToFlush(cards: List<Card>): List<Card>? {
    val sameSuitCards = findSameSuit(cards, 4) ?: return null
    return cards - sameSuitCards
}

private fun findFourToStraightOpen(cards: List<Card>): List<Card>? {
    for (skip in cards) {
        val remaining = cards - skip
        val firstCard = remaining[0]
        val ordinalList = remaining.map { it.rank.ordinal - firstCard.rank.ordinal }
        if (ordinalList == listOf(0, 1, 2, 3) && remaining[3].rank != CardRank.ACE) {
            return listOf(skip)
        }
    }
    return null
}

private fun findFourToStraight(cards: List<Card>): List<Card>? {
    for (skip in cards) {
        val remaining = (cards - skip).map { it.rank }
        if (remaining[3].ordinal - remaining[0].ordinal <= 4) return listOf(skip)
        if (remaining[3] == CardRank.ACE && remaining[2] <= CardRank.FIVE) return listOf(skip)
    }
    return null
}

private fun findSameSuit(cards: List<Card>, numCards: Int ): List<Card>? {
    require(numCards > 1)
    val lastIndex = 5 - numCards
    for (i in 0..lastIndex) {
        val sameSuitCards = cards.filter { it.suit == cards[i].suit }
        if (sameSuitCards.size == numCards) {
            return sameSuitCards
        }
    }
    return null
}