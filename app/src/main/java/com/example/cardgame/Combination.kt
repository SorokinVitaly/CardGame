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

data class Combination(
    val type: CombinationType,
    val highCard: Card
) : Comparable<Combination> {
    override fun compareTo(other: Combination): Int =
        compareValuesBy(this, other, { it.type }, { it.highCard })

    companion object {
        fun calcCombination(cards: List<Card>): Combination {
            if (cards.size != 5) {
                throw IllegalArgumentException("cards.size != 5")
            }

            val firstCard = cards[0]
            val isFlush = cards.all { it.suit == firstCard.suit }

            fun calcStraight(highCard: Card): Combination =
                if (isFlush) {
                    if (firstCard.rank == CardRank.TEN) {
                        Combination(CombinationType.ROYAL_FLUSH, highCard)
                    } else {
                        Combination(CombinationType.STRAIGHT_FLUSH, highCard)
                    }
                } else {
                    Combination(CombinationType.STRAIGHT, highCard)
                }

            val ordinalList = cards.map { it.rank.ordinal - firstCard.rank.ordinal }
            if (ordinalList == listOf(0, 1, 2, 3, 4)) {
                return calcStraight(cards[4])
            }
            if (ordinalList == listOf(0, 1, 2, 3, 12)) {
                return calcStraight(cards[3])
            }
            if (isFlush) {
                return Combination(CombinationType.FLUSH, cards[4])
            }

            val card1Rank = cards[1].rank
            val card3Rank = cards[3].rank
            val count1 = cards.count { it.rank == card1Rank }
            val count3 = cards.count { it.rank == card3Rank }
            return when {
                count1 == 4 -> {
                    Combination(
                        type = CombinationType.FOUR_OF_A_KIND,
                        highCard = if (card1Rank == cards[0].rank) { cards[3] } else { cards[4] }
                    )
                }
                count1 == 3  && count3 == 2 -> {
                    Combination(
                        type = CombinationType.FULL_HOUSE,
                        highCard = cards[2]
                    )
                }
                count1 == 2  && count3 == 3 -> {
                    Combination(
                        type = CombinationType.FULL_HOUSE,
                        highCard = cards[4]
                    )
                }
                count1 == 2  && count3 == 2 -> {
                    Combination(
                        type = CombinationType.TWO_PAIRS,
                        highCard = if (card3Rank == cards[4].rank) { cards[4] } else { cards[3] }
                    )
                }
                count1 == 3 -> {
                    Combination(
                        type = CombinationType.THREE_OF_A_KIND,
                        highCard = cards.last { it.rank == card1Rank }
                    )
                }
                count3 == 3 -> {
                    Combination(
                        type = CombinationType.THREE_OF_A_KIND,
                        highCard = cards.last { it.rank == card3Rank }
                    )
                }
                count1 == 2 -> {
                    Combination(
                        type = CombinationType.PAIR,
                        highCard = if (card1Rank == cards[0].rank) { cards[1] } else { cards[2] }
                    )
                }
                count3 == 2 -> {
                    Combination(
                        type = CombinationType.PAIR,
                        highCard = if (card3Rank == cards[4].rank) { cards[4] } else { cards[3] }
                    )
                }
                else -> {
                    Combination(
                        type = CombinationType.HIGH_CARD,
                        highCard = cards[4]
                    )
                }
            }
        }
    }
}