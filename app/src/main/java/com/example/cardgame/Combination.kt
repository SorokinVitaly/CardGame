package com.example.cardgame

enum class CombinationType {
    HIGH_CARD,
    PAIR,
    TWO_PAIRS,
    THREE_OF_A_KIND,
    STRAIGHT,
    FLUSH,
    FULL_HOUSE,
    FOUR_OF_A_KIND,
    STRAIGHT_FLUSH,
    ROYAL_FLUSH
}

enum class IncompleteCombinationType {
    NO_INCOMPLETE,
    THREE_TO_STRAIGHT_FLUSH,    // p = 2/47 * 1/46
    FOUR_TO_STRAIGHT,           // p = 4/47
    FOUR_TO_STRAIGHT_OPEN,      // p = 8/47
    FOUR_TO_FLUSH               // p = 9/47
}

data class Combination(
    val type: CombinationType,
    val highCard: Card
) : Comparable<Combination> {
    override fun compareTo(other: Combination): Int =
        compareValuesBy(this, other, { it.type }, { it.highCard })
}

class DrawCombination(
    val onHandCombination: Combination,
    val incompleteCombination: IncompleteCombinationType = IncompleteCombinationType.NO_INCOMPLETE,
    val cardsForDraw: List<Card> = emptyList()
)