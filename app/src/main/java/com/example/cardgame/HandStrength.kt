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
    val combinationType = combination.onHandCombination.type
    when {
        combinationType >= CombinationType.FOUR_OF_A_KIND -> return HandStrength.MONSTER
        combinationType >= CombinationType.THREE_OF_A_KIND -> return HandStrength.STRONG
        combinationType == CombinationType.TWO_PAIRS -> return if (rank >= CardRank.TEN) {
            HandStrength.STRONG
        } else {
            HandStrength.MEDIUM
        }
        combinationType == CombinationType.PAIR -> when {
            rank >= CardRank.JACK -> return HandStrength.MEDIUM
            rank >= CardRank.EIGHT && isPreDraw -> return HandStrength.DRAWING
        }
    }
    return if (combination.incompleteCombination != IncompleteCombinationType.NO_INCOMPLETE) {
        HandStrength.DRAWING
    } else {
        HandStrength.WEAK
    }
}