package com.example.cardgame

import kotlin.random.Random

enum class BettingStrategy {
    DROP,
    PASSIVE,
    AGGRESSIVE
}

fun selectBotStrategy(
    strength: HandStrength,
    numOfRaise: Int,
    isLatePosition: Boolean,
    isFacingBet: Boolean,
    isPreDraw: Boolean,
    drawOdds: Float
): BettingStrategy {
    val random = Random.nextFloat()
    return when (strength) {
        HandStrength.MONSTER -> BettingStrategy.AGGRESSIVE

        HandStrength.STRONG -> if (numOfRaise == 0 || !isFacingBet) BettingStrategy.AGGRESSIVE else BettingStrategy.PASSIVE

        HandStrength.MEDIUM -> when {
            !isFacingBet && isLatePosition  -> BettingStrategy.AGGRESSIVE
            !isFacingBet || numOfRaise <= 1 -> BettingStrategy.PASSIVE
            else                            -> BettingStrategy.DROP
        }

        HandStrength.DRAWING -> when {
            !isFacingBet                                            -> BettingStrategy.PASSIVE
            random < drawOdds * DRAW_RISK_FACTOR && numOfRaise <= 1 -> BettingStrategy.PASSIVE
            else                                                    -> BettingStrategy.DROP
        }

        HandStrength.WEAK -> when {
            random < BLUFF_ODD &&
                    isLatePosition &&
                    isPreDraw &&
                    numOfRaise == 0 -> BettingStrategy.AGGRESSIVE
            !isFacingBet            -> BettingStrategy.PASSIVE
            else                    -> BettingStrategy.DROP
        }
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