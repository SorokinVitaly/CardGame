package com.example.cardgame

import kotlin.random.Random

enum class BettingStrategy {
    DROP,
    PASSIVE,
    AGGRESSIVE
}

fun selectBotStrategy(
    strength: HandStrength,
    payToCall: Int,
    numOfRaise: Int,
    isLatePosition: Boolean,
    isPreDraw: Boolean,
    incomplete: IncompleteCombinationType
): BettingStrategy {
    val betSize = if (isPreDraw) SMALL_BET else BIG_BET
    val random = Random.nextFloat()
    return when (strength) {
        HandStrength.MONSTER -> BettingStrategy.AGGRESSIVE

        HandStrength.STRONG -> if (numOfRaise == 0 || payToCall == 0) BettingStrategy.AGGRESSIVE else BettingStrategy.PASSIVE

        HandStrength.MEDIUM -> when {
                payToCall <= betSize ->    BettingStrategy.PASSIVE
                numOfRaise >= 2 ->         BettingStrategy.DROP
                payToCall > betSize * 2 -> BettingStrategy.DROP
                else ->                    BettingStrategy.PASSIVE
            }

        HandStrength.DRAWING -> {
            val limitToPay = if (incomplete >= IncompleteCombinationType.THREE_TO_STRAIGHT_FLUSH) betSize * 2 else betSize
            if (payToCall <= limitToPay) BettingStrategy.PASSIVE else BettingStrategy.DROP
        }

        HandStrength.WEAK -> if (random < BLUFF_ODD &&
            isLatePosition &&
            isPreDraw &&
            numOfRaise == 0
        ) BettingStrategy.AGGRESSIVE else BettingStrategy.DROP
    }
}

fun resolveAction(
    strategy: BettingStrategy,
    availableActions: List<ActionType>,
): ActionType {
    if (strategy == BettingStrategy.AGGRESSIVE) {
        availableActions.find { it is ActionType.Raise }?.let { return it }
        availableActions.find { it is ActionType.Bet   }?.let { return it }
    }
    if (strategy >= BettingStrategy.PASSIVE) {
        availableActions.find { it is ActionType.Call  }?.let { return it }
    }
    availableActions.find { it is ActionType.Check }?.let { return it }
    return ActionType.Fold()
}