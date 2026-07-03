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
    THREE_TO_STRAIGHT_FLUSH,
    FOUR_TO_STRAIGHT,
    FOUR_TO_STRAIGHT_OPEN,
    FOUR_TO_FLUSH
}

val drawOdds = mapOf(
    IncompleteCombinationType.FOUR_TO_FLUSH to 0.19f,
    IncompleteCombinationType.FOUR_TO_STRAIGHT_OPEN to 0.17f,
    IncompleteCombinationType.FOUR_TO_STRAIGHT to 0.09f,
    IncompleteCombinationType.THREE_TO_STRAIGHT_FLUSH to 0.12f,
    IncompleteCombinationType.NO_INCOMPLETE to 0f
)

data class Kickers(val list: List<CardRank> = emptyList()) : Comparable<Kickers> {
    override fun compareTo(other: Kickers): Int {
        val size = list.size
        if (size != other.list.size) {
            return size - other.list.size
        }
        for (i in (size - 1) downTo 0) {
            val res = list[i].compareTo(other.list[i])
            if (res != 0) return res
        }
        return 0
    }
}

data class Combination(
    val type: CombinationType,
    val highRank: CardRank,
    val lowRank: CardRank = highRank, // lowRank == highRank except cases of FULL_HOUSE or TWO_PAIRS
    val kickers: Kickers = Kickers()
) : Comparable<Combination> {
    override fun compareTo(other: Combination): Int =
        compareValuesBy(
            this,
            other,
            { it.type },
            { it.highRank },
            { it.lowRank },
            { it.kickers }
            )

    fun highKicker(): CardRank {
        val list = kickers.list
        return if (list.isEmpty()) CardRank.TWO else list.last()
    }
}

class DrawCombination(
    val onHandCombination: Combination,
    val incompleteCombination: IncompleteCombinationType = IncompleteCombinationType.NO_INCOMPLETE,
    val cardsForDraw: List<Card> = emptyList()
)