package com.example.cardgame

enum class HandStrength {
    WEAK,
    DRAWING,
    MEDIUM,
    STRONG,
    MONSTER
}

fun calcHandStrength(combination: DrawCombination, isPreDraw: Boolean): HandStrength {
    val rank = combination.onHandCombination.highRank
    return when (combination.onHandCombination.type) {
        CombinationType.ROYAL_FLUSH,
        CombinationType.STRAIGHT_FLUSH,
        CombinationType.FOUR_OF_A_KIND,
        CombinationType.FULL_HOUSE,
        CombinationType.FLUSH,
        CombinationType.STRAIGHT,
        CombinationType.THREE_OF_A_KIND -> HandStrength.MONSTER

        CombinationType.TWO_PAIRS -> if (rank >= CardRank.KING) {
            HandStrength.MONSTER
        } else {
            HandStrength.STRONG
        }

        CombinationType.PAIR -> when {
            rank == CardRank.ACE  -> HandStrength.STRONG
            rank >= CardRank.JACK -> HandStrength.MEDIUM
            isPreDraw             -> HandStrength.MEDIUM
            else                  -> HandStrength.WEAK
            }

        CombinationType.HIGH_CARD -> if (combination.incompleteCombination != IncompleteCombinationType.NO_INCOMPLETE) {
            HandStrength.DRAWING
        } else {
            HandStrength.WEAK
        }
    }
}